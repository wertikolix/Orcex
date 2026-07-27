package ru.wertik.orcex.core

public sealed interface MathNode {
    public data class Sequence(val children: List<MathNode>) : MathNode
    public data class Symbol(val value: String, val kind: SymbolKind = SymbolKind.ORDINARY) : MathNode
    public data class Text(val value: String) : MathNode
    public data class Space(val em: Float) : MathNode
    /**
     * @property rule Whether the fraction bar is drawn. `\binom` and `\atop` stack their
     *   arguments without one.
     */
    public data class Fraction(
        val numerator: MathNode,
        val denominator: MathNode,
        val rule: Boolean = true,
    ) : MathNode
    public data class Radical(val radicand: MathNode, val index: MathNode? = null) : MathNode
    public data class Scripts(
        val base: MathNode,
        val superscript: MathNode? = null,
        val subscript: MathNode? = null,
    ) : MathNode
    public data class Delimited(val left: String, val content: MathNode, val right: String) : MathNode
    public data class Accent(val accent: AccentType, val content: MathNode) : MathNode
    public data class Styled(val style: TextStyle, val content: MathNode) : MathNode
    public data class Matrix(val rows: List<List<MathNode>>, val environment: MatrixEnvironment) : MathNode

    /** Content rendered in an explicit ARGB [color], produced by `\textcolor` and `\color`. */
    public data class Colored(val color: Int, val content: MathNode) : MathNode

    /** Content framed by a rectangular rule, produced by `\boxed`. */
    public data class Boxed(val content: MathNode) : MathNode

    /**
     * Content stacked directly above and/or below a base, produced by `\overset`/`\underset`.
     */
    public data class Stacked(
        val base: MathNode,
        val above: MathNode? = null,
        val below: MathNode? = null,
    ) : MathNode

    /**
     * A command the parser does not know, kept verbatim so the formula around it still
     * renders.
     *
     * This is what `strictCommands = false` (the default) produces instead of throwing.
     * A formula is usually shown while it is still being typed or streamed, where an
     * unfinished or unsupported command is normal and losing the whole formula is not.
     *
     * @property command Command name without the leading backslash.
     */
    public data class Unknown(val command: String) : MathNode {
        /** Source text of the command, which is what renderers draw. */
        public val source: String get() = "\\$command"
    }

    /**
     * Content that takes up space without being drawn, produced by `\phantom` and its
     * one-dimensional variants.
     *
     * @property width Whether the content's width is kept.
     * @property height Whether the content's height is kept.
     */
    public data class Phantom(
        val content: MathNode,
        val width: Boolean = true,
        val height: Boolean = true,
    ) : MathNode
}

public enum class SymbolKind {
    ORDINARY,
    OPERATOR,
    BINARY,
    RELATION,
    OPEN,
    CLOSE,
    PUNCTUATION,
    LARGE_OPERATOR,
}

public enum class AccentType {
    HAT,
    BAR,
    VEC,
    DOT,
    TILDE,
    ACUTE,
    GRAVE,
    BREVE,
    CHECK,
    RING,
    DOUBLE_DOT,
    TRIPLE_DOT,

    /** Left-pointing arrow above the content, produced by `\overleftarrow`. */
    LEFT_VEC,

    /** Rule under the content, produced by `\underline`. */
    UNDERLINE,
}

public enum class TextStyle {
    ROMAN,
    BOLD,
    ITALIC,
    BOLD_ITALIC,
    CALLIGRAPHIC,
    BLACKBOARD,
    SANS_SERIF,
    MONOSPACE,
    FRAKTUR,
}

public enum class MatrixEnvironment {
    ALIGNED,
    MATRIX,
    PMATRIX,
    BMATRIX,
    VMATRIX,
    CASES,

    /** `Bmatrix`: curly braces. */
    BRACE_MATRIX,

    /** `Vmatrix`: double vertical bars. */
    NORM_MATRIX,

    /** `smallmatrix`: script-sized, undecorated. */
    SMALL_MATRIX,
}
