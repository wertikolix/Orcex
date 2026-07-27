package ru.wertik.orcex.core

internal class LatexLexer(
    private val source: String,
    private val config: ParserConfig = ParserConfig(),
) {
    fun tokenize(): List<LatexToken> = buildList {
        var index = 0
        while (index < source.length) {
            val start = index
            when (val character = source[index++]) {
                '\\' -> {
                    if (index >= source.length) {
                        // A trailing backslash is a command the author has not finished
                        // typing. Strict callers still reject it; everyone else gets an
                        // empty command name, which parses to MathNode.Unknown.
                        if (config.strictCommands) throw LatexParseException("Dangling escape", start)
                        add(LatexToken.Command("", start))
                        continue
                    }
                    if (source[index].isLetter()) {
                        val commandStart = index
                        while (index < source.length && source[index].isLetter()) index++
                        add(LatexToken.Command(source.substring(commandStart, index), start))
                    } else {
                        add(LatexToken.Command(source[index++].toString(), start))
                    }
                }
                '{' -> add(LatexToken.GroupStart(start))
                '}' -> add(LatexToken.GroupEnd(start))
                '[' -> add(LatexToken.OptionalStart(start))
                ']' -> add(LatexToken.OptionalEnd(start))
                '^' -> add(LatexToken.Superscript(start))
                '_' -> add(LatexToken.Subscript(start))
                '&' -> add(LatexToken.Alignment(start))
                ' ', '\n', '\t', '\r' -> {
                    while (index < source.length && source[index].isWhitespace()) index++
                    add(LatexToken.Whitespace(start))
                }
                else -> add(LatexToken.Character(character, start))
            }
        }
        add(LatexToken.End(source.length))
    }
}
