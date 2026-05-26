package ru.wertik.orcex.layout

import ru.wertik.orcex.core.MathNode

internal interface LayoutScope {
    fun box(node: MathNode, style: MathStyle): LayoutBox
    fun text(value: String, style: MathStyle): LayoutBox
}
