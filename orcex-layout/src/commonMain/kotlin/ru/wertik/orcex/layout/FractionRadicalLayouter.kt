package ru.wertik.orcex.layout

import kotlin.math.max
import ru.wertik.orcex.core.MathNode

internal class FractionRadicalLayouter(private val scope: LayoutScope) {
    fun fraction(node: MathNode.Fraction, style: MathStyle): LayoutBox {
        val nested = style.script()
        val numerator = scope.box(node.numerator, nested)
        val denominator = scope.box(node.denominator, nested)
        val padding = style.fontSize * 0.12f
        val gap = style.fontSize * 0.09f
        val thickness = max(1f, style.fontSize * 0.045f)
        val axisY = -style.fontSize * 0.24f
        val width = max(numerator.width, denominator.width) + padding * 2f
        val numeratorY = axisY - gap - thickness / 2f - numerator.descent
        val denominatorY = axisY + gap + thickness / 2f + denominator.ascent
        return LayoutBox(width, -numeratorY + numerator.ascent, denominatorY + denominator.descent, buildList {
            addAll(numerator.translated((width - numerator.width) / 2f, numeratorY).commands)
            add(DrawCommand.Line(0f, axisY, width, axisY, thickness, style.color))
            addAll(denominator.translated((width - denominator.width) / 2f, denominatorY).commands)
        })
    }

    fun radical(node: MathNode.Radical, style: MathStyle): LayoutBox {
        val content = scope.box(node.radicand, style)
        val root = scope.text("√", style.withSize(max(style.fontSize, content.height * 1.04f)))
        val index = node.index?.let { scope.box(it, style.script().script()) }
        val indexAdvance = (index?.width ?: 0f) * 0.55f
        val thickness = max(1f, style.fontSize * 0.04f)
        val overbarY = -content.ascent - style.fontSize * 0.06f
        val contentX = indexAdvance + root.width - style.fontSize * 0.08f
        return LayoutBox(
            contentX + content.width,
            max(root.ascent, content.ascent - overbarY),
            max(root.descent, content.descent),
            buildList {
                addAll(root.translated(indexAdvance, 0f).commands)
                addAll(content.translated(contentX, 0f).commands)
                add(DrawCommand.Line(contentX, overbarY, contentX + content.width, overbarY, thickness, style.color))
                index?.let { addAll(it.translated(0f, -root.ascent * 0.55f).commands) }
            },
        )
    }
}
