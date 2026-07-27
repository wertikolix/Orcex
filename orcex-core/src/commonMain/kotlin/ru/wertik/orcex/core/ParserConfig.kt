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

/**
 * @property enabledModules Constructs the parser is allowed to build. A disabled module
 *   fails the parse rather than degrading, since disabling it is an explicit choice.
 * @property strictCommands Whether an unsupported command fails the parse. Off by
 *   default: a formula is usually parsed while it is still being typed or streamed, and
 *   one unknown command should cost that command, not the whole formula. Unknown
 *   commands become [MathNode.Unknown] and render as their source text. Turn it on to
 *   validate formulas (authoring tools, tests, content pipelines).
 */
public data class ParserConfig(
    val enabledModules: Set<LatexModule> = LatexModule.entries.toSet(),
    val strictCommands: Boolean = false,
) {
    public fun isEnabled(module: LatexModule): Boolean = module in enabledModules
}

public class LatexParseException(message: String, public val position: Int) :
    IllegalArgumentException("$message at index $position")
