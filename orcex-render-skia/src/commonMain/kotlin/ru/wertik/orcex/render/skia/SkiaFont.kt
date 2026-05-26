package ru.wertik.orcex.render.skia

import org.jetbrains.skia.Font
import org.jetbrains.skia.Typeface
import ru.wertik.orcex.layout.MathStyle

internal fun createMathFont(typeface: Typeface, style: MathStyle): Font = Font(typeface, style.fontSize).apply {
    isSubpixel = true
    isLinearMetrics = true
}
