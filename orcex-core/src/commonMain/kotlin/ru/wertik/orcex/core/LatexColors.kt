package ru.wertik.orcex.core

/**
 * Resolves LaTeX color arguments to ARGB integers.
 *
 * Accepts the xcolor base names plus `#RGB`, `#RRGGBB` and `#AARRGGBB` hex forms.
 */
internal object LatexColors {
    private val named: Map<String, Int> = mapOf(
        "black" to 0xFF000000.toInt(),
        "white" to 0xFFFFFFFF.toInt(),
        "red" to 0xFFDD1111.toInt(),
        "green" to 0xFF00A000.toInt(),
        "blue" to 0xFF1144CC.toInt(),
        "cyan" to 0xFF00AAAA.toInt(),
        "magenta" to 0xFFCC00CC.toInt(),
        "yellow" to 0xFFC7A500.toInt(),
        "orange" to 0xFFE07000.toInt(),
        "purple" to 0xFF8000A0.toInt(),
        "violet" to 0xFF6A00C0.toInt(),
        "brown" to 0xFF8B5A2B.toInt(),
        "pink" to 0xFFE05580.toInt(),
        "olive" to 0xFF808000.toInt(),
        "teal" to 0xFF008080.toInt(),
        "lime" to 0xFF7FBF00.toInt(),
        "gray" to 0xFF808080.toInt(),
        "grey" to 0xFF808080.toInt(),
        "darkgray" to 0xFF404040.toInt(),
        "darkgrey" to 0xFF404040.toInt(),
        "lightgray" to 0xFFBFBFBF.toInt(),
        "lightgrey" to 0xFFBFBFBF.toInt(),
    )

    fun parse(value: String): Int? {
        val trimmed = value.trim()
        if (trimmed.startsWith("#")) return parseHex(trimmed.substring(1))
        return named[trimmed.lowercase()]
    }

    private fun parseHex(hex: String): Int? {
        return when (hex.length) {
            3 -> {
                val expanded = buildString {
                    for (char in hex) {
                        append(char)
                        append(char)
                    }
                }
                parseHex(expanded)
            }
            6 -> hex.toLongOrNull(16)?.let { rgb -> (0xFF000000L or rgb).toInt() }
            8 -> hex.toLongOrNull(16)?.toInt()
            else -> null
        }
    }
}
