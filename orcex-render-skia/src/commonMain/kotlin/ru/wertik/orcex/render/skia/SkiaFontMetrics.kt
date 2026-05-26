package ru.wertik.orcex.render.skia

import org.jetbrains.skia.Font
import org.jetbrains.skia.Typeface
import ru.wertik.orcex.layout.GlyphMetrics
import ru.wertik.orcex.layout.MathFontMetrics
import ru.wertik.orcex.layout.MathStyle

public class SkiaFontMetrics(private val typeface: Typeface) : MathFontMetrics {
    private val fonts: MutableMap<MathStyle, Font> = mutableMapOf()

    override fun measure(text: String, style: MathStyle): GlyphMetrics {
        val font = fonts.getOrPut(style) { createMathFont(typeface, style) }
        val metrics = font.metrics
        return GlyphMetrics(
            width = font.measureTextWidth(text),
            ascent = -metrics.ascent,
            descent = metrics.descent,
        )
    }
}
