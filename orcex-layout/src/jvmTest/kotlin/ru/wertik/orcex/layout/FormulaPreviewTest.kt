package ru.wertik.orcex.layout

import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.ceil
import kotlin.test.Test
import kotlin.test.assertTrue
import ru.wertik.orcex.core.LatexParser

class FormulaPreviewTest {
    @Test
    fun generatesFormulaPreviewUsingBundledFont() {
        val fontFile = listOf(
            File("orcex-font-stix2-android/src/main/assets/orcex/fonts/STIXTwoMath-Regular.ttf"),
            File("../orcex-font-stix2-android/src/main/assets/orcex/fonts/STIXTwoMath-Regular.ttf"),
        ).firstOrNull { it.isFile } ?: error("Bundled STIX Two Math font not found")
        val typeface = Font.createFont(Font.TRUETYPE_FONT, fontFile)
        val metrics = AwtMetrics(typeface)
        val engine = MathLayoutEngine(metrics)
        val parser = LatexParser()
        val formulas = listOf(
            "\\int_0^1 x^2 \\, dx = \\frac{1}{3}" to Float.POSITIVE_INFINITY,
            "\\int \\frac{1}{\\sqrt{1-x^2}} \\, dx = \\arcsin(x) + C" to Float.POSITIVE_INFINITY,
            "\\int_{-\\infty}^{\\infty} e^{-x^2} \\, dx = \\sqrt{\\pi}" to Float.POSITIVE_INFINITY,
            "\\int x \\ln(x) \\, dx = \\frac{x^2 \\ln(x)}{2} - \\frac{x^2}{4} + C" to Float.POSITIVE_INFINITY,
            "\\iint_D e^{-(x^2+y^2)} \\, dA = \\pi \\left(1 - e^{-R^2}\\right), \\quad D = \\{x^2+y^2 \\leq R^2\\}" to Float.POSITIVE_INFINITY,
            "\\left.x^2\\right|_0^1 = 1" to Float.POSITIVE_INFINITY,
            "\\begin{aligned}\\nabla \\cdot \\mathbf{E} &= \\frac{\\rho}{\\varepsilon_0} \\\\ \\nabla \\cdot \\mathbf{B} &= 0 \\\\ \\nabla \\times \\mathbf{E} &= -\\frac{\\partial \\mathbf{B}}{\\partial t} \\\\ \\nabla \\times \\mathbf{B} &= \\mu_0 \\mathbf{J} + \\mu_0\\,\\varepsilon_0 \\frac{\\partial \\mathbf{E}}{\\partial t}\\end{aligned}" to Float.POSITIVE_INFINITY,
            "\\iint_D e^{-(x^2+y^2)} \\, dA = \\pi \\left(1 - e^{-R^2}\\right) + \\frac{\\rho}{\\varepsilon_0} + \\sqrt{x^2+y^2}" to 520f,
        )
        val layouts = formulas.map { (formula, maxWidth) ->
            if (maxWidth.isFinite()) engine.layout(parser.parse(formula), MathStyle(fontSize = 40f), MathLayoutConstraints(maxWidth))
            else engine.layout(parser.parse(formula), MathStyle(fontSize = 40f))
        }
        val margin = 44f
        val gap = 32f
        val labelHeight = 20f
        val width = ceil(layouts.maxOf { it.width } + margin * 2).toInt()
        val height = ceil(margin + layouts.sumOf { (labelHeight + it.height + gap).toDouble() }).toInt()
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val canvas = image.createGraphics()
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        canvas.color = Color(0x11, 0x13, 0x18)
        canvas.fillRect(0, 0, width, height)
        var top = margin
        layouts.forEachIndexed { index, layout ->
            canvas.font = Font(Font.SANS_SERIF, Font.PLAIN, 13)
            canvas.color = Color(0x93, 0x9B, 0xAA)
            canvas.drawString("Formula ${index + 1} · 40sp", margin, top)
            val originY = top + labelHeight
            layout.commands.forEach { command ->
                when (command) {
                    is DrawCommand.Text -> {
                        canvas.color = Color(0xF2, 0xF3, 0xF7)
                        canvas.font = typeface.deriveFont(command.style.fontSize)
                        canvas.drawString(command.value, margin + command.x, originY + command.baseline)
                    }
                    is DrawCommand.Line -> {
                        canvas.color = Color(0xF2, 0xF3, 0xF7)
                        canvas.stroke = java.awt.BasicStroke(command.thickness)
                        canvas.drawLine(
                            (margin + command.startX).toInt(),
                            (originY + command.startY).toInt(),
                            (margin + command.endX).toInt(),
                            (originY + command.endY).toInt(),
                        )
                    }
                }
            }
            top += labelHeight + layout.height + gap
        }
        canvas.dispose()
        val output = File("build/previews/formula-preview.png")
        output.parentFile.mkdirs()
        ImageIO.write(image, "png", output)
        assertTrue(output.length() > 0)
        assertTrue(width > 700 && height > 300)
    }

    private class AwtMetrics(private val typeface: Font) : MathFontMetrics {
        override fun measure(text: String, style: MathStyle): GlyphMetrics {
            val font = typeface.deriveFont(style.fontSize)
            val image = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
            val canvas = image.createGraphics()
            canvas.font = font
            val metrics = canvas.fontMetrics
            val result = GlyphMetrics(
                width = metrics.stringWidth(text).toFloat(),
                ascent = metrics.ascent.toFloat(),
                descent = metrics.descent.toFloat(),
            )
            canvas.dispose()
            return result
        }
    }
}
