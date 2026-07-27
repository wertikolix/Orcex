package ru.wertik.orcex.layout

import kotlin.math.max
import ru.wertik.orcex.core.AccentType
import ru.wertik.orcex.core.MathNode

internal class DecorationLayouter(private val scope: LayoutScope) {
    fun delimited(node: MathNode.Delimited, style: MathStyle): LayoutBox =
        wrap(node.left, node.right, scope.box(node.content, style), style)

    fun wrap(leftValue: String, rightValue: String, content: LayoutBox, style: MathStyle): LayoutBox {
        val delimiterStyle = style.withSize(max(style.fontSize, content.height * 1.06f))
        val left = delimiter(leftValue, content, delimiterStyle)
        val right = delimiter(rightValue, content, delimiterStyle)
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

    private fun delimiter(value: String, content: LayoutBox, style: MathStyle): LayoutBox = when (value) {
        "" -> LayoutBox(0f, 0f, 0f, emptyList())
        "|" -> {
            val padding = style.fontSize * 0.06f
            val thickness = max(1f, style.fontSize * 0.045f)
            val ascent = content.ascent + padding
            val descent = content.descent + padding
            LayoutBox(thickness, ascent, descent, listOf(DrawCommand.Line(thickness / 2f, -ascent, thickness / 2f, descent, thickness, style.color)))
        }
        else -> scope.text(value, style)
    }

    fun boxed(node: MathNode.Boxed, style: MathStyle): LayoutBox {
        val content = scope.box(node.content, style)
        val padding = style.fontSize * 0.18f
        val thickness = max(1f, style.fontSize * 0.045f)
        val inset = padding + thickness
        val width = content.width + inset * 2f
        val ascent = content.ascent + inset
        val descent = content.descent + inset
        val top = -ascent + thickness / 2f
        val bottom = descent - thickness / 2f
        val left = thickness / 2f
        val right = width - thickness / 2f
        return LayoutBox(width, ascent, descent, buildList {
            addAll(content.translated(inset, 0f).commands)
            add(DrawCommand.Line(left, top, right, top, thickness, style.color))
            add(DrawCommand.Line(left, bottom, right, bottom, thickness, style.color))
            add(DrawCommand.Line(left, top, left, bottom, thickness, style.color))
            add(DrawCommand.Line(right, top, right, bottom, thickness, style.color))
        })
    }

    fun accent(node: MathNode.Accent, style: MathStyle): LayoutBox {
        val content = scope.box(node.content, style)
        val gap = style.fontSize * 0.07f
        val y = -content.ascent - gap
        val mark = when (node.accent) {
            AccentType.HAT -> "ˆ"
            AccentType.VEC -> "→"
            AccentType.LEFT_VEC -> "←"
            AccentType.DOT -> "˙"
            AccentType.DOUBLE_DOT -> "¨"
            AccentType.TRIPLE_DOT -> "˙˙˙"
            AccentType.TILDE -> "˜"
            AccentType.ACUTE -> "´"
            AccentType.GRAVE -> "`"
            AccentType.BREVE -> "˘"
            AccentType.CHECK -> "ˇ"
            AccentType.RING -> "˚"
            AccentType.BAR, AccentType.UNDERLINE -> null
        }
        if (node.accent == AccentType.UNDERLINE) {
            val thickness = max(1f, style.fontSize * 0.04f)
            val ruleY = content.descent + gap
            return LayoutBox(
                content.width,
                content.ascent,
                content.descent + gap + thickness,
                content.commands + DrawCommand.Line(0f, ruleY, content.width, ruleY, thickness, style.color),
            )
        }
        if (mark == null) {
            val thickness = max(1f, style.fontSize * 0.04f)
            return LayoutBox(content.width, content.ascent + gap + thickness, content.descent,
                content.commands + DrawCommand.Line(0f, y, content.width, y, thickness, style.color))
        }
        val accent = scope.text(mark, style.script())
        return LayoutBox(content.width, content.ascent + accent.height + gap, content.descent,
            content.commands + accent.translated((content.width - accent.width) / 2f, y - accent.descent).commands)
    }
}
