package com.lastwave.app.ui.player

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
        val split = PlayerSplit.of(windowWidthDp = 760, windowHeightDp = 822)!!
        assertEquals(380.dp, split.sideWidth)
        assertEquals(380, split.appWidthDp)
        assertEquals(380, split.paneWidthDp)
    }

    @Test
    fun endColumnStaysBetweenPhoneWidths() {
        assertEquals(360.dp, PlayerSplit.of(windowWidthDp = 673, windowHeightDp = 841)!!.sideWidth)
        assertEquals(480.dp, PlayerSplit.of(windowWidthDp = 1280, windowHeightDp = 800)!!.sideWidth)
    }

    @Test
    fun dockedPlayerIsACardBelowTheStatusBar() {
        // 1.5dp margins = 3px; a 50px status bar.
        assertEquals(Rect(783f, 53f, 1500f, 1660f), density.sidePaneRect(window, 360.dp, 50f, LayoutDirection.Ltr))
    }

    @Test
    fun rightToLeftDocksOnTheLeft() {
        assertEquals(Rect(0f, 53f, 717f, 1660f), density.sidePaneRect(window, 360.dp, 50f, LayoutDirection.Rtl))
    }

    private val density = Density(2f)
    private val window = Size(1500f, 1660f)
}
