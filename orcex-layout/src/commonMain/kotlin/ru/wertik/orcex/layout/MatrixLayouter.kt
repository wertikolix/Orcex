package ru.wertik.orcex.layout

import kotlin.math.max
import ru.wertik.orcex.core.MathNode
import ru.wertik.orcex.core.MatrixEnvironment

internal class MatrixLayouter(
    private val scope: LayoutScope,
    private val decorations: DecorationLayouter,
) {
    fun layout(node: MathNode.Matrix, style: MathStyle): LayoutBox {
        val cells = node.rows.map { row -> row.map { cell -> scope.box(cell, style) } }
        val columns = cells.maxOfOrNull { it.size } ?: 0
        val widths = (0 until columns).map { column -> cells.maxOfOrNull { it.getOrNull(column)?.width ?: 0f } ?: 0f }
        val columnGap = style.fontSize * 0.55f
        val rowGap = style.fontSize * 0.3f
        val rowAscents = cells.map { row -> row.maxOfOrNull { it.ascent } ?: 0f }
        val rowDescents = cells.map { row -> row.maxOfOrNull { it.descent } ?: 0f }
        val width = widths.sum() + columnGap * max(0, columns - 1)
        val height = rowAscents.indices.sumOf { (rowAscents[it] + rowDescents[it]).toDouble() }.toFloat() + rowGap * max(0, cells.size - 1)
        val ascent = height / 2f
        val commands = mutableListOf<DrawCommand>()
        var baseline = -ascent
        cells.forEachIndexed { rowIndex, row ->
            baseline += rowAscents[rowIndex]
            var x = 0f
            row.forEachIndexed { column, cell ->
                val alignmentOffset = when {
                    node.environment != MatrixEnvironment.ALIGNED -> (widths[column] - cell.width) / 2f
                    column % 2 == 0 -> widths[column] - cell.width
                    else -> 0f
                }
                commands += cell.translated(x + alignmentOffset, baseline).commands
                x += widths[column] + columnGap
            }
            baseline += rowDescents[rowIndex] + rowGap
        }
        return decorate(LayoutBox(width, ascent, height - ascent, commands), node.environment, style)
    }

    private fun decorate(content: LayoutBox, environment: MatrixEnvironment, style: MathStyle): LayoutBox = when (environment) {
        MatrixEnvironment.ALIGNED -> content
        MatrixEnvironment.MATRIX -> content
        MatrixEnvironment.PMATRIX -> decorations.wrap("(", ")", content, style)
        MatrixEnvironment.BMATRIX -> decorations.wrap("[", "]", content, style)
        MatrixEnvironment.VMATRIX -> decorations.wrap("|", "|", content, style)
        MatrixEnvironment.CASES -> decorations.wrap("{", "", content, style)
    }
}
