package ru.wertik.orcex.layout

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.math.abs
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
        val leftBracket = text.first()
        val bracketVisibleCenter = leftBracket.baseline - leftBracket.style.fontSize * (0.76f - 0.24f) / 2f
        assertTrue(abs(bracketVisibleCenter - layout.height / 2f) < 0.001f)
    }

    @Test
    fun rendersProvidedCalculusExamplesAtResponsiveSizes() {
        val formulas = listOf(
            "\\int_0^1 x^2 \\, dx = \\frac{1}{3}",
            "\\int \\frac{1}{\\sqrt{1-x^2}} \\, dx = \\arcsin(x) + C",
            "\\int_{-\\infty}^{\\infty} e^{-x^2} \\, dx = \\sqrt{\\pi}",
            "\\int x \\ln(x) \\, dx = \\frac{x^2 \\ln(x)}{2} - \\frac{x^2}{4} + C",
            "\\iint_D e^{-(x^2+y^2)} \\, dA = \\pi \\left(1 - e^{-R^2}\\right), \\quad D = \\{x^2+y^2 \\leq R^2\\}",
        )
        formulas.forEach { formula ->
            var previous: MathLayout? = null
            listOf(12f, 24f, 48f, 96f).forEach { fontSize ->
                val layout = engine.layout(parser.parse(formula), MathStyle(fontSize = fontSize))
                assertTrue(layout.width.isFinite() && layout.height.isFinite())
                assertTrue(layout.width > fontSize && layout.height > fontSize)
                previous?.let {
                    assertTrue(layout.width > it.width * 1.95f)
                    assertTrue(layout.height > it.height * 1.95f)
                }
                previous = layout
            }
        }
    }

    @Test
    fun treatsPrefixMinusAsUnaryAndEnlargesDisplayIntegrals() {
        val unary = engine.layout(parser.parse("-x"), MathStyle(fontSize = 40f))
        val binary = engine.layout(parser.parse("x-x"), MathStyle(fontSize = 40f))
        assertEquals(40f * 0.48f * 2, unary.width, 0.001f)
        assertTrue(binary.width > 40f * 0.48f * 3)

        val integral = engine.layout(parser.parse("\\iint_D"), MathStyle(fontSize = 40f))
        val sign = integral.commands.filterIsInstance<DrawCommand.Text>().first()
        assertEquals("∬", sign.value)
        assertEquals(47.2f, sign.style.fontSize, 0.001f)
    }

    @Test
    fun doesNotEmitPhantomGlyphForInvisibleDelimiter() {
        val layout = engine.layout(parser.parse("\\left.\\frac{d}{dx}\\right|_0^1"))
        val text = layout.commands.filterIsInstance<DrawCommand.Text>()
        assertTrue(text.none { it.value.isEmpty() })
        assertTrue(text.any { it.value == "|" })
    }

    @Test
    fun positionsRadicalIndexBeforeTheRootWithoutTrailingPadding() {
        val plain = engine.layout(parser.parse("\\sqrt{x}"), MathStyle(fontSize = 40f))
        val indexed = engine.layout(parser.parse("\\sqrt[3]{x}"), MathStyle(fontSize = 40f))
        val text = indexed.commands.filterIsInstance<DrawCommand.Text>()
        val index = text.first { it.value == "3" }
        val root = text.first { it.value == "√" }
        assertTrue(index.x < root.x)
        assertTrue(indexed.width - plain.width < 40f * 0.2f)
    }

    @Test
    fun laysOutAccentsTextAndAllMatrixDecorations() {
        val accents = engine.layout(parser.parse("\\hat{x}+\\bar{x}+\\vec{x}+\\dot{x}+\\tilde{x}+\\text{ok}"))
        val values = accents.commands.filterIsInstance<DrawCommand.Text>().map { it.value }
        assertTrue(values.containsAll(listOf("ˆ", "→", "˙", "˜", "ok")))
        assertTrue(accents.commands.filterIsInstance<DrawCommand.Line>().isNotEmpty())

        mapOf(
            "matrix" to emptyList(),
            "pmatrix" to listOf("(", ")"),
            "vmatrix" to listOf("|", "|"),
            "cases" to listOf("{"),
        ).forEach { (name, expectedDelimiters) ->
            val layout = engine.layout(parser.parse("\\begin{$name}x&y\\\\z&w\\end{$name}"))
            val text = layout.commands.filterIsInstance<DrawCommand.Text>().map { it.value }
            assertTrue(expectedDelimiters.all(text::contains))
            assertTrue(layout.width > 0f && layout.height > 0f)
        }
    }

    @Test
    fun mapsMathematicalAlphabetVariantsAndSpacingKinds() {
        assertEquals("Az09", MathAlphabet.apply("Az09", null))
        assertEquals("Az09", MathAlphabet.apply("Az09", ru.wertik.orcex.core.TextStyle.ROMAN))
        assertEquals("𝐀𝐳1", MathAlphabet.apply("Az1", ru.wertik.orcex.core.TextStyle.BOLD))
        assertEquals("ℎ𝑥!", MathAlphabet.apply("hx!", ru.wertik.orcex.core.TextStyle.ITALIC))
        assertEquals("ℬ𝒜ℯ", MathAlphabet.apply("BAe", ru.wertik.orcex.core.TextStyle.CALLIGRAPHIC))
        assertEquals("ℝ𝔸𝕩", MathAlphabet.apply("RAx", ru.wertik.orcex.core.TextStyle.BLACKBOARD))

        val ordinary = ru.wertik.orcex.core.MathNode.Symbol("x")
        val binary = ru.wertik.orcex.core.MathNode.Symbol("+", ru.wertik.orcex.core.SymbolKind.BINARY)
        val relation = ru.wertik.orcex.core.MathNode.Symbol("=", ru.wertik.orcex.core.SymbolKind.RELATION)
        val punctuation = ru.wertik.orcex.core.MathNode.Symbol(",", ru.wertik.orcex.core.SymbolKind.PUNCTUATION)
        val operator = ru.wertik.orcex.core.MathNode.Symbol("sin", ru.wertik.orcex.core.SymbolKind.OPERATOR)
        assertEquals(0f, MathSpacing.between(null, binary, ordinary, 10f))
        assertEquals(2.2f, MathSpacing.between(ordinary, binary, ordinary, 10f), 0.001f)
        assertEquals(2.8f, MathSpacing.between(ordinary, ordinary, relation, 10f), 0.001f)
        assertEquals(1.7f, MathSpacing.between(ordinary, punctuation, ordinary, 10f), 0.001f)
        assertEquals(1.7f, MathSpacing.between(ordinary, operator, ordinary, 10f), 0.001f)
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
