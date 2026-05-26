package ru.wertik.orcex.core

internal sealed interface LatexToken {
    val index: Int

    data class Character(val value: Char, override val index: Int) : LatexToken
    data class Command(val value: String, override val index: Int) : LatexToken
    data class Whitespace(override val index: Int) : LatexToken
    data class GroupStart(override val index: Int) : LatexToken
    data class GroupEnd(override val index: Int) : LatexToken
    data class OptionalStart(override val index: Int) : LatexToken
    data class OptionalEnd(override val index: Int) : LatexToken
    data class Superscript(override val index: Int) : LatexToken
    data class Subscript(override val index: Int) : LatexToken
    data class Alignment(override val index: Int) : LatexToken
    data class End(override val index: Int) : LatexToken
}
