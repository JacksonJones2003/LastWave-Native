package com.lastwave.app.ui.player

import android.app.Activity
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
 * Half folded: the cover (or the queue) on the standing half above the hinge, the controls
 * on the half lying flat below it, where the thumb is.
 */
@Composable
internal fun TabletopPlayer(viewModel: PlayerViewModel, hingeTopPx: Float, onClose: () -> Unit) {
    val state by viewModel.fullPlayerState.collectAsStateWithLifecycle()
    val track = state.current ?: return
    val player = viewModel.player
    var showQueue by rememberSaveable { mutableStateOf(false) }
    BackHandler { if (showQueue) showQueue = false else onClose() }
    val skip = { deltaMs: Long ->
        val p = viewModel.progressState.value
        player.seekTo((p.positionMs + deltaMs).coerceIn(0L, p.durationMs.coerceAtLeast(0L)))
    }
    CompositionLocalProvider(LocalIsDarkTheme provides true) {
        Column(Modifier.fillMaxSize().background(Color.Black)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(with(LocalDensity.current) { hingeTopPx.toDp() })
                    .windowInsetsPadding(WindowInsets.statusBars),
                contentAlignment = Alignment.Center,
            ) {
                if (showQueue) {
                    QueuePanel(state, player, modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp))
                } else {
                    PlayerArtwork(
                        track = track,
                        modifier = Modifier.padding(vertical = 20.dp).fillMaxHeight().aspectRatio(1f),
                        corner = 16.dp,
                    )
                }
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
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
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(onClick = { showQueue = !showQueue }) {
                        Icon(
                            Icons.AutoMirrored.Filled.QueueMusic,
                            if (showQueue) "Show cover" else "Show queue",
                            tint = if (showQueue) MaterialTheme.colorScheme.primary else Color.White,
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, "Close", tint = Color.White)
                    }
                }
                SeekBar(
                    progressState = viewModel.progressState,
                    isPlaying = state.isPlaying,
                    trackKey = track.videoId ?: "${track.artist}|${track.title}",
                    onSeek = player::seekTo,
                    fallbackDurationMs = track.durationMs ?: 0L,
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TabletopButton(Icons.Filled.SkipPrevious, "Previous", player::previous)
                    TabletopButton(Icons.Filled.Replay10, "Back 10 seconds") { skip(-10_000L) }
                    Surface(
                        onClick = player::togglePlayPause,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.12f),
                        contentColor = Color.White,
                        modifier = Modifier.size(72.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AnimatedPlayPauseIcon(state.isPlaying, Modifier.size(36.dp))
                        }
                    }
                    TabletopButton(Icons.Filled.Forward10, "Forward 10 seconds") { skip(10_000L) }
                    TabletopButton(Icons.Filled.SkipNext, "Next", player::next)
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun TabletopButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = hapticClick(action = onClick), modifier = Modifier.size(56.dp)) {
        Icon(icon, description, tint = Color.White, modifier = Modifier.size(32.dp))
    }
}
