package ru.wertik.orcex.core

public sealed interface MathNode {
    public data class Sequence(val children: List<MathNode>) : MathNode
    public data class Symbol(val value: String, val kind: SymbolKind = SymbolKind.ORDINARY) : MathNode
    public data class Text(val value: String) : MathNode
    public data class Space(val em: Float) : MathNode
    public data class Fraction(val numerator: MathNode, val denominator: MathNode) : MathNode
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

public enum class AccentType { HAT, BAR, VEC, DOT, TILDE }

public enum class TextStyle { ROMAN, BOLD, ITALIC, CALLIGRAPHIC, BLACKBOARD }

public enum class MatrixEnvironment {
    ALIGNED,
    MATRIX,
    PMATRIX,
    BMATRIX,
    VMATRIX,
    CASES,
}
