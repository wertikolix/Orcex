package ru.wertik.orcex.render.android

import android.graphics.Paint
import android.graphics.Typeface
import ru.wertik.orcex.layout.GlyphMetrics
import ru.wertik.orcex.layout.MathFontMetrics
import ru.wertik.orcex.layout.MathStyle

public class AndroidFontMetrics(private val typeface: Typeface) : MathFontMetrics {
    private val paints: MutableMap<MathStyle, Paint> = mutableMapOf()

    override fun measure(text: String, style: MathStyle): GlyphMetrics {
        val paint = synchronized(paints) { paints.getOrPut(style) { createMathPaint(typeface, style) } }
        val fontMetrics = paint.fontMetrics
        return GlyphMetrics(
            width = paint.measureText(text),
            ascent = -fontMetrics.ascent,
            descent = fontMetrics.descent,
        )
    }
}
