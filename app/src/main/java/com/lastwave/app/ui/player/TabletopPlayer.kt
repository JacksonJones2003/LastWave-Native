package com.lastwave.app.ui.player

import android.app.Activity
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.MusicNote
import com.lastwave.app.ui.common.ExpressiveMotion
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import com.lastwave.app.ui.theme.rememberLayerBackdrop
import com.lastwave.app.ui.theme.liquidGlassSource
import com.lastwave.app.ui.theme.isLiquidGlassBackdropSupported
import com.lastwave.app.ui.theme.LocalLiquidGlassOverlayBackdrop
import com.lastwave.app.ui.theme.LocalLiquidGlassBackdrop
import com.lastwave.app.ui.theme.LocalLiquidGlass
import androidx.compose.foundation.layout.widthIn

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
    // Its glass controls refract this backdrop, as in the full player, not the app hidden behind it.
    val glass = LocalLiquidGlass.current && isLiquidGlassBackdropSupported()
    val glassSource = rememberLayerBackdrop()
    // Swiping down anywhere drags it away like the full player: it follows the finger and fades
    // a little, slides off past the threshold, and springs back short of it.
    val closeDistance = with(LocalDensity.current) { 88.dp.toPx() }
    val dragY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = dragY.value
                alpha = (1f - dragY.value / (size.height * 1.5f)).coerceIn(0.72f, 1f)
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (dragY.value > closeDistance) {
                                dragY.animateTo(size.height.toFloat(), PlayerSheetCloseSpec)
                                onClose()
                            } else {
                                dragY.animateTo(0f, ExpressiveMotion.spatialSpring())
                            }
                        }
                    },
                    onDragCancel = { scope.launch { dragY.animateTo(0f, ExpressiveMotion.spatialSpring()) } },
                ) { change, dy ->
                    change.consume()
                    scope.launch { dragY.snapTo((dragY.value + dy).coerceAtLeast(0f)) }
                }
            },
    ) {
    Box(Modifier.matchParentSize().liquidGlassSource(if (glass) glassSource else null).background(backdrop))
    CompositionLocalProvider(
        LocalIsDarkTheme provides true,
        LocalLiquidGlassBackdrop provides if (glass) glassSource else null,
        LocalLiquidGlassOverlayBackdrop provides if (glass) glassSource else null,
    ) {
        Column(Modifier.fillMaxSize()) {
            // The standing half, down to the hinge.
            Column(Modifier.fillMaxWidth().height(with(LocalDensity.current) { hingeTopPx.toDp() })) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(width = 36.dp, height = 4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.5f)))
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 24.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // The cover with the song under it on the start half; the queue takes the end half.
                Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    PlayerArtwork(track = track, modifier = Modifier.weight(1f).aspectRatio(1f), corner = 16.dp)
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
            }
            Column(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    // Phone-width controls, like the full player's, not stretched across the fold.
                    .widthIn(max = 560.dp)
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
}

/** Shown while half folded after the tabletop player was swiped away. */
@Composable
internal fun TabletopReopenButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.Surface(
        onClick = hapticClick(action = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 6.dp,
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars).padding(top = 8.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            androidx.compose.material3.Icon(
                androidx.compose.material.icons.Icons.Filled.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text("Tabletop player", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
