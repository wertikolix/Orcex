package ru.wertik.orcex.core

internal class LatexLexer(private val source: String) {
    fun tokenize(): List<LatexToken> = buildList {
        var index = 0
        while (index < source.length) {
            val start = index
            when (val character = source[index++]) {
                '\\' -> {
                    if (index >= source.length) throw LatexParseException("Dangling escape", start)
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
