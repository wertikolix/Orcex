package ru.wertik.orcex.layout

import kotlin.math.max
import ru.wertik.orcex.core.MathNode

internal class ScriptLayouter(private val scope: LayoutScope) {
    fun layout(node: MathNode.Scripts, style: MathStyle): LayoutBox {
        val base = scope.box(node.base, style)
        val scriptStyle = style.script()
        val superscript = node.superscript?.let { scope.box(it, scriptStyle) }
        val subscript = node.subscript?.let { scope.box(it, scriptStyle) }
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
}
