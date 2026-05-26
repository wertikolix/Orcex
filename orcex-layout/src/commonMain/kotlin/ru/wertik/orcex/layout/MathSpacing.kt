package ru.wertik.orcex.layout

import ru.wertik.orcex.core.MathNode
import ru.wertik.orcex.core.SymbolKind

internal object MathSpacing {
    fun between(left: MathNode?, right: MathNode?, em: Float): Float {
        val leftKind = (left as? MathNode.Symbol)?.kind ?: return 0f
        val rightKind = (right as? MathNode.Symbol)?.kind ?: return 0f
        return when {
            leftKind == SymbolKind.BINARY || rightKind == SymbolKind.BINARY -> em * 0.22f
            leftKind == SymbolKind.RELATION || rightKind == SymbolKind.RELATION -> em * 0.28f
            leftKind == SymbolKind.PUNCTUATION -> em * 0.17f
            leftKind == SymbolKind.OPERATOR && rightKind == SymbolKind.ORDINARY -> em * 0.17f
            else -> 0f
        }
    }
}
