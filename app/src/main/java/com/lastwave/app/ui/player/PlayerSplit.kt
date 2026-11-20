package com.lastwave.app.ui.player

import androidx.compose.foundation.layout.WindowInsets
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
 * Wide windows (an unfolded foldable, a tablet) dock Now Playing as a card on the end side and
 * keep the app usable beside it. [sideWidth] is that end column: the docked card with its
 * margins, and while the player is closed the mini player's slot beside the nav bar.
 */
@Immutable
internal class PlayerSplit private constructor(val sideWidth: Dp, windowWidthDp: Int) {
    /** The app column's width while the player is docked. */
    val appWidthDp = windowWidthDp - sideWidth.value.roundToInt()

    /** The docked card's width, give or take the end inset. */
    val paneWidthDp = (sideWidth - SidePaneMargin * 2).value.roundToInt()

    companion object {
        /**
         * Null where two columns don't fit: phones, and landscape phones, whose ~400dp
         * height would squash the docked player. The end column takes about half the window,
         * kept between a narrow and a large phone's width so the player keeps its phone layout.
         */
        fun of(windowWidthDp: Int, windowHeightDp: Int): PlayerSplit? {
            if (windowWidthDp < 640 || windowHeightDp < 480) return null
            return PlayerSplit((windowWidthDp * 48 / 100).dp.coerceIn(340.dp, 440.dp), windowWidthDp)
        }
    }
}

internal val SidePaneMargin = 8.dp
internal val SidePaneCorner = 24.dp

/** The docked card in [size]: below the status bar, [SidePaneMargin] clear of the app and the window edges. */
internal fun Density.sidePaneRect(
    size: Size,
    sideWidth: Dp,
    insets: WindowInsets,
    direction: LayoutDirection,
): Rect {
    val margin = SidePaneMargin.toPx()
    val ltr = direction == LayoutDirection.Ltr
    val endInset = (if (ltr) insets.getRight(this, direction) else insets.getLeft(this, direction)).toFloat()
    val width = sideWidth.toPx() - endInset - margin * 2
    val left = if (ltr) size.width - endInset - margin - width else endInset + margin
    return Rect(left, insets.getTop(this) + margin, left + width, size.height - margin)
}

/** Narrows the content to the start column, giving up [docked] (0..1) of [sideWidth]. */
internal fun Modifier.appColumn(sideWidth: Dp, docked: () -> Float): Modifier =
    layout { measurable, constraints ->
        val width = (constraints.maxWidth - sideWidth.toPx() * docked()).roundToInt()
            .coerceIn(0, constraints.maxWidth)
        val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
        layout(constraints.maxWidth, constraints.maxHeight) { placeable.placeRelative(0, 0) }
    }

/** Lays the player out at the docked card; its host layer stays window-sized for the open/close morph. */
internal fun Modifier.sidePaneBounds(sideWidth: Dp, insets: WindowInsets): Modifier =
    layout { measurable, constraints ->
        val window = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        val card = sidePaneRect(window, sideWidth, insets, layoutDirection)
        val placeable = measurable.measure(
            Constraints.fixed(card.width.roundToInt(), card.height.roundToInt()),
        )
        // The host layer is already moved down to the card's top edge.
        layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(card.left.roundToInt(), 0) }
    }
