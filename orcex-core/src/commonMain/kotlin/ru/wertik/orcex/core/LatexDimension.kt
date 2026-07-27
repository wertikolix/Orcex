package ru.wertik.orcex.core

/**
 * Explicit lengths written in a formula, normalised to em so the layout stays
 * resolution independent.
 *
 * Only the units that appear in formulas are supported; anything else is rejected and
 * treated like an unsupported command (dropped, or fatal for a validating parser).
 */
internal object LatexDimension {
    /** 1 em is 18 mu, the unit `\mkern` and `\mskip` are written in. */
    private const val MU_PER_EM = 18f

    /** TeX points per inch, with 1 em taken as the classic 10 pt body size. */
    private const val PT_PER_EM = 10f
    private const val PT_PER_INCH = 72.27f
    private const val PT_PER_CM = PT_PER_INCH / 2.54f

    fun parseEm(value: String): Float? {
        val text = value.trim().removePrefix("+")
        val unit = UNITS.firstOrNull { unit -> text.endsWith(unit.suffix, ignoreCase = true) } ?: return null
        val amount = text.dropLast(unit.suffix.length).trim().toFloatOrNull() ?: return null
        return amount * unit.em
    }

    private class Unit(val suffix: String, val em: Float)

    private val UNITS = listOf(
        Unit("em", 1f),
        Unit("ex", 0.45f),
        Unit("mu", 1f / MU_PER_EM),
        Unit("pt", 1f / PT_PER_EM),
        Unit("pc", 12f / PT_PER_EM),
        Unit("cm", PT_PER_CM / PT_PER_EM),
        Unit("mm", PT_PER_CM / PT_PER_EM / 10f),
        Unit("in", PT_PER_INCH / PT_PER_EM),
    )
}
