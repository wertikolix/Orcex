package ru.wertik.orcex.render.android

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import ru.wertik.orcex.layout.DrawCommand
import ru.wertik.orcex.layout.MathLayout
import ru.wertik.orcex.layout.MathStyle

public class CanvasMathRenderer(
    private val typeface: Typeface,
    public var color: Int,
) {
    private val paints: MutableMap<MathStyle, Paint> = mutableMapOf()
    private val rulePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    public fun draw(canvas: Canvas, layout: MathLayout, x: Float, y: Float) {
        layout.commands.forEach { command ->
            when (command) {
                is DrawCommand.Text -> canvas.drawText(command.value, x + command.x, y + command.baseline, textPaint(command.style))
                is DrawCommand.Line -> {
                    rulePaint.color = color
                    rulePaint.strokeWidth = command.thickness
                    canvas.drawLine(x + command.startX, y + command.startY, x + command.endX, y + command.endY, rulePaint)
                }
            }
        }
    }

    private fun textPaint(style: MathStyle): Paint {
        val paint = paints.getOrPut(style) { createMathPaint(typeface, style) }
        paint.color = color
        return paint
    }
}
