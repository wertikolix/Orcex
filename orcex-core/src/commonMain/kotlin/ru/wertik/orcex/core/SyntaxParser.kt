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
            // `\textbf` and friends set upright text; `\mathbf` and friends style maths.
            if (CommandCatalog.isTextStyleCommand(name)) return MathNode.Styled(it, text())
            return MathNode.Styled(it, argument("style"))
        }
        if (CommandCatalog.isDelimiterSize(name)) {
            // `\big(` and friends only ask for a larger delimiter; the layout already
            // scales delimiters to their content, so the size prefix is dropped.
            return base()
        }
        return when (name) {
            "frac", "dfrac", "tfrac", "cfrac" -> fraction(rule = true)
            "binom", "dbinom", "tbinom" -> binomial()
            "atop" -> fraction(rule = false)
            "sqrt" -> radical()
            "left" -> delimited()
            "text", "mbox", "textnormal" -> text()
            "begin" -> matrix()
            "textcolor" -> colored()
            "color" -> colorDeclaration()
            "boxed", "fbox" -> boxed()
            "overset", "stackrel" -> stacked(above = true)
            "underset" -> stacked(above = false)
            "operatorname", "operatorname*" -> operatorName()
            "pmod" -> parenthesisedModulo()
            "middle" -> middleDelimiter()
            "not" -> negated()
            "mathstrut" -> MathNode.Phantom(MathNode.Symbol("("), width = false, height = true)
            // Numbering metadata: no effect on the formula itself.
            "label", "tag", "tag*" -> discardArgument()
            "phantom" -> phantom(width = true, height = true)
            "hphantom" -> phantom(width = true, height = false)
            "vphantom" -> phantom(width = false, height = true)
            "hspace", "hspace*", "kern", "mkern", "mskip", "hskip" -> horizontalSpace()
            // Style declarations that this layout resolves on its own.
            "displaystyle", "textstyle", "scriptstyle", "scriptscriptstyle", "limits", "nolimits",
            "nonumber", "notag",
                -> MathNode.Sequence(emptyList())
            else -> unsupportedCommand(name)
        }
    }

    /**
     * An unsupported command is only fatal when the caller asked for validation. By
     * default it becomes a [MathNode.Unknown] that renders as its source text, so the
     * rest of the formula survives.
     */
    private fun unsupportedCommand(name: String): MathNode {
        if (config.strictCommands) fail("Unsupported command \\$name")
        return MathNode.Unknown(name)
    }

    private fun fraction(rule: Boolean): MathNode {
        requireModule(LatexModule.FRACTIONS)
        return MathNode.Fraction(argument("numerator"), argument("denominator"), rule)
    }

    /** `\binom{n}{k}`: a rule-less fraction inside parentheses. */
    private fun binomial(): MathNode {
        requireModule(LatexModule.FRACTIONS)
        val fraction = MathNode.Fraction(argument("binomial top"), argument("binomial bottom"), rule = false)
        return MathNode.Delimited("(", fraction, ")")
    }

    /** `\operatorname{name}`: an upright multi-letter operator such as `sgn`. */
    private fun operatorName(): MathNode {
        val name = literalGroup().trim()
        return MathNode.Symbol(name, SymbolKind.OPERATOR)
    }

    /** `\pmod{n}` renders as a spaced `(mod n)`. */
    private fun parenthesisedModulo(): MathNode {
        val modulus = argument("modulus")
        return MathNode.Sequence(
            listOf(
                MathNode.Space(0.44f),
                MathNode.Delimited(
                    "(",
                    MathNode.Sequence(
                        listOf(
                            MathNode.Symbol("mod", SymbolKind.OPERATOR),
                            MathNode.Space(0.22f),
                            modulus,
                        ),
                    ),
                    ")",
                ),
            ),
        )
    }

    private fun phantom(width: Boolean, height: Boolean): MathNode =
        MathNode.Phantom(argument("phantom content"), width = width, height = height)

    /** `\middle|` inside `\left … \right`: a delimiter that does not scale on its own. */
    private fun middleDelimiter(): MathNode {
        skipWhitespace()
        val value = delimiter()
        return if (value.isEmpty()) MathNode.Sequence(emptyList()) else MathNode.Symbol(value, SymbolKind.RELATION)
    }

    /** `\not` negates the symbol that follows it. */
    private fun negated(): MathNode {
        skipWhitespace()
        val target = base()
        return if (target is MathNode.Symbol) {
            target.copy(value = CommandCatalog.negated(target.value))
        } else {
            MathNode.Sequence(listOf(MathNode.Symbol("\u0338"), target))
        }
    }

    private fun discardArgument(): MathNode {
        skipWhitespace()
        if (current() is LatexToken.GroupStart) literalGroup()
        return MathNode.Sequence(emptyList())
    }

    /** `\hspace{1em}` and the `\kern` family, in whatever unit they were written. */
    private fun horizontalSpace(): MathNode {
        val index = current().index
        val value = literalGroup().trim()
        val em = LatexDimension.parseEm(value)
            ?: if (config.strictCommands) fail("Unsupported dimension $value", index) else 0f
        return MathNode.Space(em)
    }

    private fun colored(): MathNode {
        requireModule(LatexModule.STYLING)
        val color = colorArgument()
        val content = argument("textcolor content")
        return if (color == null) content else MathNode.Colored(color, content)
    }

    private fun colorDeclaration(): MathNode {
        requireModule(LatexModule.STYLING)
        val color = colorArgument()
        // `\color` is a declaration: it applies to the remainder of the enclosing
        // group, matrix cell, or delimited body.
        val content = sequenceUntil { token ->
            token is LatexToken.GroupEnd ||
                token is LatexToken.End ||
                token is LatexToken.Alignment ||
                (token is LatexToken.Command && (token.value == "\\" || token.value == "end" || token.value == "right"))
        }
        return if (color == null) content else MathNode.Colored(color, content)
    }

    /** Colour of the declaration, or `null` when it is unknown and strictness is off. */
    private fun colorArgument(): Int? {
        skipWhitespace()
        val index = current().index
        val value = literalGroup().trim()
        val color = LatexColors.parse(value)
        if (color == null && config.strictCommands) fail("Unsupported color $value", index)
        return color
    }

    private fun boxed(): MathNode {
        requireModule(LatexModule.STYLING)
        return MathNode.Boxed(argument("boxed content"))
    }

    private fun stacked(above: Boolean): MathNode {
        requireModule(LatexModule.SCRIPTS)
        val annotation = argument(if (above) "overset annotation" else "underset annotation")
        val base = argument(if (above) "overset base" else "underset base")
        return if (above) {
            MathNode.Stacked(base = base, above = annotation)
        } else {
            MathNode.Stacked(base = base, below = annotation)
        }
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
        if (CommandCatalog.hasColumnSpecification(name)) {
            // The column specification only affects alignment, which this layout derives
            // from the environment, so it is read and dropped.
            skipWhitespace()
            if (current() is LatexToken.GroupStart) literalGroup()
        }
        val environment = CommandCatalog.environment(name)
            ?: if (config.strictCommands) {
                fail("Unsupported environment $name")
            } else {
                // Row/cell structure is the one thing every environment shares, so an
                // unknown one is laid out like `aligned` instead of losing the formula.
                MatrixEnvironment.ALIGNED
            }
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
        is LatexToken.Command -> CommandCatalog.delimiter(token.value)
            ?: if (config.strictCommands) fail("Unsupported delimiter \\${token.value}") else ""
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
