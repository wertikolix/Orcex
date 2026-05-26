package ru.wertik.orcex.render.skia

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Typeface
import ru.wertik.orcex.layout.DrawCommand
import ru.wertik.orcex.layout.MathLayout
import ru.wertik.orcex.layout.MathStyle

public class SkiaMathRenderer(
    private val typeface: Typeface,
    public var color: Int,
) {
    private val fonts: MutableMap<MathStyle, Font> = mutableMapOf()
    private val textPaint: Paint = Paint().apply { isAntiAlias = true }
    private val rulePaint: Paint = Paint().apply { isAntiAlias = true }

    public fun draw(canvas: Canvas, layout: MathLayout, x: Float, y: Float) {
        layout.commands.forEach { command ->
            when (command) {
                is DrawCommand.Text -> {
                    textPaint.color = color
                    canvas.drawString(command.value, x + command.x, y + command.baseline, font(command.style), textPaint)
                }
                is DrawCommand.Line -> {
                    rulePaint.color = color
                    rulePaint.strokeWidth = command.thickness
                    canvas.drawLine(x + command.startX, y + command.startY, x + command.endX, y + command.endY, rulePaint)
                }
            }
        }
    }

    private fun font(style: MathStyle): Font = fonts.getOrPut(style) { createMathFont(typeface, style) }
}
