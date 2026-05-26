package ru.wertik.orcex.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LatexParserTest {
    private val parser = LatexParser()

    @Test
    fun parsesNestedDisplayFormula() {
        val parsed = parser.parse("\\left(\\frac{\\sum_{i=1}^{n} i^2}{\\sqrt[3]{x+\\alpha}}\\right)")
        val delimited = assertIs<MathNode.Delimited>(parsed.onlyChild())
        assertEquals("(", delimited.left)
        assertEquals(")", delimited.right)
        val fraction = assertIs<MathNode.Fraction>(delimited.content.onlyChild())
        val sum = assertIs<MathNode.Scripts>(assertIs<MathNode.Sequence>(fraction.numerator).children.first())
        assertEquals("∑", assertIs<MathNode.Symbol>(sum.base).value)
        val radical = assertIs<MathNode.Radical>(fraction.denominator.onlyChild())
        assertEquals("3", assertIs<MathNode.Symbol>(assertNotNull(radical.index).onlyChild()).value)
    }

    @Test
    fun parsesMatricesAndStyledSymbols() {
        val formula = "\\begin{pmatrix}\\mathbb{R} & x_1 \\\\ \\frac{1}{2} & \\hat{y}\\end{pmatrix}"
        val matrix = assertIs<MathNode.Matrix>(parser.parse(formula).onlyChild())
        assertEquals(MatrixEnvironment.PMATRIX, matrix.environment)
        assertEquals(2, matrix.rows.size)
        assertEquals(2, matrix.rows.first().size)
        assertIs<MathNode.Styled>(matrix.rows.first().first().onlyChild())
        assertIs<MathNode.Fraction>(matrix.rows[1].first().onlyChild())
    }

    @Test
    fun attachesUnbracedSubscriptToBaseNotSuperscript() {
        val scripts = assertIs<MathNode.Scripts>(parser.parse("x^2_3").onlyChild())
        assertEquals("x", assertIs<MathNode.Symbol>(scripts.base).value)
        assertEquals("2", assertIs<MathNode.Symbol>(scripts.superscript).value)
        assertEquals("3", assertIs<MathNode.Symbol>(scripts.subscript).value)
    }

    @Test
    fun rejectsDisabledFeatureAndMalformedInput() {
        val lite = LatexParser(ParserConfig(enabledModules = setOf(LatexModule.SCRIPTS)))
        assertFailsWith<LatexParseException> { lite.parse("\\frac{1}{2}") }
        assertFailsWith<LatexParseException> { parser.parse("\\left(x+1") }
        assertFailsWith<LatexParseException> { parser.parse("x^^2") }
        assertFailsWith<LatexParseException> { parser.parse("\\unknown{x}") }
    }

    @Test
    fun keepsSquareBracketsAsOrdinaryDelimiters() {
        val parsed = assertIs<MathNode.Sequence>(parser.parse("[ x ]") )
        assertEquals(listOf("[", "x", "]"), parsed.children.map { assertIs<MathNode.Symbol>(it).value })
        assertTrue(parsed.children.isNotEmpty())
    }

    private fun MathNode.onlyChild(): MathNode = assertIs<MathNode.Sequence>(this).children.single()
}
