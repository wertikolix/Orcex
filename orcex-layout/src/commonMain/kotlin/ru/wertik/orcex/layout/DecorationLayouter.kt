package ru.wertik.orcex.layout

import kotlin.math.max
import ru.wertik.orcex.core.AccentType
import ru.wertik.orcex.core.MathNode

internal class DecorationLayouter(private val scope: LayoutScope) {
    fun delimited(node: MathNode.Delimited, style: MathStyle): LayoutBox =
        wrap(node.left, node.right, scope.box(node.content, style), style)

    fun wrap(leftValue: String, rightValue: String, content: LayoutBox, style: MathStyle): LayoutBox {
        val delimiterStyle = style.withSize(max(style.fontSize, content.height * 1.06f))
        val left = if (leftValue.isEmpty()) LayoutBox(0f, 0f, 0f, emptyList()) else scope.text(leftValue, delimiterStyle)
        val right = if (rightValue.isEmpty()) LayoutBox(0f, 0f, 0f, emptyList()) else scope.text(rightValue, delimiterStyle)
        val leftBaseline = (left.ascent - left.descent) / 2f
        val rightBaseline = (right.ascent - right.descent) / 2f
        val gap = style.fontSize * 0.08f
        val contentX = left.width + gap
        val rightX = contentX + content.width + if (right.width > 0f) gap else 0f
        return LayoutBox(
            rightX + right.width,
            max(content.ascent, max(left.height / 2f, right.height / 2f)),
            max(content.descent, max(left.height / 2f, right.height / 2f)),
            left.translated(0f, leftBaseline).commands +
                content.translated(contentX, 0f).commands +
                right.translated(rightX, rightBaseline).commands,
        )
    }

    fun accent(node: MathNode.Accent, style: MathStyle): LayoutBox {
        val content = scope.box(node.content, style)
        val gap = style.fontSize * 0.07f
        val y = -content.ascent - gap
        if (node.accent == AccentType.BAR) {
            val thickness = max(1f, style.fontSize * 0.04f)
            return LayoutBox(content.width, content.ascent + gap + thickness, content.descent,
                content.commands + DrawCommand.Line(0f, y, content.width, y, thickness))
        }
        val mark = when (node.accent) {
            AccentType.HAT -> "ˆ"
            AccentType.VEC -> "→"
            AccentType.DOT -> "˙"
            AccentType.TILDE -> "˜"
            AccentType.BAR -> error("handled")
        }
        val accent = scope.text(mark, style.script())
        return LayoutBox(content.width, content.ascent + accent.height + gap, content.descent,
            content.commands + accent.translated((content.width - accent.width) / 2f, y - accent.descent).commands)
    }
}
