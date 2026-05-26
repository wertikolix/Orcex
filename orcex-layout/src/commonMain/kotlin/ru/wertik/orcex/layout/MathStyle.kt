package ru.wertik.orcex.layout

import ru.wertik.orcex.core.TextStyle

public data class MathStyle(
    val fontSize: Float = 32f,
    val scriptLevel: Int = 0,
    val variant: TextStyle? = null,
) {
    public fun script(): MathStyle = copy(fontSize = fontSize * 0.7f, scriptLevel = scriptLevel + 1)
    public fun withSize(size: Float): MathStyle = copy(fontSize = size)
    public fun withVariant(textStyle: TextStyle): MathStyle = copy(variant = textStyle)
}

public data class GlyphMetrics(
    val width: Float,
    val ascent: Float,
    val descent: Float,
    val italicCorrection: Float = 0f,
)

public fun interface MathFontMetrics {
    public fun measure(text: String, style: MathStyle): GlyphMetrics
}
