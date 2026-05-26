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

    public fun layout(node: MathNode, style: MathStyle = MathStyle()): MathLayout = buildLayout(box(node, style))

    public fun layout(node: MathNode, style: MathStyle, constraints: MathLayoutConstraints): MathLayout {
        require(constraints.maxWidth.isFinite() && constraints.maxWidth > 0f) { "maxWidth must be finite and positive" }
        val box = if (node is MathNode.Sequence) wrappedSequence(node, style, constraints.maxWidth) else box(node, style)
        return buildLayout(box)
    }

    private fun buildLayout(box: LayoutBox): MathLayout {
        return MathLayout(box.width, box.height, box.ascent, box.translated(0f, box.ascent).commands)
    }

    private fun box(node: MathNode, style: MathStyle): LayoutBox = when (node) {
        is MathNode.Sequence -> sequence(node, style)
        is MathNode.Symbol -> symbol(node, style)
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

    private fun symbol(node: MathNode.Symbol, style: MathStyle): LayoutBox {
        val sizedStyle = if (node.kind == SymbolKind.LARGE_OPERATOR && style.scriptLevel == 0) {
            style.withSize(style.fontSize * 1.18f)
        } else {
            style
        }
        return text(node.value, symbolStyle(node, sizedStyle))
    }

    private fun sequence(node: MathNode.Sequence, style: MathStyle): LayoutBox {
        val commands = mutableListOf<DrawCommand>()
        var x = 0f
        var ascent = 0f
        var descent = 0f
        node.children.forEachIndexed { index, child ->
            if (index > 0) x += MathSpacing.between(node.children.getOrNull(index - 2), node.children[index - 1], child, style.fontSize)
            val part = box(child, style).translated(x, 0f)
            commands += part.commands
            x += part.width
            ascent = max(ascent, part.ascent)
            descent = max(descent, part.descent)
        }
        return LayoutBox(x, ascent, descent, commands)
    }

    private fun wrappedSequence(node: MathNode.Sequence, style: MathStyle, maxWidth: Float): LayoutBox {
        val whole = sequence(node, style)
        if (whole.width <= maxWidth || node.children.size < 2) return whole
        val lines = mutableListOf<LayoutBox>()
        var start = 0
        while (start < node.children.size) {
            var end = start + 1
            val breaks = mutableListOf<BreakPoint>()
            while (end <= node.children.size) {
                if (end > start + 1) breakPointBefore(node.children, end - 1)?.let(breaks::add)
                val candidate = sequence(MathNode.Sequence(node.children.subList(start, end)), style)
                if (candidate.width > maxWidth && end > start + 1) {
                    val split = preferredBreak(node.children, start, breaks, style, maxWidth)
                    if (split != null) {
                        lines += sequence(MathNode.Sequence(node.children.subList(start, split)), style)
                        start = split
                        break
                    }
                }
                if (end == node.children.size) {
                    lines += candidate
                    start = end
                    break
                }
                end++
            }
        }
        val gap = style.fontSize * 0.24f
        val commands = mutableListOf<DrawCommand>()
        var baseline = 0f
        lines.forEachIndexed { index, line ->
            if (index > 0) baseline += lines[index - 1].descent + gap + line.ascent
            commands += line.translated(0f, baseline).commands
        }
        return LayoutBox(
            width = lines.maxOf { it.width },
            ascent = lines.first().ascent,
            descent = baseline + lines.last().descent,
            commands = commands,
        )
    }

    private fun preferredBreak(children: List<MathNode>, start: Int, breaks: List<BreakPoint>, style: MathStyle, maxWidth: Float): Int? {
        val preferred = breaks.minWithOrNull(compareBy<BreakPoint> { it.penalty }.thenByDescending { it.index }) ?: return null
        if (preferred.penalty != 0) return preferred.index
        val relationWidth = sequence(MathNode.Sequence(children.subList(start, preferred.index)), style).width
        val laterOperator = breaks.filter { it.penalty > 0 }.maxByOrNull { it.index }
        return if (laterOperator != null && relationWidth < maxWidth * 0.65f) laterOperator.index else preferred.index
    }

    private fun breakPointBefore(children: List<MathNode>, index: Int): BreakPoint? {
        val node = children[index] as? MathNode.Symbol ?: return null
        return when (node.kind) {
            SymbolKind.RELATION -> BreakPoint(index, 0)
            SymbolKind.BINARY -> if (isUnaryBinary(children, index)) null else BreakPoint(index, binaryPenalty(node.value))
            else -> null
        }
    }

    private fun binaryPenalty(value: String): Int = when (value) {
        "+", "-", "−", "±", "∓" -> 1
        else -> 2
    }

    private fun isUnaryBinary(children: List<MathNode>, index: Int): Boolean {
        if (index == 0) return true
        return (children[index - 1] as? MathNode.Symbol)?.kind in setOf(SymbolKind.OPEN, SymbolKind.BINARY, SymbolKind.RELATION)
    }

    private data class BreakPoint(val index: Int, val penalty: Int)

    private fun symbolStyle(node: MathNode.Symbol, style: MathStyle): MathStyle =
        if (style.variant == null && node.kind == SymbolKind.ORDINARY && node.value.firstOrNull()?.isLetter() == true) {
            style.withVariant(TextStyle.ITALIC)
        } else style
}
