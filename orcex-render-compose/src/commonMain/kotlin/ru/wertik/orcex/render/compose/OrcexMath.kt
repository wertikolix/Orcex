package ru.wertik.orcex.render.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import ru.wertik.orcex.layout.DrawCommand
import ru.wertik.orcex.layout.MathLayout

@Composable
public fun OrcexMath(
    layout: MathLayout,
    renderer: ComposeMathRenderer,
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    contentDescription: String? = null,
) {
    val density = LocalDensity.current
    val semanticModifier = if (contentDescription == null) modifier else modifier.semantics { this.contentDescription = contentDescription }
    val sizedModifier = with(density) { semanticModifier.requiredSize(layout.width.toDp(), layout.height.toDp()) }
    Canvas(sizedModifier) {
        drawMathLayout(layout, renderer, color)
    }
}

public fun DrawScope.drawMathLayout(
    layout: MathLayout,
    renderer: ComposeMathRenderer,
    color: Color = Color.Black,
    origin: Offset = Offset.Zero,
) {
    layout.commands.forEach { command ->
        when (command) {
            is DrawCommand.Text -> {
                val textLayout = renderer.textLayout(command.value, command.style, color)
                drawText(textLayout, topLeft = Offset(origin.x + command.x, origin.y + composeTextTop(command.baseline, textLayout.firstBaseline)))
            }
            is DrawCommand.Line -> drawLine(
                color = color,
                start = Offset(origin.x + command.startX, origin.y + command.startY),
                end = Offset(origin.x + command.endX, origin.y + command.endY),
                strokeWidth = command.thickness,
            )
        }
    }
}

internal fun composeTextTop(commandBaseline: Float, textBaseline: Float): Float = commandBaseline - textBaseline
