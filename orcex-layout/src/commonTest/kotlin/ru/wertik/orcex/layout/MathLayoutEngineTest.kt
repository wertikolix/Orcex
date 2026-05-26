package ru.wertik.orcex.layout

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ru.wertik.orcex.core.LatexParser

class MathLayoutEngineTest {
    private val parser = LatexParser()
    private val engine = MathLayoutEngine(FixedMetrics)

    @Test
    fun laysOutComplexFormulaWithRulesAndScripts() {
        val formula = "\\left(\\frac{\\sum_{i=1}^{n} i^2}{\\sqrt[3]{x+1}}\\right)"
        val layout = engine.layout(parser.parse(formula), MathStyle(fontSize = 40f))
        assertTrue(layout.width > 80f)
        assertTrue(layout.height > 50f)
        assertTrue(layout.commands.count { it is DrawCommand.Line } >= 2)
        assertTrue(layout.commands.filterIsInstance<DrawCommand.Text>().any { it.value == "∑" })
    }

    @Test
    fun emitsMathematicalAlphabetGlyphs() {
        val layout = engine.layout(parser.parse("x+\\mathbf{A}+\\mathbb{R}+\\mathcal{F}"))
        val text = layout.commands.filterIsInstance<DrawCommand.Text>().joinToString("") { it.value }
        assertTrue("𝑥" in text)
        assertTrue("𝐀" in text)
        assertTrue("ℝ" in text)
        assertTrue("ℱ" in text)
    }

    @Test
    fun centersMatrixInsideScalableDelimiters() {
        val layout = engine.layout(parser.parse("\\begin{bmatrix}a&b\\\\c&\\frac{1}{d}\\end{bmatrix}"))
        val text = layout.commands.filterIsInstance<DrawCommand.Text>()
        assertEquals("[", text.first().value)
        assertEquals("]", text.last().value)
        assertTrue(layout.height > 45f)
    }

    private object FixedMetrics : MathFontMetrics {
        override fun measure(text: String, style: MathStyle): GlyphMetrics = GlyphMetrics(
            width = text.codePointCount() * style.fontSize * 0.48f,
            ascent = style.fontSize * 0.76f,
            descent = style.fontSize * 0.24f,
        )

        private fun String.codePointCount(): Int {
            var count = 0
            var index = 0
            while (index < length) {
                index += if (this[index].isHighSurrogate()) 2 else 1
                count++
            }
            return count
        }
    }
}
