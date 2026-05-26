package ru.wertik.orcex.render.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import ru.wertik.orcex.layout.GlyphMetrics
import ru.wertik.orcex.layout.MathFontMetrics
import ru.wertik.orcex.layout.MathStyle

public class ComposeMathRenderer(
    private val textMeasurer: TextMeasurer,
    private val density: Density,
    private val fontFamily: FontFamily,
) : MathFontMetrics {
    override fun measure(text: String, style: MathStyle): GlyphMetrics {
        val result = textLayout(text, style)
        return GlyphMetrics(
            width = result.multiParagraph.width,
            ascent = result.firstBaseline,
            descent = result.multiParagraph.height - result.firstBaseline,
        )
    }

    internal fun textLayout(text: String, style: MathStyle, color: Color = Color.Unspecified): TextLayoutResult =
        textMeasurer.measure(
            text = AnnotatedString(text),
            style = TextStyle(
                color = color,
                fontSize = with(density) { style.fontSize.toSp() },
                fontFamily = fontFamily,
            ),
        )
}

@Composable
public fun rememberComposeMathRenderer(fontFamily: FontFamily): ComposeMathRenderer {
    val textMeasurer = rememberTextMeasurer()
    val density = androidx.compose.ui.platform.LocalDensity.current
    return remember(textMeasurer, density, fontFamily) { ComposeMathRenderer(textMeasurer, density, fontFamily) }
}
