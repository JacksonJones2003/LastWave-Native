package com.lastwave.app.ui.player

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlayerSplitTest {
    @Test
    fun phonesAndLandscapePhonesKeepTheFullScreenPlayer() {
        assertNull(PlayerSplit.of(windowWidthDp = 411, windowHeightDp = 891))
        assertNull(PlayerSplit.of(windowWidthDp = 891, windowHeightDp = 411))
        assertNull(PlayerSplit.of(windowWidthDp = 600, windowHeightDp = 960))
    }

    @Test
    fun unfoldedFoldableDocksTheEndHalf() {
        val split = PlayerSplit.of(windowWidthDp = 750, windowHeightDp = 830)!!
        assertEquals(360.dp, split.sideWidth)
        assertEquals(390, split.appWidthDp)
        assertEquals(344, split.paneWidthDp)
    }

    @Test
    fun endColumnStaysBetweenPhoneWidths() {
        assertEquals(340.dp, PlayerSplit.of(windowWidthDp = 673, windowHeightDp = 841)!!.sideWidth)
        assertEquals(440.dp, PlayerSplit.of(windowWidthDp = 1280, windowHeightDp = 800)!!.sideWidth)
    }

    @Test
    fun dockedCardSitsBelowTheStatusBarOneMarginFromTheAppColumn() {
        val card = density.sidePaneRect(window, 360.dp, WindowInsets(0, 50, 0, 48), LayoutDirection.Ltr)
        // App column ends at 1500 - 720 = 780; margins are 8dp = 16px. The card runs
        // under the gesture bar: the player pads its own controls above it.
        assertEquals(Rect(796f, 66f, 1484f, 1644f), card)
    }

    @Test
    fun rightToLeftDocksOnTheLeft() {
        val card = density.sidePaneRect(window, 360.dp, WindowInsets(0, 50, 0, 48), LayoutDirection.Rtl)
        assertEquals(Rect(16f, 66f, 704f, 1644f), card)
    }

    @Test
    fun endInsetNarrowsTheCardWithoutMovingItsAppSide() {
        val card = density.sidePaneRect(window, 360.dp, WindowInsets(0, 50, 60, 0), LayoutDirection.Ltr)
        assertEquals(Rect(796f, 66f, 1424f, 1644f), card)
    }

    private val density = Density(2f)
    private val window = Size(1500f, 1660f)
}
