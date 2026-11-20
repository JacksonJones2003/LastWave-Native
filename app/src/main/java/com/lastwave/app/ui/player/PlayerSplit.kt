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

/** Docked, the app and the player are rounded cards: [SidePaneMargin] from the window edges, twice it apart. */
internal val SidePaneMargin = 3.dp
internal val SidePaneCorner = 24.dp

/** The docked player in [size]: the end column below the status bar ([topInset] px), down past the bottom edge. */
internal fun Density.sidePaneRect(size: Size, sideWidth: Dp, topInset: Float, direction: LayoutDirection): Rect {
    val margin = SidePaneMargin.toPx()
    val width = sideWidth.toPx()
    val top = topInset + margin
    return if (direction == LayoutDirection.Ltr) {
        Rect(size.width - width + margin, top, size.width - margin, size.height)
    } else {
        Rect(margin, top, width - margin, size.height)
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
internal fun Modifier.sidePaneBounds(sideWidth: Dp, topInset: Density.() -> Float): Modifier =
    layout { measurable, constraints ->
        val window = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        val card = sidePaneRect(window, sideWidth, topInset(), layoutDirection)
        val placeable = measurable.measure(
            Constraints.fixed(card.width.roundToInt(), card.height.roundToInt()),
        )
        layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(card.left.roundToInt(), 0) }
    }
