package ru.wertik.orcex.layout

import ru.wertik.orcex.core.TextStyle

internal object MathAlphabet {
    private val italicExceptions = mapOf('h' to 0x210E)
    private val calligraphicExceptions = mapOf(
        'B' to 0x212C, 'E' to 0x2130, 'F' to 0x2131, 'H' to 0x210B, 'I' to 0x2110,
        'L' to 0x2112, 'M' to 0x2133, 'R' to 0x211B, 'e' to 0x212F, 'g' to 0x210A,
        'o' to 0x2134,
    )
    private val blackboardExceptions = mapOf(
        'C' to 0x2102, 'H' to 0x210D, 'N' to 0x2115, 'P' to 0x2119, 'Q' to 0x211A,
        'R' to 0x211D, 'Z' to 0x2124,
    )

    fun apply(value: String, style: TextStyle?): String {
        if (style == null) return value
        val result = StringBuilder()
        value.forEach { character -> result.appendMathCodePoint(mappedCodePoint(character, style)) }
        return result.toString()
    }

    private fun mappedCodePoint(character: Char, style: TextStyle): Int = when (style) {
        TextStyle.BOLD -> range(character, 0x1D400, 0x1D41A) ?: character.code
        TextStyle.ITALIC -> italicExceptions[character] ?: range(character, 0x1D434, 0x1D44E) ?: character.code
        TextStyle.CALLIGRAPHIC -> calligraphicExceptions[character] ?: range(character, 0x1D49C, 0x1D4B6) ?: character.code
        TextStyle.BLACKBOARD -> blackboardExceptions[character] ?: range(character, 0x1D538, 0x1D552) ?: character.code
        TextStyle.ROMAN -> character.code
    }

    private fun range(character: Char, uppercase: Int, lowercase: Int): Int? = when (character) {
        in 'A'..'Z' -> uppercase + character.code - 'A'.code
        in 'a'..'z' -> lowercase + character.code - 'a'.code
        else -> null
    }

    private fun StringBuilder.appendMathCodePoint(codePoint: Int) {
        if (codePoint <= 0xFFFF) {
            append(codePoint.toChar())
        } else {
            val shifted = codePoint - 0x10000
            append((0xD800 + (shifted shr 10)).toChar())
            append((0xDC00 + (shifted and 0x3FF)).toChar())
        }
    }
}
