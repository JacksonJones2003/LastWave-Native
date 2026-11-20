@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.lastwave.app.ui.player

import com.lastwave.app.data.local.SeekbarStyle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import com.lastwave.app.data.playlist.isLikedIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.combinedClickable
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.lastwave.app.ui.common.PredictiveBackScreen
import com.lastwave.app.playback.haptics.MusicHapticsConfig
import com.lastwave.app.playback.haptics.MusicHapticsEngineMode
import com.lastwave.app.playback.haptics.MusicHapticsIntensity
import com.lastwave.app.playback.haptics.MusicHapticsMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.spring
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.util.lerp
import com.lastwave.app.ui.common.WindowPane
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.statusBars
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import com.lastwave.app.ui.common.adaptiveContentWidth
import com.lastwave.app.ui.common.isTabletOrWideScreen
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.zIndex
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import androidx.media3.common.Player
import com.lastwave.app.data.generate.GeneratedTrack
import com.lastwave.app.data.lyrics.LyricsRepository
import com.lastwave.app.data.lyrics.LyricsResult
import com.lastwave.app.data.lyrics.LyricsSyncType
import kotlinx.coroutines.flow.update
import com.lastwave.app.data.playlist.PlaylistRepository
import com.lastwave.app.data.playlist.SavedPlaylist
import com.lastwave.app.data.playlist.LIKED_SONGS_MODE
import com.lastwave.app.playback.MusicPlayer
import com.lastwave.app.playback.MusicPlayerState
import com.lastwave.app.playback.PlaybackChromeState
import com.lastwave.app.playback.formatSampleRateKHz
import com.lastwave.app.playback.withoutProgress
import com.lastwave.app.playback.isSpatialAudioCodec
import com.lastwave.app.playback.qualityBadgeLabel
import com.lastwave.app.playback.SignalPathReport
import com.lastwave.app.playback.PlaybackProgressState
import androidx.hilt.navigation.compose.hiltViewModel
import com.lastwave.app.data.artwork.ArtworkNormalizer
import com.lastwave.app.playback.PlayableTrack
import com.lastwave.app.playback.toGeneratedTrack
import com.lastwave.app.playback.resolve.MetadataLog
import com.lastwave.app.ui.common.ArtworkImage
import com.lastwave.app.ui.common.ArtworkViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import androidx.compose.material.icons.filled.Timer
import com.lastwave.app.ui.common.ExpressiveInlineLoadingIndicator
import com.lastwave.app.ui.common.ExpressiveMotion
import com.lastwave.app.ui.common.PlaylistCover
import com.lastwave.app.ui.common.TrackContextMenuSheet
import com.lastwave.app.ui.common.TrackMenuCapabilities
import com.lastwave.app.ui.common.TrackMenuTarget
import com.lastwave.app.ui.common.GroupGap
import com.lastwave.app.ui.common.TopCurvedSectionClipShape
import com.lastwave.app.ui.common.groupPositionFor
import com.lastwave.app.ui.common.groupShape
import com.lastwave.app.ui.theme.LocalLiquidGlass
import com.lastwave.app.ui.theme.LocalMd3NavBar
import com.lastwave.app.ui.theme.LocalIsDarkTheme
import com.lastwave.app.ui.theme.LiquidGlassSurface
import com.lastwave.app.ui.theme.liquidGlass
import com.lastwave.app.ui.theme.liquidGlassChrome
import com.lastwave.app.ui.theme.liquidGlassContainerColor
import com.lastwave.app.ui.theme.liquidGlassSource
import com.lastwave.app.ui.theme.isLiquidGlassBackdropSupported
import com.lastwave.app.ui.theme.LocalLiquidGlassBackdrop
import com.lastwave.app.ui.theme.LocalLiquidGlassOverlayBackdrop
import com.lastwave.app.ui.theme.LiquidGlassPreset
import com.lastwave.app.ui.theme.Backdrop
import com.lastwave.app.ui.theme.rememberLayerBackdrop
import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.scale
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.nio.IntBuffer
import kotlin.time.Duration.Companion.seconds
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

enum class FullPlayerTab {
    NOW_PLAYING,
    LYRICS,
    QUEUE
}

val LocalMusicPlayer = staticCompositionLocalOf<MusicPlayer> {
    error("MusicPlayer is only available inside PlayerHost")
}

val LocalAddToPlaylist = staticCompositionLocalOf<(PlayableTrack) -> Unit> {
    error("Add-to-playlist is only available inside PlayerHost")
}

/**
 * Extra list-end clearance needed while the collapsed player overlays a
 * screen. The Material 3 bar is about 86dp tall; 88dp clears it without
 * leaving a large empty band at the end of short lists.
 */
val LocalMiniPlayerScrollClearance = staticCompositionLocalOf { 0.dp }
val LocalMiniPlayerSnackbarClearance = staticCompositionLocalOf { 0.dp }


val LocalBottomNavSlot = androidx.compose.runtime.staticCompositionLocalOf<androidx.compose.runtime.MutableState<BottomNavigationContent>> {
    error("LocalBottomNavSlot not provided")
}

/** App-wide collapsed + maximized player layered over every navigation route. */
@Composable
fun PlayerHost(
    viewModel: PlayerViewModel = hiltViewModel(),
    hasBottomNavigation: Boolean = false,
    material3NavigationEnabled: Boolean = false,
    hideMiniPlayer: Boolean = false,
    /**
     * A screen built around a WebView (Spotify / YouTube sign-in). The mini player is
     * hidden and the liquid-glass backdrop stops capturing the page: re-recording a
     * WebView into an offscreen layer every frame (the mini player animates while
     * music plays) makes the page stutter and jitter on some GPUs/ROMs.
     */
    webContentScreen: Boolean = false,
    content: @Composable () -> Unit,
) {
    @Suppress("NAME_SHADOWING")
    val hideMiniPlayer = hideMiniPlayer || webContentScreen
    val state by viewModel.chromeState.collectAsStateWithLifecycle()
    val playbackState by viewModel.player.stateWithoutProgress.collectAsStateWithLifecycle()
    val activeDownloads by viewModel.activeDownloads.collectAsStateWithLifecycle()
    var expanded by rememberSaveable { mutableStateOf(false) }
    val window = LocalConfiguration.current
    val split = remember(window.screenWidthDp, window.screenHeightDp) {
        PlayerSplit.of(window.screenWidthDp, window.screenHeightDp)
    }
    // Wide windows only: the open player fills the window instead of docking beside the app.
    var playerFullscreen by rememberSaveable { mutableStateOf(false) }
    val dockedSplit = split?.takeIf { !playerFullscreen }
    var currentTab by rememberSaveable { mutableStateOf(FullPlayerTab.NOW_PLAYING) }
    // Lyrics shown inline on Now Playing. Kept beside currentTab, not as a tab,
    // so the regular lyrics tab returns to it on back.
    var playerLyrics by rememberSaveable { mutableStateOf(false) }
    // Only line-synced lyrics need the 60 ms playhead; on Now Playing each
    // tick redrew the whole full player to move the seek bar under a pixel.
    val lyricsOpen = expanded &&
        (currentTab == FullPlayerTab.LYRICS || (currentTab == FullPlayerTab.NOW_PLAYING && playerLyrics))
    DisposableEffect(lyricsOpen) {
        com.lastwave.app.playback.PlaybackUiDemand.needsHighFrequency = lyricsOpen
        onDispose { com.lastwave.app.playback.PlaybackUiDemand.needsHighFrequency = false }
    }
    var playlistTrack by remember { mutableStateOf<PlayableTrack?>(null) }
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val keepLyricsAwake = lyricsOpen && settings.keepLyricsScreenAwake
    val currentView = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(keepLyricsAwake) {
        if (keepLyricsAwake) {
            currentView.keepScreenOn = true
        }
        onDispose {
            if (keepLyricsAwake) {
                currentView.keepScreenOn = false
            }
        }
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    val requestAddToPlaylist = remember(viewModel) {
        { track: PlayableTrack ->
            viewModel.prepareCustomPlaylists()
            playlistTrack = track
        }
    }
    val trackKey = state.current?.let { it.videoId ?: "${it.artist}|${it.title}" }
    LaunchedEffect(trackKey) {
        if (trackKey == null) {
            expanded = false
            currentTab = FullPlayerTab.NOW_PLAYING
        }
    }
    LaunchedEffect(expanded) {
        if (!expanded) {
            currentTab = FullPlayerTab.NOW_PLAYING
            playerLyrics = false
        }
    }
    // A page opened from the player: a docked player stays beside it, a full one docks so it shows.
    val showPage by rememberUpdatedState {
        if (split != null) {
            playerFullscreen = false
        } else {
            expanded = false
            currentTab = FullPlayerTab.NOW_PLAYING
        }
    }
    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { showPage() }
    }
    LaunchedEffect(Unit) {
        viewModel.mixLauncher.requests.collect { showPage() }
    }
    LaunchedEffect(Unit) {
        viewModel.genreExplorer.pendingGenre.collect { genre ->
            if (genre != null) showPage()
        }
    }
    LaunchedEffect(Unit) {
        com.lastwave.app.widget.wave.WaveWidgetNavigation.requests.collect { requestId ->
            if (requestId <= 0L) return@collect
            val hasTrack = viewModel.player.state.value.current != null ||
                withTimeoutOrNull(2_000L) {
                    viewModel.player.state.first { it.current != null }
                    true
                } == true
            if (hasTrack && com.lastwave.app.widget.wave.WaveWidgetNavigation.consume(requestId)) {
                currentTab = FullPlayerTab.NOW_PLAYING
                expanded = true
            }
        }
    }
    val miniPlayerShown = state.current != null && !settings.buttonNowPlayingEnabled && !hideMiniPlayer
    val miniPlayerVisible = miniPlayerShown && !expanded
    // Wide windows keep the mini player in the nav row's end slot, not stacked above the bar.
    val miniBesideNav = split != null && hasBottomNavigation

    // Sibling pattern: ONE backdrop capturing feed content,
    // consumed by the sibling MiniPlayer. Unconditional remember keeps composition stable.
    val miniBackdropColor = MaterialTheme.colorScheme.background
    val miniBackdrop = rememberLayerBackdrop {
        drawRect(miniBackdropColor)
        drawContent()
    }
    val miniGlass = LocalLiquidGlass.current && isLiquidGlassBackdropSupported() && !webContentScreen

    // Player presentation (open / close): driven by sheet expansion progress (0..1).
    // The player slides smoothly from the bottom over the content like a native
    // sheet with spring physics (matching PixelMusic), while the page behind
    // receives a clean dimming scrim without blur or zoom distortions.
    val playerExpansion = remember { Animatable(if (expanded && state.current != null) 1f else 0f) }
    val expansionTarget = if (expanded && state.current != null) 1f else 0f
    // The full player is heavy to compose. Mount it (invisible, expansion still 0) and let it
    // finish its first composition/measure before the spring starts, so that cost lands before
    // the animation instead of inside its first frames (the open stutter/freeze).
    var playerMounted by remember { mutableStateOf(expanded && state.current != null) }
    LaunchedEffect(expansionTarget) {
        if (expansionTarget == 1f) {
            if (!playerMounted) {
                playerMounted = true
                withFrameNanos { }
                withFrameNanos { }
            }
        }
        playerExpansion.animateTo(
            targetValue = expansionTarget,
            animationSpec = if (expansionTarget == 1f) PlayerSheetOpenSpec else PlayerSheetCloseSpec,
        )
        if (expansionTarget == 0f) playerMounted = false
    }
    // 0 = docked beside the app, 1 = filling the window (always, below the split size).
    val paneFullscreen = remember { Animatable(if (dockedSplit != null) 0f else 1f) }
    LaunchedEffect(dockedSplit != null) {
        val target = if (dockedSplit != null) 0f else 1f
        // Closed, there is no card to resize.
        if (playerExpansion.value == 0f) paneFullscreen.snapTo(target)
        else paneFullscreen.animateTo(target, ExpressiveMotion.spatialSlow())
    }
    // The player is laid out at its new size at once; it fades back in over the card's last
    // two thirds, rather than showing its old layout stretched or cropped.
    val paneSwapAlpha = {
        ((0.65f - abs(paneFullscreen.value - paneFullscreen.targetValue)) / 0.65f).coerceIn(0f, 1f)
    }
    // The app takes its column as soon as the player opens and gets the window back as soon as it
    // closes: resizing it every frame of the morph re-measured the whole app each frame.
    val appDocked = split != null && expansionTarget == 1f

    var isPlayerDragging by remember { mutableStateOf(false) }
    val playerLayerShown by remember { derivedStateOf { playerExpansion.value > 0f } }
    // How much of the window the player covers: all of it once open, none while docked.
    val coverProgress = { playerExpansion.value.coerceIn(0f, 1f) * paneFullscreen.value.coerceIn(0f, 1f) }
    val contentCovered by remember { derivedStateOf { coverProgress() >= 1f && !isPlayerDragging } }
    val showMiniPlayer by remember { derivedStateOf { playerExpansion.value < 0.45f } }
    val showBottomNav by remember { derivedStateOf { state.current == null || coverProgress() < 0.625f } }
    // Mini player rides above the growing card for the first half of the morph.
    val miniOnTop by remember { derivedStateOf { playerExpansion.value > 0f && playerExpansion.value < 0.5f } }

    // Container-transform morph: the full player grows out of the mini player card
    // (PixelMusic-style). Bounds are captured only while the player is at rest so
    // the mini player's own follow-the-card translation can't feed back into them.
    var hostOrigin by remember { mutableStateOf(Offset.Zero) }
    var miniCardBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val morphCardColor = MaterialTheme.colorScheme.surfaceContainerHigh

    val bottomNavSlot = remember { mutableStateOf<BottomNavigationContent>({ _ -> }) }
    // Off: progress stays at 0, which is the classic full bar + full mini player layout.
    // A wide window's single nav + mini player row has nothing to collapse.
    val chromeCollapse = rememberChromeCollapseState(
        enabled = hasBottomNavigation && settings.collapsingNavBar && split == null,
    )
    // While docking, the card slides down over the fading bar, not under it.
    val chromeDocking by remember { derivedStateOf { chromeCollapse.progress() > 0f } }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.toastMessage.collect { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.snackbarEvent.collect { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = androidx.compose.material3.SnackbarDuration.Short,
            )
        }
    }
    CompositionLocalProvider(
        LocalMusicPlayer provides viewModel.player,
        LocalAddToPlaylist provides requestAddToPlaylist,
        LocalMiniPlayerScrollClearance provides when {
            !miniPlayerShown -> 0.dp
            // Beside the bar the card only stands a little taller than it.
            miniBesideNav -> 16.dp
            else -> 98.dp
        },
        LocalMiniPlayerSnackbarClearance provides if (miniPlayerVisible) {
            // Keep snackbars clear of the tray: its bottom lift plus its ~80dp card.
            (if (hasBottomNavigation) {
                if (material3NavigationEnabled) 84.dp else 80.dp
            } else {
                14.dp
            }) + 80.dp
        } else {
            0.dp
        },
        LocalBottomNavSlot provides bottomNavSlot,
        LocalChromeCollapse provides chromeCollapse,
        LocalLiquidGlassBackdrop provides (if (miniGlass) miniBackdrop else null),
    ) {
        val layoutDirection = LocalLayoutDirection.current
        // Docked, the app and the player are cards; the window between them is a shade off the page.
        val gapColor = MaterialTheme.colorScheme.surfaceContainerHighest
        // Read in layout and draw only, so status bar changes never recompose the host.
        val statusBars = WindowInsets.statusBars
        Box(
            Modifier
                .fillMaxSize()
                .onGloballyPositioned { hostOrigin = it.boundsInRoot().topLeft }
                .then(
                    if (split == null || !appDocked) {
                        Modifier
                    } else {
                        Modifier.drawBehind {
                            drawRect(gapColor)
                        }
                    },
                ),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .then(
                        if (split == null || !appDocked) {
                            Modifier
                        } else {
                            // A rounded card below the status bar, clear of the window edge and the player.
                            Modifier
                                .appColumn(split.sideWidth) { 1f }
                                .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                                .padding(start = SidePaneMargin, top = SidePaneMargin, end = SidePaneMargin)
                                .clip(RoundedCornerShape(SidePaneCorner))
                        },
                    )
                    .drawWithContent {
                        // Under the opaque full player the shell is invisible, but drawing it anyway
                        // re-rendered the whole feed (and its path clip, masked on the CPU) every frame.
                        if (contentCovered) return@drawWithContent
                        drawContent()
                        val c = coverProgress()
                        if (c > 0f) {
                            drawRect(Color.Black.copy(alpha = 0.45f * c))
                        }
                    },
            ) {
                Box(Modifier.fillMaxSize().liquidGlassSource(if (miniGlass) miniBackdrop else null)) {
                    com.lastwave.app.ui.common.AmbientAnimationsGate(visible = { !contentCovered }) {
                        // The app lays out for its column while the player is docked.
                        WindowPane(
                            widthDp = split?.takeIf { appDocked }?.appWidthDp ?: window.screenWidthDp,
                            heightDp = window.screenHeightDp,
                            content = content,
                        )
                    }
                }
            }
            if (miniPlayerShown && showMiniPlayer) {
                // Wide windows: as tall as the bar beside it and level with it (MainShell's bar metrics).
                val liquidNav = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && LocalLiquidGlass.current
                val (navRowHeight, navRowLift) = when {
                    !miniBesideNav -> 64.dp to 14.dp
                    material3NavigationEnabled && !liquidNav -> 66.dp to 0.dp
                    liquidNav && !material3NavigationEnabled -> 64.dp to 8.dp
                    else -> 60.dp to 12.dp
                }
                val miniBottomPadding = when {
                    split != null -> navRowLift
                    hasBottomNavigation -> if (material3NavigationEnabled) 84.dp else 80.dp
                    else -> 14.dp
                }
                MiniPlayer(
                    state = state,
                    progressState = viewModel.miniProgressState,
                    onExpand = { expanded = true },
                    onToggle = viewModel.player::togglePlayPause,
                    onPrevious = viewModel.player::previous,
                    onNext = viewModel.player::next,
                    onClose = viewModel.player::stopAndClear,
                    bottomPadding = miniBottomPadding,
                    edgeToEdge = !hasBottomNavigation,
                    backdrop = if (miniGlass) miniBackdrop else null,
                    onCardBounds = { rootBounds ->
                        if (playerExpansion.value == 0f) {
                            miniCardBounds = rootBounds.translate(-hostOrigin)
                        }
                    },
                    compactProgress = chromeCollapse.progress,
                    rowHeight = if (split != null) navRowHeight else null,
                    modifier = Modifier
                        .then(
                            // Wide windows: the end column, where the docked player opens.
                            if (split == null) {
                                Modifier.align(Alignment.BottomCenter)
                            } else {
                                Modifier.align(Alignment.BottomEnd).width(split.sideWidth)
                            },
                        )
                        .zIndex(if (miniOnTop || chromeDocking) 1f else 0f)
                        .graphicsLayer {
                            val p = playerExpansion.value.coerceIn(0f, 1f)
                            alpha = (1f - p * 2.2f).coerceIn(0f, 1f)
                            // Follow the morphing card's top edge as it rises.
                            translationY = -(miniCardBounds?.top ?: 0f) * p
                        }
                        .dockBesidePill(miniBottomPadding, chromeCollapse.progress),
                )
            }

            // Sleep Timer Overlay Button (shown whenever timer is active, regardless of mini-player or button mode)
            if (!expanded && playbackState.sleepTimerRemainingMs != null) {
                DraggableSleepTimerOverlay(
                    sleepTimerRemainingMs = playbackState.sleepTimerRemainingMs,
                    onCycleTimer = { viewModel.player.cycleSleepTimer() },
                    onCloseTimer = { viewModel.player.setSleepTimerMinutes(0) },
                )
            }
            val currentTrack = state.current
            if (currentTrack != null && !expanded && settings.buttonNowPlayingEnabled) {
                DraggableNowPlayingButton(
                    track = currentTrack,
                    progressState = viewModel.miniProgressState,
                    isPlaying = state.isPlaying,
                    onExpand = { expanded = true },
                    onClose = viewModel.player::stopAndClear,
                )
            }
            if (activeDownloads.isNotEmpty() && !expanded) {
                val latestProgress = activeDownloads.firstOrNull()?.progressPercent ?: 0
                com.lastwave.app.ui.download.DraggableDownloadOverlay(
                    activeDownloadsCount = activeDownloads.size,
                    latestProgressPercent = latestProgress,
                    onOpenDownloads = { viewModel.navigateToDownloads() },
                )
            }
            if ((playerLayerShown || playerMounted) && state.current != null) {
                val sheetDensity = LocalDensity.current
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val p = playerExpansion.value.coerceIn(0f, 1f)
                            val f = paneFullscreen.value.coerceIn(0f, 1f)
                            val whole = Rect(0f, 0f, size.width, size.height)
                            val docked = split?.let { sidePaneRect(size, it.sideWidth, statusBars.getTop(this).toFloat(), layoutDirection) }
                            val target = if (docked == null) whole else lerp(docked, whole, f)
                            // Start rect = mini player card; fallback (button mode / no mini)
                            // = zero-height strip at the bottom, i.e. a reveal from below.
                            val start = miniCardBounds ?: Rect(target.left, size.height, target.right, size.height)
                            val card = lerp(start, target, p)
                            val startRadius = minOf(start.height / 2f, PLAYER_SHEET_CORNER_RADIUS.toPx() + 8.dp.toPx())
                            val endRadius = if (docked == null) 0f else lerp(SidePaneCorner.toPx(), 0f, f)
                            // Content is anchored to the card's top edge and clipped to the card.
                            translationY = card.top
                            // Pre-mount frames (p == 0): composed and measured, but not drawn over the mini player.
                            alpha = if (p == 0f) 0f else 1f
                            shape = PlayerMorphShape(card.left, card.right, card.height, lerp(startRadius, endRadius, p))
                            // A docked card stays clipped: rounded, and taps beside it reach the app.
                            clip = p < 1f || f < 1f
                        }
                        .drawBehind {
                            // Solid card surface visible until the full player fades in. While it
                            // resizes between docked and full, dark like the player, not a grey flash.
                            when {
                                playerExpansion.value < 1f -> drawRect(morphCardColor)
                                paneSwapAlpha() < 1f -> drawRect(Color.Black)
                            }
                        },
                ) {
                    Box(
                        Modifier
                            .then(
                                if (dockedSplit == null) {
                                    Modifier.fillMaxSize()
                                } else {
                                    // Below the status bar: the player must not pad for it again.
                                    Modifier
                                        .sidePaneBounds(dockedSplit.sideWidth) { statusBars.getTop(this).toFloat() }
                                        .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                                },
                            )
                            .graphicsLayer {
                                val p = playerExpansion.value.coerceIn(0f, 1f)
                                // Full player: invisible until 25% expanded, opaque at 100%,
                                // with a 24dp upward settle (PixelMusic FullPlayerVisualState).
                                val contentAlpha = ((p - 0.25f) / 0.75f).coerceIn(0f, 1f)
                                alpha = contentAlpha * paneSwapAlpha()
                                val settlePx = with(sheetDensity) { 24.dp.toPx() }
                                translationY = lerp(settlePx, 0f, contentAlpha)
                            },
                    ) {
                        WindowPane(
                            // Full screen on a wide window the player is one of two columns, the queue the other.
                            widthDp = when {
                                dockedSplit != null -> dockedSplit.paneWidthDp
                                split != null -> window.screenWidthDp / 2
                                else -> window.screenWidthDp
                            },
                            heightDp = window.screenHeightDp,
                        ) {
                            PredictiveBackScreen(
                                // Docked, back belongs to the app's pages beside the player.
                                enabled = expanded && state.current != null &&
                                    currentTab == FullPlayerTab.NOW_PLAYING && dockedSplit == null,
                                // The player's only back handler: leave Player Lyrics first, then dock or close.
                                onBack = {
                                    when {
                                        playerLyrics -> playerLyrics = false
                                        split != null -> playerFullscreen = false
                                        else -> expanded = false
                                    }
                                },
                            ) {
                                ExpandedPlayer(
                                    viewModel = viewModel,
                                    currentTab = currentTab,
                                    onTabChange = { currentTab = it },
                                    playerLyrics = playerLyrics,
                                    onPlayerLyricsChange = { playerLyrics = it },
                                    onRetryLyrics = viewModel::retryLyrics,
                                    onCollapse = { expanded = false },
                                    paneFullscreen = if (split == null) null else playerFullscreen,
                                    onTogglePane = { playerFullscreen = !playerFullscreen },
                                    queueBeside = split != null && dockedSplit == null,
                                    onOpenArtist = { artist ->
                                        showPage()
                                        viewModel.openArtist(artist)
                                    },
                                    onDragStatusChange = { isPlayerDragging = it },
                                    isOpen = expanded,
                                )
                            }
                        }
                    }
                }
            }

            if (hasBottomNavigation && showBottomNav) {
                Box(
                    Modifier
                        .fillMaxSize()
                        // Wide windows: the bar keeps to the app column beside the mini player or the docked
                        // player; with neither (nothing playing, button mode) it spans the window.
                        .then(
                            if (split == null) {
                                Modifier
                            } else {
                                Modifier.appColumn(split.sideWidth) { if (miniPlayerShown || appDocked) 1f else 0f }
                            },
                        )
                        .graphicsLayer {
                            val c = if (state.current == null) 0f else coverProgress()
                            alpha = (1f - c * 1.6f).coerceIn(0f, 1f)
                            // Nav bar sinks off the bottom as the player card grows over it.
                            translationY = c * 96.dp.toPx()
                        },
                ) {
                    CompositionLocalProvider(LocalMd3NavBar provides material3NavigationEnabled) {
                        bottomNavSlot.value(material3NavigationEnabled)
                    }
                }
            }
            
            playlistTrack?.let { track ->
                AddToPlaylistDialogHost(
                    viewModel = viewModel,
                    track = track,
                    onDismiss = { playlistTrack = null },
                    onAdd = { playlistIds, duplicatePlaylistIds ->
                        viewModel.addToPlaylists(playlistIds, duplicatePlaylistIds, track)
                        playlistTrack = null
                    },
                    onFindDuplicates = { playlistIds ->
                        viewModel.findDuplicatePlaylistIds(playlistIds, track)
                    },
                    onFindCachedDuplicates = { playlistIds ->
                        viewModel.findCachedDuplicatePlaylistIds(playlistIds, track)
                    },
                    onCreate = { title ->
                        viewModel.createPlaylistAndAdd(title, track)
                        playlistTrack = null
                    },
                )
            }

            MiniPlayerAwareSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(100f),
            )
        }
    }
}

@Composable
internal fun ExpandedPlayer(
    viewModel: PlayerViewModel,
    currentTab: FullPlayerTab,
    onTabChange: (FullPlayerTab) -> Unit,
    playerLyrics: Boolean,
    onPlayerLyricsChange: (Boolean) -> Unit,
    onRetryLyrics: () -> Unit,
    onCollapse: () -> Unit,
    paneFullscreen: Boolean?,
    onTogglePane: () -> Unit,
    queueBeside: Boolean,
    onOpenArtist: (String) -> Unit,
    onDragStatusChange: (Boolean) -> Unit = {},
    isOpen: Boolean = true,
) {
    val state by viewModel.fullPlayerState.collectAsStateWithLifecycle()
    val lyricsState by viewModel.lyricsState.collectAsStateWithLifecycle()
    val lyricsProviderStatuses by viewModel.lyricsProviderStatuses.collectAsStateWithLifecycle()
    val canvas by viewModel.canvasState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val likedTrackKeys by viewModel.likedTrackKeys.collectAsStateWithLifecycle()
    val currentTrack = state.current
    // Same test Liked Songs storage uses: normalized title|artist OR YouTube id.
    val isLiked = currentTrack != null &&
        currentTrack.toGeneratedTrack().isLikedIn(likedTrackKeys)
    val youTubeLiked by viewModel.youTubeLiked.collectAsStateWithLifecycle()
    val lyricsOffsetMs by viewModel.liveLyricsOffsetMs.collectAsStateWithLifecycle()
    FullPlayer(
        state = state,
        progressState = viewModel.progressState,
        player = viewModel.player,
        lyricsState = lyricsState,
        lyricsProviderStatuses = lyricsProviderStatuses,
        onProbeLyricsProviders = viewModel::probeLyricsProviders,
        lyricsStyle = settings.lyricsStyle,
        onSetLyricsStyle = viewModel::setLyricsStyle,
        seekbarStyle = settings.seekbarStyle,
        wavySeekbarEnabled = settings.wavySeekbarEnabled,
        rotatingBackgroundEnabled = settings.rotatingBackgroundEnabled,
        lyricsRotatingBackgroundEnabled = settings.lyricsRotatingBackgroundEnabled,
        fluidBeatSyncEnabled = settings.fluidBeatSyncEnabled,
        lyricsOffsetMs = lyricsOffsetMs,
        onSetLyricsOffsetMs = viewModel::setLyricsOffsetMs,
        lyricsFontScale = settings.lyricsFontScale,
        onSetLyricsFontScale = viewModel::setLyricsFontScale,
        lyricsCentered = settings.lyricsCentered,
        onSetLyricsCentered = viewModel::setLyricsCentered,
        lyricsButtonsAutoHideSeconds = settings.lyricsButtonsAutoHideSeconds,
        translationLanguage = settings.translationLanguage,
        canvas = canvas,
        canvasEnabled = settings.canvasEnabled,
        canvasFullBleedEnabled = settings.canvasFullBleed,
        canvasGradientBlurEnabled = settings.canvasGradientBlurEnabled,
        musicVideoEnabled = settings.musicVideoEnabled,
        musicVideoNowPlayingButtonEnabled = settings.musicVideoNowPlayingButtonEnabled,
        showAutoEqInVolumePanel = settings.showAutoEqInVolumePanel,
        currentTab = currentTab,
        onTabChange = onTabChange,
        playerLyrics = playerLyrics,
        onPlayerLyricsChange = onPlayerLyricsChange,
        onRetryLyrics = onRetryLyrics,
        onCollapse = onCollapse,
        paneFullscreen = paneFullscreen,
        onTogglePane = onTogglePane,
        queueBeside = queueBeside,
        onOpenArtist = onOpenArtist,
        isLiked = isLiked,
        // One saved state drives the heart and YouTube sync.
        onToggleLiked = { currentTrack?.let(viewModel::toggleLiked) },
        onDoubleTapLike = { currentTrack?.let(viewModel::like) },
        youTubeLiked = youTubeLiked,
        onToggleYouTubeLike = { currentTrack?.let(viewModel::toggleYouTubeLike) },
        onDragStatusChange = onDragStatusChange,
        isOpen = isOpen,
        onSelectLyricsProvider = viewModel::selectLyricsProvider,
        onTryAnotherLyricsSource = viewModel::tryAnotherLyricsSource,
    )
}

@Composable
internal fun PlayerArtwork(
    track: PlayableTrack,
    modifier: Modifier,
    corner: androidx.compose.ui.unit.Dp,
    decodeSizePx: Int? = null,
    canvas: com.lastwave.app.data.canvas.CanvasArtwork? = null,
    isPlaying: Boolean = false,
    pausedForTransition: Boolean = false,
    transformations: List<coil.transform.Transformation> = emptyList(),
    onAspectRatioChanged: (Float) -> Unit = {},
) {
    // The sleeve cover and its canvas are stacked in the same bounds: the
    // cover is the placeholder while the video loads, then hands the pixels
    // over as the canvas's own alpha climbs. Without this the static art
    // stays at full opacity underneath the video and shows through wherever
    // the canvas is transparent.
    var sleeveCover by remember(canvas) { mutableFloatStateOf(0f) }
    Box(modifier.clip(RoundedCornerShape(corner)).background(MaterialTheme.colorScheme.surfaceContainerHighest), contentAlignment = Alignment.Center) {
        ArtworkImage(
            name = track.title,
            artist = track.artist,
            embeddedUrl = track.artworkUrl,
            fallbackIcon = Icons.Filled.MusicNote,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha =
                        if (sleeveCover >= 0.95f) 0f else (1f - sleeveCover).coerceIn(0f, 1f)
                },
            decodeSizePx = decodeSizePx,
            transformations = transformations,
        )
        if (canvas != null) {
            CanvasArtworkPlayer(
                canvas = canvas,
                isPlaying = isPlaying,
                pausedForTransition = pausedForTransition,
                onAspectRatioChanged = onAspectRatioChanged,
                onCoverChanged = { sleeveCover = it },
                contentMode = CanvasContentMode.CROP,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** True for files copied by the Local songs importer (app-private storage). */
internal fun PlayableTrack.isImportedLocalFile(): Boolean =
    com.lastwave.app.data.generate.LocalAudioFormats.importedPathOrNull(playbackUrl) != null

// -- Player open/close presentation (PixelMusic-style bottom sheet transition) ---

internal val PlayerSheetOpenSpec = spring<Float>(
    dampingRatio = 0.88f,
    stiffness = 110f,
    visibilityThreshold = 0.001f,
)
internal val PlayerSheetCloseSpec = spring<Float>(
    dampingRatio = 0.92f,
    stiffness = 95f,
    visibilityThreshold = 0.001f,
)

/** Top corner radius of the sliding sheet in collapsed/transit state. */
internal val PLAYER_SHEET_CORNER_RADIUS = 28.dp

/**
 * Outline for the morphing player card: a rounded rect spanning [left]..[right]
 * horizontally and 0..[height] vertically (layer is already translated to the card top).
 */
internal data class PlayerMorphShape(
    val left: Float,
    val right: Float,
    val height: Float,
    val radius: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density,
    ): Outline {
        val rect = androidx.compose.ui.geometry.Rect(left, 0f, right, height.coerceAtLeast(0f))
        val r = radius.coerceIn(0f, rect.minDimension / 2f)
        return if (r <= 0.5f) {
            Outline.Rectangle(rect)
        } else {
            Outline.Rounded(androidx.compose.ui.geometry.RoundRect(rect, CornerRadius(r, r)))
        }
    }
}
