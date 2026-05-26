package ru.wertik.orcex.core

internal class SyntaxParser(
    private val tokens: List<LatexToken>,
    private val config: ParserConfig,
) {
    private var position: Int = 0

    fun parse(): MathNode {
        val result = sequenceUntil { it is LatexToken.End }
        if (current() !is LatexToken.End) fail("Unexpected token")
        return result
    }

    private fun sequenceUntil(stop: (LatexToken) -> Boolean): MathNode.Sequence {
        val children = mutableListOf<MathNode>()
        while (!stop(current())) {
            if (current() is LatexToken.End) fail("Unexpected end")
            if (current() is LatexToken.GroupEnd) fail("Unexpected closing group")
            if (current() is LatexToken.Whitespace) {
                consume()
                continue
            }
            children += atom()
        }
        return MathNode.Sequence(children)
    }

    private fun atom(): MathNode {
        val base = base()
        if (current() !is LatexToken.Superscript && current() !is LatexToken.Subscript) return base
        requireModule(LatexModule.SCRIPTS)
        var superscript: MathNode? = null
        var subscript: MathNode? = null
        while (true) {
            when (current()) {
                is LatexToken.Superscript -> {
                    if (superscript != null) fail("Duplicate superscript")
                    consume()
                    superscript = argument("superscript")
                }
                is LatexToken.Subscript -> {
                    if (subscript != null) fail("Duplicate subscript")
                    consume()
                    subscript = argument("subscript")
                }
                else -> return MathNode.Scripts(base, superscript, subscript)
            }
        }
    }

    private fun base(): MathNode = when (val token = consume()) {
        is LatexToken.Character -> character(token.value)
        is LatexToken.Whitespace -> MathNode.Space(0.22f)
        is LatexToken.GroupStart -> group()
        is LatexToken.Command -> command(token.value)
        is LatexToken.OptionalStart -> character('[')
        is LatexToken.OptionalEnd -> character(']')
        else -> fail("Expected expression", token.index)
    }

    private fun group(): MathNode {
        val result = sequenceUntil { it is LatexToken.GroupEnd }
        expect<LatexToken.GroupEnd>("Unclosed group")
        return result
    }

    private fun command(name: String): MathNode {
        CommandCatalog.symbol(name)?.let { return it }
        CommandCatalog.space(name)?.let { return it }
        CommandCatalog.accent(name)?.let {
            requireModule(LatexModule.ACCENTS)
            return MathNode.Accent(it, argument("accent"))
        }
        CommandCatalog.style(name)?.let {
            requireModule(LatexModule.STYLING)
            return MathNode.Styled(it, argument("style"))
        }
        return when (name) {
            "frac", "dfrac", "tfrac" -> fraction()
            "sqrt" -> radical()
            "left" -> delimited()
            "text" -> text()
            "begin" -> matrix()
            else -> if (config.strictCommands) fail("Unsupported command \\$name") else MathNode.Symbol("\\$name")
        }
    }

    private fun fraction(): MathNode {
        requireModule(LatexModule.FRACTIONS)
        return MathNode.Fraction(argument("numerator"), argument("denominator"))
    }

    private fun radical(): MathNode {
        requireModule(LatexModule.RADICALS)
        skipWhitespace()
        val index = if (current() is LatexToken.OptionalStart) optionalArgument() else null
        return MathNode.Radical(argument("radicand"), index)
    }

    private fun delimited(): MathNode {
        requireModule(LatexModule.DELIMITERS)
        skipWhitespace()
        val left = delimiter()
        val content = sequenceUntil { it is LatexToken.Command && it.value == "right" }
        expectCommand("right", "Missing \\right")
        skipWhitespace()
        return MathNode.Delimited(left, content, delimiter())
    }

    private fun text(): MathNode {
        requireModule(LatexModule.TEXT)
        skipWhitespace()
        expect<LatexToken.GroupStart>("Expected text group")
        val value = StringBuilder()
        var depth = 1
        while (true) {
            when (val token = consume()) {
                is LatexToken.Character -> value.append(token.value)
                is LatexToken.Whitespace -> value.append(' ')
                is LatexToken.GroupStart -> { depth++; value.append('{') }
                is LatexToken.GroupEnd -> if (--depth == 0) return MathNode.Text(value.toString()) else value.append('}')
                is LatexToken.Command -> value.append('\\').append(token.value)
                is LatexToken.End -> fail("Unclosed text group", token.index)
                is LatexToken.OptionalStart -> value.append('[')
                is LatexToken.OptionalEnd -> value.append(']')
                is LatexToken.Superscript -> value.append('^')
                is LatexToken.Subscript -> value.append('_')
                is LatexToken.Alignment -> value.append('&')
            }
        }
    }

    private fun matrix(): MathNode {
        requireModule(LatexModule.MATRICES)
        val name = literalGroup()
        val environment = CommandCatalog.environment(name) ?: fail("Unsupported environment $name")
        val rows = mutableListOf<List<MathNode>>()
        var row = mutableListOf<MathNode>()
        while (true) {
            row += sequenceUntil {
                it is LatexToken.Alignment || it is LatexToken.End || it is LatexToken.Command && (it.value == "\\" || it.value == "end")
            }
            if (current() is LatexToken.End) fail("Unclosed environment $name")
            if (current() is LatexToken.Alignment) {
                consume()
                continue
            }
            val command = consume() as LatexToken.Command
            if (command.value == "\\") {
                rows += row
                row = mutableListOf()
            } else {
                if (literalGroup() != name) fail("Environment end mismatch")
                rows += row
                return MathNode.Matrix(rows, environment)
            }
        }
    }

    private fun argument(label: String): MathNode {
        skipWhitespace()
        if (current() is LatexToken.End) fail("Missing $label")
        return if (current() is LatexToken.GroupStart) {
            consume()
            group()
        } else {
            base()
        }
    }

    private fun optionalArgument(): MathNode {
        expect<LatexToken.OptionalStart>("Expected optional argument")
        val result = sequenceUntil { it is LatexToken.OptionalEnd }
        expect<LatexToken.OptionalEnd>("Unclosed optional argument")
        return result
    }

    private fun literalGroup(): String {
        skipWhitespace()
        expect<LatexToken.GroupStart>("Expected group")
        val value = StringBuilder()
        while (true) {
            when (val token = consume()) {
                is LatexToken.Character -> value.append(token.value)
                is LatexToken.Whitespace -> value.append(' ')
                is LatexToken.GroupEnd -> return value.toString()
                else -> fail("Expected literal group", token.index)
            }
        }
    }

    private fun delimiter(): String = when (val token = consume()) {
        is LatexToken.Character -> if (token.value == '.') "" else token.value.toString()
        is LatexToken.OptionalStart -> "["
        is LatexToken.OptionalEnd -> "]"
        is LatexToken.Command -> CommandCatalog.delimiter(token.value) ?: fail("Unsupported delimiter \\${token.value}")
        else -> fail("Missing delimiter", token.index)
    }

    private fun character(value: Char): MathNode.Symbol = MathNode.Symbol(
        value.toString(),
        when (value) {
            '+', '-' -> SymbolKind.BINARY
            '=', '<', '>' -> SymbolKind.RELATION
            '(', '[' -> SymbolKind.OPEN
            ')', ']' -> SymbolKind.CLOSE
            ',', ';' -> SymbolKind.PUNCTUATION
            else -> SymbolKind.ORDINARY
        },
    )

    private inline fun <reified T : LatexToken> expect(message: String) {
        if (current() !is T) fail(message)
        consume()
    }

    private fun expectCommand(name: String, message: String) {
        val token = current()
        if (token !is LatexToken.Command || token.value != name) fail(message)
        consume()
    }

    private fun requireModule(module: LatexModule) {
        if (!config.isEnabled(module)) fail("Module $module is disabled")
    }

    private fun skipWhitespace() {
        while (current() is LatexToken.Whitespace) consume()
    }

    private fun current(): LatexToken = tokens[position]
    private fun consume(): LatexToken = tokens[position++]
    private fun fail(message: String, index: Int = current().index): Nothing = throw LatexParseException(message, index)
}
