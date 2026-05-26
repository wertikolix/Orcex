package ru.wertik.orcex.render.compose

import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeGeometryTest {
    @Test
    fun convertsBaselinePositionToComposeTopPosition() {
        assertEquals(25f, composeTextTop(commandBaseline = 40f, textBaseline = 15f))
    }
}
