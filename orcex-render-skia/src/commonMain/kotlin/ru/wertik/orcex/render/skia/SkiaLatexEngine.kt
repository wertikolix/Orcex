package ru.wertik.orcex.render.skia

import org.jetbrains.skia.Typeface
import ru.wertik.orcex.core.LatexParser
import ru.wertik.orcex.core.ParserConfig
import ru.wertik.orcex.layout.MathLayout
import ru.wertik.orcex.layout.MathLayoutConstraints
import ru.wertik.orcex.layout.MathLayoutEngine
import ru.wertik.orcex.layout.MathStyle

public class SkiaLatexEngine(
    typeface: Typeface,
    parserConfig: ParserConfig = ParserConfig(),
) {
    private val parser = LatexParser(parserConfig)
    private val layout = MathLayoutEngine(SkiaFontMetrics(typeface))

    public fun layout(source: String, fontSize: Float): MathLayout =
        layout.layout(parser.parse(source), MathStyle(fontSize = fontSize))

    public fun layout(source: String, fontSize: Float, constraints: MathLayoutConstraints): MathLayout =
        layout.layout(parser.parse(source), MathStyle(fontSize = fontSize), constraints)
}
