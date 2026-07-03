package ru.wertik.orcex.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class LatexColorAndDecorationTest {
    private val parser = LatexParser()

    @Test
    fun parsesTextcolorWithNamedColor() {
        val colored = assertIs<MathNode.Colored>(parser.parse("\\textcolor{red}{x+1}").onlyChild())
        assertEquals(0xFFDD1111.toInt(), colored.color)
        val content = assertIs<MathNode.Sequence>(colored.content)
        assertEquals("x", assertIs<MathNode.Symbol>(content.children.first()).value)
    }

    @Test
    fun parsesTextcolorWithHexColors() {
        val sixDigit = assertIs<MathNode.Colored>(parser.parse("\\textcolor{#FF8800}{x}").onlyChild())
        assertEquals(0xFFFF8800.toInt(), sixDigit.color)

        val threeDigit = assertIs<MathNode.Colored>(parser.parse("\\textcolor{#F80}{x}").onlyChild())
        assertEquals(0xFFFF8800.toInt(), threeDigit.color)

        val eightDigit = assertIs<MathNode.Colored>(parser.parse("\\textcolor{#80FF8800}{x}").onlyChild())
        assertEquals(0x80FF8800.toInt(), eightDigit.color)
    }

    @Test
    fun namedColorsAreCaseInsensitiveAndCoverCatalog(){
        assertEquals(LatexColors.parse("black"), LatexColors.parse(" BLACK "))
        listOf(
            "black", "white", "red", "green", "blue", "cyan", "magenta", "yellow",
            "orange", "purple", "violet", "brown", "pink", "olive", "teal", "lime",
            "gray", "grey", "darkgray", "darkgrey", "lightgray", "lightgrey",
        ).forEach { name ->
            assertIs<Int>(LatexColors.parse(name), name)
        }
        assertEquals(LatexColors.parse("gray"), LatexColors.parse("grey"))
    }

    @Test
    fun rejectsUnknownAndMalformedColors() {
        assertFailsWith<LatexParseException> { parser.parse("\\textcolor{nope}{x}") }
        assertFailsWith<LatexParseException> { parser.parse("\\textcolor{#12345}{x}") }
        assertFailsWith<LatexParseException> { parser.parse("\\textcolor{#GGHHII}{x}") }
        assertNull(LatexColors.parse("#ZZZ"))
        assertNull(LatexColors.parse("#AABBCCDDEE"))
    }

    @Test
    fun colorDeclarationAppliesToRestOfGroup() {
        val group = assertIs<MathNode.Sequence>(parser.parse("{a \\color{blue} b + c}").onlyChild())
        assertEquals("a", assertIs<MathNode.Symbol>(group.children.first()).value)
        val colored = assertIs<MathNode.Colored>(group.children[1])
        assertEquals(0xFF1144CC.toInt(), colored.color)
        val rest = assertIs<MathNode.Sequence>(colored.content)
        assertEquals(3, rest.children.size)
    }

    @Test
    fun colorDeclarationAppliesToRestOfInput() {
        val root = assertIs<MathNode.Sequence>(parser.parse("a \\color{teal} b"))
        val colored = assertIs<MathNode.Colored>(root.children[1])
        assertEquals("b", assertIs<MathNode.Symbol>(assertIs<MathNode.Sequence>(colored.content).children.single()).value)
    }

    @Test
    fun colorDeclarationStopsAtMatrixBoundaries() {
        val matrix = assertIs<MathNode.Matrix>(
            parser.parse("\\begin{matrix}\\color{red} x & y \\\\ \\color{blue} z \\end{matrix}").onlyChild(),
        )
        assertEquals(2, matrix.rows.size)
        val firstCell = assertIs<MathNode.Colored>(assertIs<MathNode.Sequence>(matrix.rows[0][0]).children.single())
        assertEquals("x", assertIs<MathNode.Symbol>(assertIs<MathNode.Sequence>(firstCell.content).children.single()).value)
        assertIs<MathNode.Colored>(assertIs<MathNode.Sequence>(matrix.rows[1][0]).children.single())
    }

    @Test
    fun colorDeclarationStopsBeforeRightDelimiter() {
        val delimited = assertIs<MathNode.Delimited>(parser.parse("\\left( \\color{green} x \\right)").onlyChild())
        assertIs<MathNode.Colored>(assertIs<MathNode.Sequence>(delimited.content).children.single())
    }

    @Test
    fun parsesBoxedContent() {
        val boxed = assertIs<MathNode.Boxed>(parser.parse("\\boxed{E=mc^2}").onlyChild())
        val content = assertIs<MathNode.Sequence>(boxed.content)
        assertEquals("E", assertIs<MathNode.Symbol>(content.children.first()).value)
    }

    @Test
    fun parsesOversetAndUnderset() {
        val overset = assertIs<MathNode.Stacked>(parser.parse("\\overset{!}{=}").onlyChild())
        assertEquals("=", assertIs<MathNode.Symbol>(assertIs<MathNode.Sequence>(overset.base).children.single()).value)
        assertEquals("!", assertIs<MathNode.Symbol>(assertIs<MathNode.Sequence>(overset.above).children.single()).value)
        assertNull(overset.below)

        val underset = assertIs<MathNode.Stacked>(parser.parse("\\underset{n \\to \\infty}{\\lim}").onlyChild())
        assertNull(underset.above)
        assertIs<MathNode.Sequence>(underset.below)
    }

    @Test
    fun gatesNewCommandsBehindModules() {
        val noStyling = LatexParser(ParserConfig(enabledModules = LatexModule.entries.toSet() - LatexModule.STYLING))
        assertFailsWith<LatexParseException> { noStyling.parse("\\textcolor{red}{x}") }
        assertFailsWith<LatexParseException> { noStyling.parse("\\color{red} x") }
        assertFailsWith<LatexParseException> { noStyling.parse("\\boxed{x}") }

        val noScripts = LatexParser(ParserConfig(enabledModules = LatexModule.entries.toSet() - LatexModule.SCRIPTS))
        assertFailsWith<LatexParseException> { noScripts.parse("\\overset{a}{b}") }
        assertFailsWith<LatexParseException> { noScripts.parse("\\underset{a}{b}") }
    }

    private fun MathNode.onlyChild(): MathNode = assertIs<MathNode.Sequence>(this).children.single()
}
