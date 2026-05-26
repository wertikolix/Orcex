package ru.wertik.orcex.core

public enum class LatexModule {
    SCRIPTS,
    FRACTIONS,
    RADICALS,
    DELIMITERS,
    ACCENTS,
    MATRICES,
    TEXT,
    STYLING,
}

public data class ParserConfig(
    val enabledModules: Set<LatexModule> = LatexModule.entries.toSet(),
    val strictCommands: Boolean = true,
) {
    public fun isEnabled(module: LatexModule): Boolean = module in enabledModules
}

public class LatexParseException(message: String, public val position: Int) :
    IllegalArgumentException("$message at index $position")
