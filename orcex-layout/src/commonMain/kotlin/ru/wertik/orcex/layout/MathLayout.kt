package ru.wertik.orcex.layout

public data class MathLayout(
    val width: Float,
    val height: Float,
    val baseline: Float,
    val commands: List<DrawCommand>,
)

public sealed interface DrawCommand {
    public data class Text(
        val value: String,
        val x: Float,
        val baseline: Float,
        val style: MathStyle,
    ) : DrawCommand

    public data class Line(
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        val thickness: Float,
    ) : DrawCommand
}
