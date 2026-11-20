package com.lastwave.app.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lastwave.app.ui.theme.rememberFittedDensity

/**
 * Standard Material 3 window size classes based on viewport width breakpoints.
 * - COMPACT: Standard phones in portrait (< 600dp)
 * - MEDIUM: Foldables unfolded, 7-8" tablets, portrait 10" tablets (600dp..839dp)
 * - EXPANDED: Large tablets (10"+), landscape tablets, desktop/Chromebook (>= 840dp)
 */
enum class WindowSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
}

@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    return when {
        screenWidth < 600 -> WindowSizeClass.COMPACT
        screenWidth < 840 -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}

/** Returns true if the current display is a tablet, foldable, or wide viewport (>= 600dp). */
@Composable
fun isTabletOrWideScreen(): Boolean =
    LocalConfiguration.current.screenWidthDp >= 600

/** Returns true if the current display is an expanded display (>= 840dp). */
@Composable
fun isExpandedScreen(): Boolean =
    LocalConfiguration.current.screenWidthDp >= 840

/**
 * Lays [content] out as a window of [widthDp] x [heightDp], for a pane sharing the real window
 * (the app beside a docked player). Breakpoints, orientation and the 360dp density fit then
 * follow the pane: a tablet layout squeezed into half a foldable overflowed its fixed-size boxes.
 *
 * Always call it, even at the full window size: switching between a wrapped and a bare
 * [content] would rebuild everything inside it, navigation state included.
 */
@Composable
fun WindowPane(widthDp: Int, heightDp: Int, content: @Composable () -> Unit) {
    val window = LocalConfiguration.current
    val pane = remember(window, widthDp, heightDp) {
        if (window.screenWidthDp == widthDp && window.screenHeightDp == heightDp) {
            window
        } else {
            Configuration(window).apply {
                screenWidthDp = widthDp
                screenHeightDp = heightDp
                smallestScreenWidthDp = minOf(widthDp, heightDp)
                orientation = if (widthDp > heightDp) {
                    Configuration.ORIENTATION_LANDSCAPE
                } else {
                    Configuration.ORIENTATION_PORTRAIT
                }
            }
        }
    }
    CompositionLocalProvider(LocalConfiguration provides pane) {
        CompositionLocalProvider(LocalDensity provides rememberFittedDensity(), content = content)
    }
}

@Composable
fun Modifier.adaptiveContentWidth(maxWidth: Dp = 840.dp): Modifier {
    return if (isTabletOrWideScreen()) {
        this.widthIn(max = maxWidth).fillMaxWidth()
    } else {
        this
    }
}
