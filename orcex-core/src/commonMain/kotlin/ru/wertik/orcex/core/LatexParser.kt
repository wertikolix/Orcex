package ru.wertik.orcex.core

public class LatexParser(private val config: ParserConfig = ParserConfig()) {
    public fun parse(source: String): MathNode = SyntaxParser(LatexLexer(source).tokenize(), config).parse()
}
