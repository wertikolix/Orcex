package ru.wertik.orcex.layout

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ru.wertik.orcex.core.LatexParser
import ru.wertik.orcex.core.MathNode
import ru.wertik.orcex.core.TextStyle

/** Layout for the commands the catalog gained: every one of them has to draw something. */
class CatalogLayoutTest {
    private val parser = LatexParser()
    private val engine = MathLayoutEngine(FixedMetrics)
    private val style = MathStyle(fontSize = 40f)

    private fun layoutOf(formula: String): MathLayout = engine.layout(parser.parse(formula), style)

    private fun textsOf(formula: String): List<String> =
        layoutOf(formula).commands.filterIsInstance<DrawCommand.Text>().map { command -> command.value }

    @Test
    fun accentMarksAreDrawnAboveTheirContent() {
        mapOf(
            "\\hat{x}" to "\u02c6",
            "\\widehat{x}" to "\u02c6",
            "\\vec{x}" to "\u2192",
            "\\overleftarrow{x}" to "\u2190",
            "\\dot{x}" to "\u02d9",
            "\\ddot{x}" to "\u00a8",
            "\\dddot{x}" to "\u02d9\u02d9\u02d9",
            "\\tilde{x}" to "\u02dc",
            "\\acute{x}" to "\u00b4",
            "\\grave{x}" to "`",
            "\\breve{x}" to "\u02d8",
            "\\check{x}" to "\u02c7",
            "\\mathring{x}" to "\u02da",
        ).forEach { (formula, mark) ->
            assertTrue(mark in textsOf(formula), "$formula did not draw $mark")
        }
    }

    @Test
    fun overlineDrawsARuleAboveAndUnderlineBelow() {
        val overline = layoutOf("\\overline{xy}")
        val underline = layoutOf("\\underline{xy}")
        val overlineRule = overline.commands.filterIsInstance<DrawCommand.Line>().single()
        val underlineRule = underline.commands.filterIsInstance<DrawCommand.Line>().single()

        // Baselines differ between the two layouts, so compare each rule with its own text.
        val overlineBaseline = overline.commands.filterIsInstance<DrawCommand.Text>().first().baseline
        val underlineBaseline = underline.commands.filterIsInstance<DrawCommand.Text>().first().baseline
        assertTrue(overlineRule.startY < overlineBaseline, "overline rule is not above the text")
        assertTrue(underlineRule.startY > underlineBaseline, "underline rule is not below the text")
        assertTrue(underline.height > underlineBaseline, "underline did not grow the box")
    }

    @Test
    fun binomialStacksWithoutARuleAndKeepsItsParentheses() {
        val binomial = layoutOf("\\binom{n}{k}")
        val fraction = layoutOf("\\frac{n}{k}")

        assertEquals(0, binomial.commands.filterIsInstance<DrawCommand.Line>().size)
        assertEquals(1, fraction.commands.filterIsInstance<DrawCommand.Line>().size)
        assertTrue("(" in textsOf("\\binom{n}{k}") && ")" in textsOf("\\binom{n}{k}"))
    }

    @Test
    fun matrixEnvironmentsUseTheirOwnFences() {
        assertTrue("{" in textsOf("\\begin{Bmatrix}a\\end{Bmatrix}"))
        assertTrue("}" in textsOf("\\begin{Bmatrix}a\\end{Bmatrix}"))
        assertTrue("\u2016" in textsOf("\\begin{Vmatrix}a\\end{Vmatrix}"))
        // smallmatrix has no fences of its own: the single cell is all that is drawn.
        assertEquals(1, textsOf("\\begin{smallmatrix}a\\end{smallmatrix}").size)
    }

    @Test
    fun smallMatrixIsScriptSized() {
        val small = layoutOf("\\begin{smallmatrix}a\\end{smallmatrix}")
        val plain = layoutOf("\\begin{matrix}a\\end{matrix}")

        assertTrue(small.height < plain.height, "smallmatrix ${small.height} vs matrix ${plain.height}")
    }

    @Test
    fun phantomsReserveSpaceWithoutDrawing() {
        val phantom = layoutOf("\\phantom{xy}")
        val horizontal = layoutOf("\\hphantom{xy}")
        val vertical = layoutOf("\\vphantom{xy}")
        val visible = layoutOf("xy")

        assertEquals(emptyList(), phantom.commands)
        assertEquals(visible.width, phantom.width)
        assertEquals(visible.width, horizontal.width)
        assertEquals(0f, horizontal.height)
        assertEquals(0f, vertical.width)
        assertEquals(visible.height, vertical.height)
    }

    @Test
    fun unsupportedCommandsAreDrawnAsSourceText() {
        val texts = textsOf("a \\nosuchcommand b")

        assertTrue("\\nosuchcommand" in texts, "got $texts")
    }

    @Test
    fun fontStylesMapToTheirUnicodeAlphabets() {
        assertEquals(listOf("\uD835\uDC00"), textsOf("\\mathbf{A}"))
        assertEquals(listOf("\uD835\uDC68"), textsOf("\\boldsymbol{A}"))
        assertEquals(listOf("\uD835\uDDA0"), textsOf("\\mathsf{A}"))
        assertEquals(listOf("\uD835\uDE70"), textsOf("\\mathtt{A}"))
        assertEquals(listOf("\uD835\uDD04"), textsOf("\\mathfrak{A}"))
        // Fraktur has precomposed letters outside the contiguous block.
        assertEquals(listOf("\u212D"), textsOf("\\mathfrak{C}"))
        // Anything without a styled form is left alone.
        assertEquals(listOf("1"), textsOf("\\mathsf{1}"))
    }

    @Test
    fun textStyleCommandsRenderTheirWordsUpright() {
        assertEquals(listOf("note"), textsOf("\\textbf{note}"))
        assertEquals(listOf("note"), textsOf("\\texttt{note}"))
    }

    @Test
    fun spacingCommandsAdvanceTheBox() {
        val tight = layoutOf("ab").width
        listOf("\\,", "\\thinspace", "\\enspace", "\\quad", "\\hspace{1em}", "\\kern{12pt}").forEach { spacer ->
            assertTrue(layoutOf("a${spacer}b").width > tight, "$spacer did not add space")
        }
        assertTrue(layoutOf("a\\!b").width < tight, "negative space did not pull in")
    }

    @Test
    fun unknownNodesReportTheirSource() {
        assertEquals("\\foo", MathNode.Unknown("foo").source)
        assertEquals(TextStyle.ROMAN, layoutOf("\\foo").commands.filterIsInstance<DrawCommand.Text>().single().style.variant)
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
