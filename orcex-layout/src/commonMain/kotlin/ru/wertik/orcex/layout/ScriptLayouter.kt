package ru.wertik.orcex.layout

import kotlin.math.max
import ru.wertik.orcex.core.MathNode
import ru.wertik.orcex.core.SymbolKind

internal class ScriptLayouter(private val scope: LayoutScope) {
    fun layout(node: MathNode.Scripts, style: MathStyle): LayoutBox {
        val base = scope.box(node.base, style)
        val scriptStyle = style.script()
        val superscript = node.superscript?.let { scope.box(it, scriptStyle) }
        val subscript = node.subscript?.let { scope.box(it, scriptStyle) }
        val baseNode = node.base as? MathNode.Symbol
        if (baseNode?.kind == SymbolKind.LARGE_OPERATOR && baseNode.value in setOf("∑", "∏") && style.scriptLevel == 0) {
            return limits(base, superscript, subscript, style)
        }
        val x = base.width + style.fontSize * 0.04f
        val up = superscript?.let { max(base.ascent * 0.58f, it.descent + style.fontSize * 0.22f) } ?: 0f
        val down = subscript?.let { max(base.descent + style.fontSize * 0.22f, it.ascent * 0.7f) } ?: 0f
        return LayoutBox(
            max(base.width, x + max(superscript?.width ?: 0f, subscript?.width ?: 0f)),
            max(base.ascent, superscript?.let { up + it.ascent } ?: 0f),
            max(base.descent, subscript?.let { down + it.descent } ?: 0f),
            buildList {
                addAll(base.commands)
                superscript?.let { addAll(it.translated(x, -up).commands) }
                subscript?.let { addAll(it.translated(x, down).commands) }
            },
        )
    }

    fun stacked(node: MathNode.Stacked, style: MathStyle): LayoutBox {
        val base = scope.box(node.base, style)
        val scriptStyle = style.script()
        val above = node.above?.let { scope.box(it, scriptStyle) }
        val below = node.below?.let { scope.box(it, scriptStyle) }
        return limits(base, above, below, style)
    }

    private fun limits(base: LayoutBox, superscript: LayoutBox?, subscript: LayoutBox?, style: MathStyle): LayoutBox {
        val gap = style.fontSize * 0.1f
        val width = max(base.width, max(superscript?.width ?: 0f, subscript?.width ?: 0f))
        val superscriptY = superscript?.let { -(base.ascent + gap + it.descent) } ?: 0f
        val subscriptY = subscript?.let { base.descent + gap + it.ascent } ?: 0f
        return LayoutBox(
            width = width,
            ascent = max(base.ascent, superscript?.let { -superscriptY + it.ascent } ?: 0f),
            descent = max(base.descent, subscript?.let { subscriptY + it.descent } ?: 0f),
            commands = buildList {
                addAll(base.translated((width - base.width) / 2f, 0f).commands)
                superscript?.let { addAll(it.translated((width - it.width) / 2f, superscriptY).commands) }
                subscript?.let { addAll(it.translated((width - it.width) / 2f, subscriptY).commands) }
            },
        )
    }
}
