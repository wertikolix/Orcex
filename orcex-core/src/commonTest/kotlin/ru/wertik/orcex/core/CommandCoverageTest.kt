package ru.wertik.orcex.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * The catalog, as a checklist.
 *
 * Every entry has to parse into real nodes — not into [MathNode.Unknown], which is the
 * as-is fallback for commands nobody taught the parser. A gap in the catalog therefore
 * shows up here as a failure with the command name, instead of as a formula that quietly
 * renders as `\somecommand` in an app.
 */
class CommandCoverageTest {
    private val parser = LatexParser()

    private val greek = listOf(
        "alpha", "beta", "gamma", "delta", "epsilon", "varepsilon", "zeta", "eta", "theta",
        "vartheta", "iota", "kappa", "varkappa", "lambda", "mu", "nu", "xi", "omicron", "pi",
        "varpi", "rho", "varrho", "sigma", "varsigma", "tau", "upsilon", "phi", "varphi",
        "chi", "psi", "omega",
        "Gamma", "Delta", "Theta", "Lambda", "Xi", "Pi", "Sigma", "Upsilon", "Phi", "Psi", "Omega",
    )

    private val relations = listOf(
        "le", "leq", "ge", "geq", "neq", "ne", "approx", "equiv", "sim", "simeq", "cong",
        "propto", "asymp", "doteq", "ll", "gg", "leqslant", "geqslant", "lesssim", "gtrsim",
        "prec", "succ", "preceq", "succeq", "in", "notin", "ni", "subset", "subseteq",
        "supset", "supseteq", "subsetneq", "supsetneq", "nsubseteq", "nsupseteq",
        "sqsubset", "sqsupset", "sqsubseteq", "sqsupseteq", "models", "vdash", "dashv",
        "perp", "parallel", "nparallel", "mid", "nmid", "triangleq", "therefore", "because",
    )

    private val operators = listOf(
        "times", "cdot", "pm", "mp", "div", "cup", "cap", "setminus", "oplus", "ominus",
        "otimes", "oslash", "odot", "wedge", "vee", "land", "lor", "neg", "lnot", "star",
        "ast", "circ", "bullet", "dagger", "ddagger", "amalg", "uplus", "sqcup", "sqcap",
        "diamond", "triangleleft", "triangleright", "bigtriangleup", "bigtriangledown",
        "ltimes", "rtimes", "bmod", "mod",
    )

    private val bigOperators = listOf(
        "sum", "prod", "coprod", "int", "iint", "iiint", "iiiint", "oint", "oiint",
        "bigcup", "bigcap", "bigoplus", "bigotimes", "bigodot", "biguplus", "bigvee",
        "bigwedge", "bigsqcup",
    )

    private val arrows = listOf(
        "to", "gets", "rightarrow", "leftarrow", "leftrightarrow", "Rightarrow", "Leftarrow",
        "Leftrightarrow", "implies", "impliedby", "iff", "mapsto", "longrightarrow",
        "longleftarrow", "longleftrightarrow", "hookrightarrow", "hookleftarrow",
        "rightharpoonup", "rightharpoondown", "leftharpoonup", "leftharpoondown",
        "rightleftharpoons", "uparrow", "downarrow", "updownarrow", "Uparrow", "Downarrow",
        "Updownarrow", "nearrow", "searrow", "swarrow", "nwarrow",
    )

    private val symbols = listOf(
        "infty", "partial", "nabla", "forall", "exists", "nexists", "emptyset", "varnothing",
        "aleph", "hbar", "ell", "Re", "Im", "wp", "imath", "jmath", "prime", "angle",
        "measuredangle", "triangle", "square", "blacksquare", "surd", "top", "bot", "flat",
        "sharp", "natural", "clubsuit", "diamondsuit", "heartsuit", "spadesuit", "degree",
        "circledR", "checkmark", "copyright", "pounds", "S", "P", "colon", "backslash",
        "cdots", "ldots", "dots", "vdots", "ddots",
    )

    private val functions = listOf(
        "lim", "liminf", "limsup", "sin", "cos", "tan", "cot", "sec", "csc", "arcsin",
        "arccos", "arctan", "sinh", "cosh", "tanh", "coth", "arcsinh", "arccosh", "arctanh",
        "sech", "csch", "log", "ln", "lg", "exp", "det", "gcd", "min", "max", "sup", "inf",
        "arg", "deg", "dim", "hom", "ker", "Pr", "argmin", "argmax",
    )

    private val spacing = listOf(
        ",", ":", ";", "!", "quad", "qquad", "thinspace", "medspace", "thickspace",
        "negthinspace", "negmedspace", "negthickspace", "enspace", "nobreakspace",
    )

    /** Commands that need arguments, with a formula that provides them. */
    private val structural = mapOf(
        "frac" to "\\frac{1}{2}",
        "dfrac" to "\\dfrac{1}{2}",
        "tfrac" to "\\tfrac{1}{2}",
        "cfrac" to "\\cfrac{1}{2}",
        "binom" to "\\binom{n}{k}",
        "dbinom" to "\\dbinom{n}{k}",
        "tbinom" to "\\tbinom{n}{k}",
        "atop" to "\\atop{n}{k}",
        "sqrt" to "\\sqrt{x}",
        "sqrt[n]" to "\\sqrt[3]{x}",
        "left/right" to "\\left( x \\right)",
        "middle" to "\\left( a \\middle| b \\right)",
        "big" to "\\big( x \\big)",
        "Bigg" to "\\Bigg[ x \\Bigg]",
        "text" to "\\text{note}",
        "mbox" to "\\mbox{note}",
        "textbf" to "\\textbf{note}",
        "textit" to "\\textit{note}",
        "texttt" to "\\texttt{note}",
        "textsf" to "\\textsf{note}",
        "textrm" to "\\textrm{note}",
        "mathrm" to "\\mathrm{d}",
        "mathbf" to "\\mathbf{v}",
        "mathit" to "\\mathit{x}",
        "mathcal" to "\\mathcal{L}",
        "mathbb" to "\\mathbb{R}",
        "mathsf" to "\\mathsf{A}",
        "mathtt" to "\\mathtt{A}",
        "mathfrak" to "\\mathfrak{g}",
        "boldsymbol" to "\\boldsymbol{\\mu}",
        "bm" to "\\bm{x}",
        "hat" to "\\hat{x}",
        "widehat" to "\\widehat{xy}",
        "bar" to "\\bar{x}",
        "overline" to "\\overline{AB}",
        "underline" to "\\underline{AB}",
        "vec" to "\\vec{v}",
        "overrightarrow" to "\\overrightarrow{AB}",
        "overleftarrow" to "\\overleftarrow{AB}",
        "dot" to "\\dot{x}",
        "ddot" to "\\ddot{x}",
        "dddot" to "\\dddot{x}",
        "tilde" to "\\tilde{x}",
        "widetilde" to "\\widetilde{xy}",
        "acute" to "\\acute{x}",
        "grave" to "\\grave{x}",
        "breve" to "\\breve{x}",
        "check" to "\\check{x}",
        "mathring" to "\\mathring{x}",
        "operatorname" to "\\operatorname{sgn}(x)",
        "pmod" to "a \\pmod{n}",
        "overset" to "\\overset{!}{=}",
        "underset" to "\\underset{n}{\\lim}",
        "stackrel" to "\\stackrel{!}{=}",
        "boxed" to "\\boxed{x}",
        "textcolor" to "\\textcolor{red}{x}",
        "color" to "\\color{blue} x",
        "phantom" to "\\phantom{x}",
        "hphantom" to "\\hphantom{x}",
        "vphantom" to "\\vphantom{x}",
        "mathstrut" to "\\mathstrut x",
        "hspace" to "a \\hspace{1em} b",
        "kern" to "a \\kern{3pt} b",
        "not" to "a \\not= b",
        "displaystyle" to "\\displaystyle \\sum x",
        "limits" to "\\sum\\limits_{i} x",
        "label" to "x \\label{eq:1}",
        "tag" to "x \\tag{1}",
        "lvert" to "\\left\\lvert x \\right\\rvert",
        "lVert" to "\\left\\lVert x \\right\\rVert",
        "langle" to "\\left\\langle x \\right\\rangle",
        "lfloor" to "\\left\\lfloor x \\right\\rfloor",
        "lceil" to "\\left\\lceil x \\right\\rceil",
        "llbracket" to "\\left\\llbracket x \\right\\rrbracket",
    )

    private val environments = listOf(
        "matrix", "pmatrix", "bmatrix", "vmatrix", "Bmatrix", "Vmatrix", "smallmatrix",
        "cases", "aligned", "align", "align*", "alignat", "gathered", "gather", "split",
        "multline", "equation", "eqnarray",
    )

    @Test
    fun symbolCommandsAreInTheCatalog() {
        val missing = (greek + relations + operators + bigOperators + arrows + symbols + functions)
            .filter { command -> parser.parse("\\$command").containsUnknown() }

        assertEquals(emptyList(), missing, "commands rendering as raw source")
    }

    @Test
    fun spacingCommandsAreInTheCatalog() {
        spacing.forEach { command ->
            val parsed = parser.parse("a\\$command b")
            assertTrue(parsed.containsSpace(), "\\$command produced no space")
        }
    }

    @Test
    fun structuralCommandsAreInTheCatalog() {
        val missing = structural.filterValues { formula -> parser.parse(formula).containsUnknown() }

        assertEquals(emptyMap(), missing, "formulas containing an unsupported command")
    }

    @Test
    fun environmentsAreInTheCatalog() {
        environments.forEach { name ->
            val formula = if (name == "array") {
                "\\begin{array}{cc}a & b\\end{array}"
            } else {
                "\\begin{$name}a & b \\\\ c & d\\end{$name}"
            }
            val matrix = parser.parse(formula).firstMatrix()
            assertTrue(matrix != null, "\\begin{$name} did not produce a matrix")
        }
    }

    @Test
    fun arrayDropsItsColumnSpecification() {
        val matrix = parser.parse("\\begin{array}{c|c}a & b\\end{array}").firstMatrix()

        assertEquals(MatrixEnvironment.MATRIX, matrix?.environment)
        assertEquals(2, matrix?.rows?.single()?.size)
    }

    @Test
    fun binomialAndAtopStackWithoutARule() {
        val binomial = assertIsDelimited(parser.parse("\\binom{n}{k}"))
        val fraction = binomial.content as? MathNode.Fraction

        assertEquals("(" to ")", binomial.left to binomial.right)
        assertEquals(false, fraction?.rule)
        assertEquals(true, (parser.parse("\\frac{n}{k}").firstFraction())?.rule)
    }

    @Test
    fun negationUsesPrecomposedGlyphsWhereTheyExist() {
        assertEquals("≠", parser.parse("a \\not= b").symbols()[1])
        assertEquals("∉", parser.parse("a \\not\\in b").symbols()[1])
        // Anything without a precomposed form gets the combining overlay instead.
        assertEquals("⊕\u0338", parser.parse("a \\not\\oplus b").symbols()[1])
        val group = parser.parse("\\not{ab}")
        assertEquals(listOf("\u0338", "a", "b"), group.symbols())
    }

    @Test
    fun bracketNamesAndSizePrefixesResolveToDelimiters() {
        assertEquals("[", CommandCatalog.delimiter("lbrack"))
        assertEquals("]", CommandCatalog.delimiter("rbrack"))
        assertEquals("‖", CommandCatalog.delimiter("Vert"))
        assertTrue(CommandCatalog.isDelimiterSize("bigl"))
        assertTrue(!CommandCatalog.isDelimiterSize("frac"))
    }

    @Test
    fun middleAcceptsAnOmittedDelimiter() {
        // `\middle.` is the "no delimiter here" spelling, same as `\left.`.
        val omitted = parser.parse("\\left( a \\middle. b \\right)")
        assertTrue(!omitted.containsUnknown())
        assertEquals("|", parser.parse("\\left( a \\middle| b \\right)").firstDelimitedContent().symbols()[1])
    }

    @Test
    fun unsupportedDimensionsAreDroppedOrRejected() {
        assertEquals(0f, parser.parse("a \\hspace{5parsec} b").spaces().single())
        assertFailsWith<LatexParseException> {
            LatexParser(ParserConfig(strictCommands = true)).parse("a \\hspace{5parsec} b")
        }
    }

    @Test
    fun dimensionsAreConvertedToEm() {
        assertEquals(1f, LatexDimension.parseEm("1em"))
        assertEquals(2f, LatexDimension.parseEm("36mu"))
        assertEquals(0.5f, LatexDimension.parseEm("5pt"))
        assertEquals(null, LatexDimension.parseEm("5parsec"))
        assertEquals(null, LatexDimension.parseEm("em"))
    }

    private fun MathNode.containsUnknown(): Boolean = when (this) {
        is MathNode.Unknown -> true
        is MathNode.Sequence -> children.any { child -> child.containsUnknown() }
        is MathNode.Scripts -> base.containsUnknown() ||
            superscript?.containsUnknown() == true ||
            subscript?.containsUnknown() == true
        is MathNode.Fraction -> numerator.containsUnknown() || denominator.containsUnknown()
        is MathNode.Radical -> radicand.containsUnknown() || index?.containsUnknown() == true
        is MathNode.Delimited -> content.containsUnknown()
        is MathNode.Accent -> content.containsUnknown()
        is MathNode.Styled -> content.containsUnknown()
        is MathNode.Colored -> content.containsUnknown()
        is MathNode.Boxed -> content.containsUnknown()
        is MathNode.Phantom -> content.containsUnknown()
        is MathNode.Stacked -> base.containsUnknown() ||
            above?.containsUnknown() == true ||
            below?.containsUnknown() == true
        is MathNode.Matrix -> rows.flatten().any { cell -> cell.containsUnknown() }
        is MathNode.Symbol, is MathNode.Text, is MathNode.Space -> false
    }

    private fun MathNode.containsSpace(): Boolean = when (this) {
        is MathNode.Space -> true
        is MathNode.Sequence -> children.any { child -> child.containsSpace() }
        else -> false
    }

    private fun MathNode.symbols(): List<String> = when (this) {
        is MathNode.Symbol -> listOf(value)
        is MathNode.Sequence -> children.flatMap { child -> child.symbols() }
        else -> emptyList()
    }

    private fun MathNode.spaces(): List<Float> = when (this) {
        is MathNode.Space -> listOf(em)
        is MathNode.Sequence -> children.flatMap { child -> child.spaces() }
        else -> emptyList()
    }

    private fun MathNode.firstDelimitedContent(): MathNode = when (this) {
        is MathNode.Delimited -> content
        is MathNode.Sequence -> children.firstNotNullOf { child -> (child as? MathNode.Delimited)?.content }
        else -> this
    }

    private fun MathNode.firstMatrix(): MathNode.Matrix? = when (this) {
        is MathNode.Matrix -> this
        is MathNode.Sequence -> children.firstNotNullOfOrNull { child -> child.firstMatrix() }
        else -> null
    }

    private fun MathNode.firstFraction(): MathNode.Fraction? = when (this) {
        is MathNode.Fraction -> this
        is MathNode.Sequence -> children.firstNotNullOfOrNull { child -> child.firstFraction() }
        else -> null
    }

    private fun assertIsDelimited(node: MathNode): MathNode.Delimited {
        val delimited = when (node) {
            is MathNode.Delimited -> node
            is MathNode.Sequence -> node.children.filterIsInstance<MathNode.Delimited>().firstOrNull()
            else -> null
        }
        return requireNotNull(delimited) { "expected a delimited node, got $node" }
    }
}
