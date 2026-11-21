package com.lastwave.app.ui.player

import android.app.Activity
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.lastwave.app.ui.theme.LocalIsDarkTheme

/**
 * The hinge's top edge in window pixels while the screen is half folded across its width
 * (tabletop: the top half stands up like a little display), else null.
 */
@Composable
internal fun rememberTabletopHingeTop(): Float? {
    val activity = generateSequence(LocalContext.current) { (it as? ContextWrapper)?.baseContext }
        .filterIsInstance<Activity>().firstOrNull() ?: return null
    val layout by remember(activity) { WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity) }
        .collectAsStateWithLifecycle(initialValue = null)
    return layout?.displayFeatures?.filterIsInstance<FoldingFeature>()
        ?.firstOrNull { it.state == FoldingFeature.State.HALF_OPENED && it.orientation == FoldingFeature.Orientation.HORIZONTAL }
        ?.bounds?.top?.toFloat()
}

/**
 * Half folded: the standing half is the unfolded player in miniature (cover on the start
 * side, the queue on the end side), the flat half holds the controls where the thumb is.
 * Same artwork backdrop and controls as the full player, so it reads as that player folded.
 */
@Composable
internal fun TabletopPlayer(viewModel: PlayerViewModel, hingeTopPx: Float, onClose: () -> Unit) {
    val state by viewModel.fullPlayerState.collectAsStateWithLifecycle()
    val track = state.current ?: return
    val player = viewModel.player
    BackHandler(onBack = onClose)
    val tint = rememberArtworkTint(track.artworkUrl)
    val backdrop = Brush.verticalGradient(
        listOf(androidx.compose.ui.graphics.lerp(tint, Color.White, 0.12f), tint, androidx.compose.ui.graphics.lerp(tint, Color.Black, 0.2f)),
    )
    CompositionLocalProvider(LocalIsDarkTheme provides true) {
        Column(Modifier.fillMaxSize().background(backdrop)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(with(LocalDensity.current) { hingeTopPx.toDp() })
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 24.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // The cover with the song under it on the start half; the queue takes the end half.
                Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.weight(1f).aspectRatio(1f)) {
                        PlayerArtwork(track = track, modifier = Modifier.fillMaxSize(), corner = 16.dp)
                        Box(Modifier.align(Alignment.TopStart).padding(8.dp)) {
                            PlayerHeaderButton(Icons.Filled.Close, "Close", isThemeLight = false, onClick = onClose)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        track.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        track.artist,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                QueuePanel(state, player, modifier = Modifier.weight(1f).fillMaxHeight())
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 32.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                SeekBar(
                    progressState = viewModel.progressState,
                    isPlaying = state.isPlaying,
                    trackKey = track.videoId ?: "${track.artist}|${track.title}",
                    onSeek = player::seekTo,
                    isTranslucent = true,
                    fallbackDurationMs = track.durationMs ?: 0L,
                )
                MainControls(state, player, isTranslucent = true)
                PlayerUtilityControls(state, player, isTranslucent = true)
            }
        }
    }
}
