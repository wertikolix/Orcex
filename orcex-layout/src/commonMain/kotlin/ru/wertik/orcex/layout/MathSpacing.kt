package ru.wertik.orcex.layout

import ru.wertik.orcex.core.MathNode
import ru.wertik.orcex.core.SymbolKind

internal object MathSpacing {
    fun between(beforeLeft: MathNode?, left: MathNode?, right: MathNode?, em: Float): Float {
        if (isUnaryBinary(left, beforeLeft) && isComposite(right)) return em * 0.1f
        val leftKind = effectiveKind(left, beforeLeft) ?: return 0f
        val rightKind = effectiveKind(right, left) ?: return 0f
        return when {
            leftKind == SymbolKind.BINARY || rightKind == SymbolKind.BINARY -> em * 0.22f
            leftKind == SymbolKind.RELATION || rightKind == SymbolKind.RELATION -> em * 0.28f
            leftKind == SymbolKind.PUNCTUATION -> em * 0.17f
            rightKind == SymbolKind.OPERATOR && leftKind == SymbolKind.ORDINARY -> em * 0.17f
            leftKind == SymbolKind.OPERATOR && rightKind == SymbolKind.ORDINARY -> em * 0.17f
            else -> 0f
        }
    }

    private fun isUnaryBinary(node: MathNode?, previous: MathNode?): Boolean =
        node is MathNode.Symbol && node.kind == SymbolKind.BINARY &&
            (previous == null || kind(previous) in setOf(SymbolKind.OPEN, SymbolKind.BINARY, SymbolKind.RELATION))

    private fun isComposite(node: MathNode?): Boolean = node is MathNode.Fraction || node is MathNode.Radical || node is MathNode.Delimited

    private fun effectiveKind(node: MathNode?, previous: MathNode?): SymbolKind? {
        val kind = kind(node) ?: return null
        if (kind != SymbolKind.BINARY) return kind
        return if (previous == null || kind(previous) in setOf(SymbolKind.OPEN, SymbolKind.BINARY, SymbolKind.RELATION)) {
            SymbolKind.ORDINARY
        } else {
            kind
        }
    }

    private fun kind(node: MathNode?): SymbolKind? = when (node) {
        is MathNode.Symbol -> node.kind
        is MathNode.Scripts -> kind(node.base)
        is MathNode.Styled -> kind(node.content)
        is MathNode.Colored -> kind(node.content)
        is MathNode.Stacked -> kind(node.base)
        is MathNode.Sequence -> node.children.singleOrNull()?.let(::kind)
        null, is MathNode.Space -> null
        else -> SymbolKind.ORDINARY
    }
}
