package ru.wertik.orcex.layout

internal data class LayoutBox(
    val width: Float,
    val ascent: Float,
    val descent: Float,
    val commands: List<DrawCommand>,
) {
    val height: Float get() = ascent + descent

    fun translated(x: Float, y: Float): LayoutBox = copy(commands = commands.map { command ->
        when (command) {
            is DrawCommand.Text -> command.copy(x = command.x + x, baseline = command.baseline + y)
            is DrawCommand.Line -> command.copy(
                startX = command.startX + x,
                startY = command.startY + y,
                endX = command.endX + x,
                endY = command.endY + y,
            )
        }
    })
}
