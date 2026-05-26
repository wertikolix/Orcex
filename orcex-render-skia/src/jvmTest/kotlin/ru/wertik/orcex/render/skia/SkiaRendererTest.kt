package ru.wertik.orcex.render.skia

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.ceil
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Surface
import org.jetbrains.skia.Typeface
import ru.wertik.orcex.core.LatexModule
import ru.wertik.orcex.core.LatexParseException
import ru.wertik.orcex.core.ParserConfig
import ru.wertik.orcex.layout.DrawCommand
import ru.wertik.orcex.layout.MathLayout
import ru.wertik.orcex.layout.MathLayoutConstraints
import ru.wertik.orcex.layout.MathStyle

class SkiaRendererTest {
    private val typeface: Typeface = requireNotNull(FontMgr.default.legacyMakeTypeface("serif", FontStyle.NORMAL))

    @Test
    fun generatesGalleryWithBundledStixFontThroughPublicRenderer() {
        val fontPath = listOf(
            Paths.get("orcex-font-stix2-android/src/main/assets/orcex/fonts/STIXTwoMath-Regular.ttf"),
            Paths.get("../orcex-font-stix2-android/src/main/assets/orcex/fonts/STIXTwoMath-Regular.ttf"),
        ).first { Files.isRegularFile(it) }
        val stix = requireNotNull(FontMgr.default.makeFromFile(fontPath.toString()))
        val engine = SkiaLatexEngine(stix)
        val formulas = listOf(
            "\\int_0^1 x^2 \\, dx = \\frac{1}{3}" to null,
            "\\int \\frac{1}{\\sqrt{1-x^2}} \\, dx = \\arcsin(x) + C" to null,
            "\\int_{-\\infty}^{\\infty} e^{-x^2} \\, dx = \\sqrt{\\pi}" to null,
            "\\left.\\frac{x^2}{2}\\right|_0^1 = \\frac{1}{2}" to null,
            "\\begin{aligned}\\nabla \\cdot \\mathbf{E} &= \\frac{\\rho}{\\varepsilon_0} \\\\ \\nabla \\cdot \\mathbf{B} &= 0 \\\\ \\nabla \\times \\mathbf{E} &= -\\frac{\\partial \\mathbf{B}}{\\partial t} \\\\ \\nabla \\times \\mathbf{B} &= \\mu_0 \\mathbf{J} + \\mu_0\\,\\varepsilon_0 \\frac{\\partial \\mathbf{E}}{\\partial t}\\end{aligned}" to null,
            "e^x = 1 + x + \\frac{x^2}{2} + \\frac{x^3}{6} + \\frac{x^4}{24} + \\frac{x^5}{120} + \\cdots" to 500f,
        )
        val layouts = formulas.map { (formula, width) ->
            if (width == null) engine.layout(formula, 40f) else engine.layout(formula, 40f, MathLayoutConstraints(width))
        }
        val margin = 44f
        val gap = 32f
        val label = 20f
        val width = ceil(layouts.maxOf { it.width } + margin * 2).toInt()
        val height = ceil(margin + layouts.sumOf { (label + it.height + gap).toDouble() }).toInt()
        val surface = Surface.makeRasterN32Premul(width, height)
        surface.canvas.clear(0xFF111318.toInt())
        val renderer = SkiaMathRenderer(stix, 0xFFF2F3F7.toInt())
        var top = margin
        layouts.forEach { layout ->
            renderer.draw(surface.canvas, layout, margin, top + label)
            top += label + layout.height + gap
        }
        val bytes = requireNotNull(surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG, 100)).bytes
        val output = Path.of("build", "skia-test-snapshots", "formula-preview.png")
        Files.createDirectories(output.parent)
        Files.write(output, bytes)
        val golden = requireNotNull(javaClass.getResourceAsStream("/golden/formula-preview.png")).use { it.readBytes() }
        assertContentEquals(golden, bytes)
        assertTrue(bytes.size > 20_000)
        assertTrue(width > 500 && height > 300)
    }

    @Test
    fun drawsTextAndLineCommandsIntoPngSnapshot() {
        val layout = MathLayout(
            width = 120f,
            height = 50f,
            baseline = 35f,
            commands = listOf(
                DrawCommand.Text("x", 8f, 35f, MathStyle(fontSize = 34f)),
                DrawCommand.Line(8f, 43f, 108f, 43f, 3f),
            ),
        )
        val rendered = renderPng("text-line") { canvas ->
            SkiaMathRenderer(typeface, Color.BLUE).draw(canvas, layout, 0f, 0f)
        }
        val blank = renderPng(null) { }

        assertContentEquals(PNG_SIGNATURE, rendered.copyOfRange(0, PNG_SIGNATURE.size))
        assertFalse(blank.contentEquals(rendered))
    }

    @Test
    fun engineWrapsConstrainedLayoutsAndRendersRules() {
        val engine = SkiaLatexEngine(typeface)
        val source = "a+b+c+d+e+f=g+h+i+j"
        val oneLine = engine.layout(source, fontSize = 30f)
        val constrained = engine.layout(source, fontSize = 30f, MathLayoutConstraints(maxWidth = oneLine.width * 0.55f))
        val fraction = engine.layout("\\frac{x+1}{2}", fontSize = 36f)

        assertTrue(constrained.height > oneLine.height)
        assertTrue(constrained.width < oneLine.width)
        assertTrue(fraction.commands.any { it is DrawCommand.Line })
        val rendered = renderPng("fraction") { canvas ->
            SkiaMathRenderer(typeface, Color.BLACK).draw(canvas, fraction, 12f, 12f)
        }
        assertTrue(rendered.size > PNG_SIGNATURE.size)
    }

    @Test
    fun engineAppliesParserConfiguration() {
        val scriptsOnly = SkiaLatexEngine(
            typeface,
            ParserConfig(enabledModules = setOf(LatexModule.SCRIPTS)),
        )

        assertFailsWith<LatexParseException> { scriptsOnly.layout("\\frac{1}{2}", fontSize = 24f) }
    }

    private fun renderPng(name: String?, draw: (org.jetbrains.skia.Canvas) -> Unit): ByteArray {
        val surface = Surface.makeRasterN32Premul(256, 112)
        surface.canvas.clear(Color.WHITE)
        draw(surface.canvas)
        val png = requireNotNull(surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG, 100)).bytes
        name?.let { snapshotName ->
            val directory = Path.of("build", "skia-test-snapshots")
            Files.createDirectories(directory)
            Files.write(directory.resolve("$snapshotName.png"), png)
        }
        return png
    }

    private companion object {
        val PNG_SIGNATURE: ByteArray = byteArrayOf(
            0x89.toByte(),
            0x50,
            0x4E,
            0x47,
            0x0D,
            0x0A,
            0x1A,
            0x0A,
        )
    }
}
