package ru.wertik.orcex.layout

import kotlin.math.max
import ru.wertik.orcex.core.MathNode
import ru.wertik.orcex.core.SymbolKind
import ru.wertik.orcex.core.TextStyle

public class MathLayoutEngine(private val metrics: MathFontMetrics) {
    private val scope = object : LayoutScope {
        override fun box(node: MathNode, style: MathStyle): LayoutBox = this@MathLayoutEngine.box(node, style)
        override fun text(value: String, style: MathStyle): LayoutBox = this@MathLayoutEngine.text(value, style)
    }
    private val fractions = FractionRadicalLayouter(scope)
    private val scripts = ScriptLayouter(scope)
    private val decorations = DecorationLayouter(scope)
    private val matrices = MatrixLayouter(scope, decorations)

    public fun layout(node: MathNode, style: MathStyle = MathStyle()): MathLayout {
        val box = box(node, style)
        return MathLayout(box.width, box.height, box.ascent, box.translated(0f, box.ascent).commands)
    }

    private fun box(node: MathNode, style: MathStyle): LayoutBox = when (node) {
        is MathNode.Sequence -> sequence(node, style)
        is MathNode.Symbol -> text(node.value, symbolStyle(node, style))
        is MathNode.Text -> text(node.value, style.withVariant(TextStyle.ROMAN))
        is MathNode.Space -> LayoutBox(node.em * style.fontSize, 0f, 0f, emptyList())
        is MathNode.Fraction -> fractions.fraction(node, style)
        is MathNode.Radical -> fractions.radical(node, style)
        is MathNode.Scripts -> scripts.layout(node, style)
        is MathNode.Delimited -> decorations.delimited(node, style)
        is MathNode.Accent -> decorations.accent(node, style)
        is MathNode.Styled -> box(node.content, style.withVariant(node.style))
        is MathNode.Matrix -> matrices.layout(node, style)
    }

    private fun text(value: String, style: MathStyle): LayoutBox {
        val glyphs = MathAlphabet.apply(value, style.variant)
        val measure = metrics.measure(glyphs, style)
        return LayoutBox(measure.width, measure.ascent, measure.descent, listOf(DrawCommand.Text(glyphs, 0f, 0f, style)))
    }

    private fun sequence(node: MathNode.Sequence, style: MathStyle): LayoutBox {
        val commands = mutableListOf<DrawCommand>()
        var x = 0f
        var ascent = 0f
        var descent = 0f
        node.children.forEachIndexed { index, child ->
            if (index > 0) x += MathSpacing.between(node.children[index - 1], child, style.fontSize)
            val part = box(child, style).translated(x, 0f)
            commands += part.commands
            x += part.width
            ascent = max(ascent, part.ascent)
            descent = max(descent, part.descent)
        }
        return LayoutBox(x, ascent, descent, commands)
    }

    private fun symbolStyle(node: MathNode.Symbol, style: MathStyle): MathStyle =
        if (style.variant == null && node.kind == SymbolKind.ORDINARY && node.value.firstOrNull()?.isLetter() == true) {
            style.withVariant(TextStyle.ITALIC)
        } else style
}
