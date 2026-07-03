package ru.wertik.orcex.layout

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ru.wertik.orcex.core.LatexParser

class ColorAndDecorationLayoutTest {
    private val parser = LatexParser()
    private val engine = MathLayoutEngine(FixedMetrics)

    @Test
    fun coloredSubtreeCarriesColorOnTextCommands() {
        val layout = engine.layout(parser.parse("a\\textcolor{red}{b}c"), MathStyle(fontSize = 40f))
        val texts = layout.commands.filterIsInstance<DrawCommand.Text>()
        assertEquals(3, texts.size)
        assertNull(texts[0].style.color)
        assertEquals(0xFFDD1111.toInt(), texts[1].style.color)
        assertNull(texts[2].style.color)
    }

    @Test
    fun coloredFractionPropagatesColorToRule() {
        val layout = engine.layout(parser.parse("\\textcolor{blue}{\\frac{1}{2}}"), MathStyle(fontSize = 40f))
        val bar = layout.commands.filterIsInstance<DrawCommand.Line>().single()
        assertEquals(0xFF1144CC.toInt(), bar.color)
    }

    @Test
    fun uncoloredRulesInheritRendererColor() {
        val layout = engine.layout(parser.parse("\\frac{1}{2}\\sqrt{x}\\bar{y}\\left|z\\right|"), MathStyle(fontSize = 40f))
        val lines = layout.commands.filterIsInstance<DrawCommand.Line>()
        assertTrue(lines.isNotEmpty())
        lines.forEach { line -> assertNull(line.color) }
    }

    @Test
    fun boxedContentIsFramedByFourRules() {
        val plain = engine.layout(parser.parse("x"), MathStyle(fontSize = 40f))
        val layout = engine.layout(parser.parse("\\boxed{x}"), MathStyle(fontSize = 40f))
        val lines = layout.commands.filterIsInstance<DrawCommand.Line>()
        assertEquals(4, lines.size)
        assertTrue(layout.width > plain.width)
        assertTrue(layout.height > plain.height)

        val horizontal = lines.filter { it.startY == it.endY }
        val vertical = lines.filter { it.startX == it.endX }
        assertEquals(2, horizontal.size)
        assertEquals(2, vertical.size)
        // The frame must fully enclose the content glyph.
        val glyph = layout.commands.filterIsInstance<DrawCommand.Text>().single()
        assertTrue(horizontal.minOf { it.startY } < glyph.baseline)
        assertTrue(horizontal.maxOf { it.startY } > glyph.baseline)
        assertTrue(vertical.minOf { it.startX } < glyph.x)
    }

    @Test
    fun boxedFrameInheritsSubtreeColor() {
        val layout = engine.layout(parser.parse("\\textcolor{teal}{\\boxed{x}}"), MathStyle(fontSize = 40f))
        val lines = layout.commands.filterIsInstance<DrawCommand.Line>()
        assertEquals(4, lines.size)
        lines.forEach { line -> assertEquals(0xFF008080.toInt(), line.color) }
    }

    @Test
    fun oversetCentersAnnotationAboveBase() {
        val layout = engine.layout(parser.parse("a\\overset{!}{=}b"), MathStyle(fontSize = 40f))
        val texts = layout.commands.filterIsInstance<DrawCommand.Text>()
        val annotation = texts.first { it.value == "!" }
        val base = texts.first { it.value == "=" }
        assertTrue(annotation.baseline < base.baseline)
        assertTrue(annotation.style.fontSize < base.style.fontSize)
        // Centered horizontally over the base glyph.
        val annotationCenter = annotation.x + annotation.style.fontSize * 0.48f / 2f
        val baseCenter = base.x + base.style.fontSize * 0.48f / 2f
        assertTrue(kotlin.math.abs(annotationCenter - baseCenter) < 0.6f)
    }

    @Test
    fun undersetPlacesAnnotationBelowBase() {
        val layout = engine.layout(parser.parse("\\underset{0}{x}"), MathStyle(fontSize = 40f))
        val texts = layout.commands.filterIsInstance<DrawCommand.Text>()
        val annotation = texts.first { it.value == "0" }
        val base = texts.first { it.value != "0" }
        assertTrue(annotation.baseline > base.baseline)
    }

    @Test
    fun stackedRelationKeepsRelationSpacing() {
        val stacked = engine.layout(parser.parse("a\\overset{!}{=}b"), MathStyle(fontSize = 40f))
        val plain = engine.layout(parser.parse("a=b"), MathStyle(fontSize = 40f))
        val stackedTexts = stacked.commands.filterIsInstance<DrawCommand.Text>()
        val plainTexts = plain.commands.filterIsInstance<DrawCommand.Text>()
        val stackedGap = stackedTexts.first { it.value == "=" }.x - (stackedTexts.first { it.value == "𝑎" }.x + 40f * 0.48f)
        val plainGap = plainTexts.first { it.value == "=" }.x - (plainTexts.first { it.value == "𝑎" }.x + 40f * 0.48f)
        assertTrue(stackedGap >= plainGap - 0.6f)
    }

    @Test
    fun colorDeclarationSpansSpacingLikeUncolored() {
        val colored = engine.layout(parser.parse("a \\textcolor{red}{=} b"), MathStyle(fontSize = 40f))
        val plain = engine.layout(parser.parse("a = b"), MathStyle(fontSize = 40f))
        assertEquals(plain.width, colored.width)
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
