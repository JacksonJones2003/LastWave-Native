package com.lastwave.app.ui.player

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Wide windows (an unfolded foldable, a tablet) dock Now Playing on the end side and keep the
 * app usable beside it. [sideWidth] is that end column: the docked player edge to edge, and
 * while the player is closed the mini player's slot beside the nav bar.
 */
@Immutable
internal class PlayerSplit private constructor(val sideWidth: Dp, windowWidthDp: Int) {
    /** The app column's width while the player is docked. */
    val appWidthDp = windowWidthDp - sideWidth.value.roundToInt()

    /** The docked player's width. */
    val paneWidthDp = sideWidth.value.roundToInt()

    companion object {
        /**
         * Null where two columns don't fit: phones, and landscape phones, whose ~400dp
         * height would squash the docked player. The end column takes half the window,
         * kept between a phone's width and a large phone's so the player keeps its phone layout.
         */
        fun of(windowWidthDp: Int, windowHeightDp: Int): PlayerSplit? {
            if (windowWidthDp < 640 || windowHeightDp < 480) return null
            return PlayerSplit((windowWidthDp / 2).dp.coerceIn(360.dp, 480.dp), windowWidthDp)
        }
    }
}

/** Rounds the docked player's corners where they meet the app. */
internal val SidePaneCorner = 20.dp

/** The docked player in [size]: the whole end column, top to bottom. */
internal fun Density.sidePaneRect(size: Size, sideWidth: Dp, direction: LayoutDirection): Rect {
    val width = sideWidth.toPx()
    return if (direction == LayoutDirection.Ltr) {
        Rect(size.width - width, 0f, size.width, size.height)
    } else {
        Rect(0f, 0f, width, size.height)
    }
}

/** Narrows the content to the start column, giving up [docked] (0..1) of [sideWidth]. */
internal fun Modifier.appColumn(sideWidth: Dp, docked: () -> Float): Modifier =
    layout { measurable, constraints ->
        val width = (constraints.maxWidth - sideWidth.toPx() * docked()).roundToInt()
            .coerceIn(0, constraints.maxWidth)
        val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
        layout(constraints.maxWidth, constraints.maxHeight) { placeable.placeRelative(0, 0) }
    }

/** Lays the player out in the end column; its host layer stays window-sized for the open/close morph. */
internal fun Modifier.sidePaneBounds(sideWidth: Dp): Modifier =
    layout { measurable, constraints ->
        val window = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        val card = sidePaneRect(window, sideWidth, layoutDirection)
        val placeable = measurable.measure(
            Constraints.fixed(card.width.roundToInt(), card.height.roundToInt()),
        )
        layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(card.left.roundToInt(), 0) }
    }
