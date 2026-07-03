package ru.wertik.orcex.layout

public data class MathLayout(
    val width: Float,
    val height: Float,
    val baseline: Float,
    val commands: List<DrawCommand>,
)

public data class MathLayoutConstraints(
    val maxWidth: Float,
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
        /** Explicit ARGB color for this rule, or `null` to inherit the renderer color. */
        val color: Int? = null,
    ) : DrawCommand
}
