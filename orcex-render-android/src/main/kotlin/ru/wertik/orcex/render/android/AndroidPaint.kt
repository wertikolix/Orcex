package ru.wertik.orcex.render.android

import android.graphics.Paint
import android.graphics.Typeface
import ru.wertik.orcex.layout.MathStyle

internal fun createMathPaint(typeface: Typeface, style: MathStyle): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    this.typeface = typeface
    textSize = style.fontSize
    isSubpixelText = true
    isLinearText = true
}
