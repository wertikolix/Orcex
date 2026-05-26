package ru.wertik.orcex.layout

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.math.abs
import ru.wertik.orcex.core.LatexParser
import ru.wertik.orcex.core.MathNode

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
    fun rendersTypographyRegressionsAtResponsiveSizes() {
        val examples = listOf(
            "\\left.\\frac{x^2}{2}\\right|_0^1 = \\frac{1}{2}" to false,
            "\\begin{aligned}\\nabla \\cdot \\mathbf{E} &= \\frac{\\rho}{\\varepsilon_0} \\\\ \\nabla \\cdot \\mathbf{B} &= 0 \\\\ \\nabla \\times \\mathbf{E} &= -\\frac{\\partial \\mathbf{B}}{\\partial t} \\\\ \\nabla \\times \\mathbf{B} &= \\mu_0 \\mathbf{J} + \\mu_0\\,\\varepsilon_0 \\frac{\\partial \\mathbf{E}}{\\partial t}\\end{aligned}" to false,
            "e^x = 1 + x + \\frac{x^2}{2} + \\frac{x^3}{6} + \\frac{x^4}{24} + \\frac{x^5}{120} + \\cdots" to true,
        )
        listOf(12f, 24f, 48f, 96f).forEach { fontSize ->
            examples.forEach { (formula, constrained) ->
                val node = parser.parse(formula)
                val layout = if (constrained) {
                    engine.layout(node, MathStyle(fontSize = fontSize), MathLayoutConstraints(fontSize * 12.5f))
                } else {
                    engine.layout(node, MathStyle(fontSize = fontSize))
                }
                assertTrue(layout.width.isFinite() && layout.height.isFinite())
                assertTrue(layout.commands.isNotEmpty())
                if (constrained) assertTrue(layout.height > fontSize)
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
    fun stacksSumLimitsButKeepsIntegralLimitsBesideTheOperator() {
        val layout = engine.layout(parser.parse("\\int_0^1 x + \\sum_{i=1}^{n} i"), MathStyle(fontSize = 40f))
        val text = layout.commands.filterIsInstance<DrawCommand.Text>()
        val integral = text.first { it.value == "∫" }
        val integralLower = text.first { it.value == "0" }
        val integralUpper = text.first { it.value == "1" }
        val sum = text.first { it.value == "∑" }
        val sumLower = text.first { it.value == "𝑖" }
        val sumUpper = text.first { it.value == "𝑛" }
        assertTrue(integralLower.x > integral.x)
        assertTrue(integralUpper.x > integral.x)
        assertTrue(sumLower.x < sum.x + sum.style.fontSize)
        assertTrue(sumUpper.x < sum.x + sum.style.fontSize)
    }

    @Test
    fun doesNotEmitPhantomGlyphForInvisibleDelimiter() {
        val layout = engine.layout(parser.parse("\\left.\\frac{d}{dx}\\right|_0^1"))
        val text = layout.commands.filterIsInstance<DrawCommand.Text>()
        assertTrue(text.none { it.value.isEmpty() })
        val evaluationBar = layout.commands.filterIsInstance<DrawCommand.Line>().maxBy { it.endY - it.startY }
        assertTrue(evaluationBar.endY - evaluationBar.startY > 40f)
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
            "cases" to listOf("{"),
        ).forEach { (name, expectedDelimiters) ->
            val layout = engine.layout(parser.parse("\\begin{$name}x&y\\\\z&w\\end{$name}"))
            val text = layout.commands.filterIsInstance<DrawCommand.Text>().map { it.value }
            assertTrue(expectedDelimiters.all(text::contains))
            assertTrue(layout.width > 0f && layout.height > 0f)
        }
        val determinant = engine.layout(parser.parse("\\begin{vmatrix}x&y\\\\z&w\\end{vmatrix}"))
        assertTrue(determinant.commands.filterIsInstance<DrawCommand.Line>().count { it.startX == it.endX } >= 2)
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
        assertEquals(1.7f, MathSpacing.between(ordinary, ordinary, operator, 10f), 0.001f)
        assertEquals(1f, MathSpacing.between(null, binary, MathNode.Fraction(ordinary, ordinary), 10f), 0.001f)
    }

    @Test
    fun keepsFractionAxisAboveTheSurroundingTextBaseline() {
        val layout = engine.layout(parser.parse("x=\\frac{1}{2}+y"), MathStyle(fontSize = 40f))
        val bar = layout.commands.filterIsInstance<DrawCommand.Line>().single()
        val equals = layout.commands.filterIsInstance<DrawCommand.Text>().first { it.value == "=" }
        assertTrue(bar.startY < equals.baseline - 40f * 0.15f)
    }

    @Test
    fun laysOutMaxwellEquationsAlignedOnRelationSigns() {
        val layout = engine.layout(parser.parse(
            "\\begin{aligned}" +
                "\\nabla \\cdot \\mathbf{E} &= \\frac{\\rho}{\\varepsilon_0} \\\\ " +
                "\\nabla \\cdot \\mathbf{B} &= 0 \\\\ " +
                "\\nabla \\times \\mathbf{E} &= -\\frac{\\partial \\mathbf{B}}{\\partial t} \\\\ " +
                "\\nabla \\times \\mathbf{B} &= \\mu_0 \\mathbf{J} + \\mu_0\\varepsilon_0 \\frac{\\partial \\mathbf{E}}{\\partial t}" +
                "\\end{aligned}",
        ), MathStyle(fontSize = 32f))
        val equalsSigns = layout.commands.filterIsInstance<DrawCommand.Text>().filter { it.value == "=" }
        assertEquals(4, equalsSigns.size)
        assertTrue(equalsSigns.map { it.x }.distinct().size == 1)
        assertTrue(layout.height > 32f * 4)
    }

    @Test
    fun automaticallyWrapsLongTopLevelExpressionsAtOperators() {
        val formula = parser.parse("a+b+c+d+e+f=g+h+i+j")
        val oneLine = engine.layout(formula, MathStyle(fontSize = 32f))
        val fitting = engine.layout(formula, MathStyle(fontSize = 32f), MathLayoutConstraints(oneLine.width + 1f))
        val wrapped = engine.layout(formula, MathStyle(fontSize = 32f), MathLayoutConstraints(maxWidth = 150f))
        val multiplied = engine.layout(parser.parse("a\\cdot b\\cdot c"), MathStyle(fontSize = 32f), MathLayoutConstraints(maxWidth = 52f))
        val unbreakable = engine.layout(parser.parse("\\frac{123456789}{123456789}"), MathStyle(fontSize = 32f), MathLayoutConstraints(maxWidth = 20f))
        assertTrue(oneLine.width > 150f)
        assertEquals(oneLine, fitting)
        assertTrue(wrapped.width <= 150f)
        assertTrue(wrapped.height > oneLine.height * 2f)
        assertTrue(multiplied.height > 32f)
        assertTrue(unbreakable.width > 20f)
        val continuationOperators = wrapped.commands.filterIsInstance<DrawCommand.Text>().filter { it.value in setOf("+", "=") }
        assertTrue(continuationOperators.any { it.x == 0f })
    }

    @Test
    fun wrapsAtRelationsBeforeOperatorsAndKeepsUnaryMinusAttached() {
        val formula = parser.parse("a+b+c=d-\\frac{e}{f}+g")
        val wrapped = engine.layout(formula, MathStyle(fontSize = 32f), MathLayoutConstraints(150f))
        val equals = wrapped.commands.filterIsInstance<DrawCommand.Text>().first { it.value == "=" }
        val minus = wrapped.commands.filterIsInstance<DrawCommand.Text>().first { it.value == "-" }
        assertEquals(0f, equals.x)
        assertTrue(minus.x > equals.x)
        assertFailsWith<IllegalArgumentException> { engine.layout(formula, MathStyle(), MathLayoutConstraints(0f)) }
        assertFailsWith<IllegalArgumentException> { engine.layout(formula, MathStyle(), MathLayoutConstraints(Float.NaN)) }
        assertFailsWith<IllegalArgumentException> { engine.layout(formula, MathStyle(), MathLayoutConstraints(Float.POSITIVE_INFINITY)) }
    }

    @Test
    fun preservesAlignedBlocksWhenConstrained() {
        val formula = parser.parse("\\begin{aligned}a &= b + c \\\\ d &= e + f\\end{aligned}")
        val unconstrained = engine.layout(formula, MathStyle(fontSize = 32f))
        val constrained = engine.layout(formula, MathStyle(fontSize = 32f), MathLayoutConstraints(20f))
        assertEquals(unconstrained, constrained)
    }

    @Test
    fun spacesRelationsAndBinariesAroundCompositeAtoms() {
        val relation = engine.layout(parser.parse("x=\\frac{1}{2}"), MathStyle(fontSize = 40f))
        val binary = engine.layout(parser.parse("\\sqrt{x}+y"), MathStyle(fontSize = 40f))
        val faraday = engine.layout(parser.parse("-\\frac{\\partial B}{\\partial t}"), MathStyle(fontSize = 40f))
        val relationText = relation.commands.filterIsInstance<DrawCommand.Text>()
        val binaryText = binary.commands.filterIsInstance<DrawCommand.Text>()
        assertTrue(relationText.first { it.value == "=" }.x + 40f * 0.48f + 40f * 0.28f <= relation.commands.filterIsInstance<DrawCommand.Line>().first().startX)
        assertTrue(binaryText.first { it.value == "+" }.x > binaryText.first { it.value == "𝑥" }.x)
        assertTrue(faraday.commands.filterIsInstance<DrawCommand.Line>().first().startX > faraday.commands.filterIsInstance<DrawCommand.Text>().first { it.value == "-" }.x + 40f * 0.48f)
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
