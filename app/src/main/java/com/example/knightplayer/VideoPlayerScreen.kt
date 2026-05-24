package com.example.knightplayer

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.knightplayer.core.models.MediaEntry
import com.example.knightplayer.core.player.SubtitleCue
import com.example.knightplayer.core.player.SrtParser
import com.example.knightplayer.components.KnightGlassCard
import com.example.knightplayer.ui.theme.ThemePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.media.audiofx.LoudnessEnhancer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.toArgb
import com.example.knightplayer.core.data.HistoryManager // 🔥 Added Import

@UnstableApi
@Composable
fun VideoPlayerScreen(
    playlist: List<MediaEntry>,
    startIndex: Int,
    startPosition: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    val seekbarThickness by ThemePreferences.seekbarThicknessFlow.collectAsState()
    val iconScale by ThemePreferences.iconScaleFlow.collectAsState()
    val topIconOrder by ThemePreferences.topIconOrderFlow.collectAsState()
    val isSeekEnabled by ThemePreferences.seekEnabledFlow.collectAsState()
    val skipDuration by ThemePreferences.doubleTapDurationFlow.collectAsState()
    val isPinchToZoomEnabled by ThemePreferences.pinchToZoomFlow.collectAsState()
    val forceSoftwareDecoder by ThemePreferences.forceSoftwareDecoderFlow.collectAsState()

    val dynamicIconSize = (32 * iconScale.coerceIn(0.7f, 1.2f)).dp

    DisposableEffect(Unit) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    var currentWindowIndex by remember { mutableIntStateOf(startIndex) }

    val subFont by ThemePreferences.subFontFlow.collectAsState()
    val subSize by ThemePreferences.subSizeFlow.collectAsState()
    val subTextColor by ThemePreferences.subTextColorFlow.collectAsState()
    val subBgColor by ThemePreferences.subBgColorFlow.collectAsState()
    val subBgOpacity by ThemePreferences.subBgOpacityFlow.collectAsState()
    val subBorderColor by ThemePreferences.subBorderColorFlow.collectAsState()
    val subBorderWidth by ThemePreferences.subBorderWidthFlow.collectAsState()
    val subPosY by ThemePreferences.subPosYFlow.collectAsState()
    val subPosX by ThemePreferences.subPosXFlow.collectAsState()

    var videoScale by remember { mutableFloatStateOf(1f) }
    var videoPan by remember { mutableStateOf(Offset.Zero) }

    var customSubtitles by remember { mutableStateOf<List<SubtitleCue>>(emptyList()) }
    var subtitleSyncMs by remember { mutableLongStateOf(0L) }
    val coroutineScope = rememberCoroutineScope()

    val subtitleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { coroutineScope.launch(Dispatchers.IO) { customSubtitles = SrtParser.parse(context, uri) } }
    }

    val volumeBoost by ThemePreferences.volumeBoostFlow.collectAsState()
    val audioPassthrough by ThemePreferences.audioPassthroughFlow.collectAsState()
    var loudnessEnhancer by remember { mutableStateOf<LoudnessEnhancer?>(null) }
    var customAudioUri by remember { mutableStateOf<Uri?>(null) }
    var audioSyncMs by remember { mutableLongStateOf(0L) }
    val audioSyncProcessor = remember { com.example.knightplayer.core.player.AudioSyncProcessor() }

    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) customAudioUri = uri
    }
    LaunchedEffect(audioSyncMs) { audioSyncProcessor.setSyncOffset(audioSyncMs) }

    val player = remember(context, forceSoftwareDecoder, audioPassthrough) {
        val customFactory = com.example.knightplayer.core.player.CustomRenderersFactory(
            context,
            audioSyncProcessor,
            forceSoftwareDecoder,
            audioPassthrough
        )
        ExoPlayer.Builder(context).setRenderersFactory(customFactory).build()
    }

    LaunchedEffect(player, playlist, customAudioUri) {
        val currentPos = if (player.currentPosition > 0) player.currentPosition else startPosition
        val currentIndex = if (player.currentMediaItemIndex != C.INDEX_UNSET) player.currentMediaItemIndex else startIndex

        val dataSourceFactory = DefaultDataSource.Factory(context)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        val sources = playlist.mapIndexed { index, video ->
            val builder = MediaItem.Builder().setUri(video.uri).build()
            var source: MediaSource = mediaSourceFactory.createMediaSource(builder)

            if (index == currentWindowIndex && customAudioUri != null) {
                val audioItem = MediaItem.fromUri(customAudioUri!!)
                val audioSource = mediaSourceFactory.createMediaSource(audioItem)
                source = MergingMediaSource(source, audioSource)
            }
            source
        }

        player.setMediaSources(sources)
        player.prepare()
        player.seekTo(currentIndex, currentPos)
        player.playWhenReady = true
    }

    // 🎛️ UI STATE
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    // 🔥 HISTORY SAVER HELPER
    val saveCurrentProgress = {
        try {
            val currentVideo = playlist.getOrNull(currentWindowIndex)
            val pos = position // Using our tracked Compose state
            val totalDur = duration.takeIf { it > 0 } ?: currentVideo?.duration ?: 0L

            // Only save if we actually watched more than 5 seconds of the video
            if (currentVideo != null && pos > 5000L) {

                // 1. Build the History Object
                val historyEntry = WatchHistory(
                    videoId = currentVideo.id,
                    title = currentVideo.title,
                    uri = currentVideo.uri.toString(),
                    duration = totalDur,
                    position = pos,
                    timestamp = System.currentTimeMillis() // 🔥 ADDED THIS LINE
                )

                // 2. Save it to permanent storage!
                HistoryManager.saveHistory(context, historyEntry)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> { player.pause(); saveCurrentProgress() } // 🔥 Trigger save
                Lifecycle.Event.ON_STOP -> { player.pause(); saveCurrentProgress() } // 🔥 Trigger save
                Lifecycle.Event.ON_DESTROY -> { saveCurrentProgress(); player.release() }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var currentTracks by remember { mutableStateOf(player.currentTracks) }
    val audioTracks = currentTracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }
    val subtitleTracks = currentTracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }
    var isPlayingState by remember { mutableStateOf(player.isPlaying) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { isPlayingState = isPlaying }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                    try {
                        loudnessEnhancer?.release()
                        val enhancer = LoudnessEnhancer(audioSessionId)
                        enhancer.setTargetGain(2000)
                        enhancer.enabled = volumeBoost
                        loudnessEnhancer = enhancer
                    } catch (e: Exception) {
                        loudnessEnhancer = null
                        android.util.Log.e("AudioBoost", "Failed to attach LoudnessEnhancer: ${e.message}")
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (currentWindowIndex != player.currentMediaItemIndex) {
                    saveCurrentProgress() // 🔥 Trigger save before index changes!
                    currentWindowIndex = player.currentMediaItemIndex
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO || reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
                        customSubtitles = emptyList()
                        customAudioUri = null
                    }
                }
            }
            override fun onTracksChanged(tracks: Tracks) { currentTracks = tracks }
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(context, "Playback Error: ${error.errorCodeName}", Toast.LENGTH_LONG).show()
                customAudioUri = null
            }
        }
        player.addListener(listener)
        onDispose {
            loudnessEnhancer?.release()
            player.removeListener(listener)
        }
    }

    LaunchedEffect(volumeBoost, loudnessEnhancer) {
        try { loudnessEnhancer?.enabled = volumeBoost } catch (e: Exception) {}
    }

    var isFullscreen by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var accumulatedDragX by remember { mutableFloatStateOf(0f) }
    val horizontalPadding = 36.dp
    var showControls by remember { mutableStateOf(true) }

    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showSpeedIsland by remember { mutableStateOf(false) }
    var showAudioMenu by remember { mutableStateOf(false) }
    var showSubtitleMenu by remember { mutableStateOf(false) }
    var showPlaylistMenu by remember { mutableStateOf(false) }

    var showSeekUI by remember { mutableStateOf(false) }
    var seekDirection by remember { mutableStateOf("RIGHT") }
    var seekSeconds by remember { mutableIntStateOf(0) }
    var gestureLock by remember { mutableStateOf("NONE") }
    var accumulatedSeekMs by remember { mutableLongStateOf(0L) }
    var lastTapDirection by remember { mutableStateOf("NONE") }

    var showVolumeUI by remember { mutableStateOf(false) }
    var showBrightnessUI by remember { mutableStateOf(false) }
    var volumePercent by remember { mutableFloatStateOf(50f) }
    var brightnessPercent by remember { mutableFloatStateOf(50f) }
    var smoothedPosition by remember { mutableLongStateOf(0L) }
    val isHoldFFEnabled by ThemePreferences.holdToFFEnabledFlow.collectAsState()
    val holdFFSpeed by ThemePreferences.holdFFSpeedFlow.collectAsState()
    var isFastForwarding by remember { mutableStateOf(false) }

    LaunchedEffect(isFullscreen) {
        if (isFullscreen) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        } else {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    BackHandler {
        saveCurrentProgress() // 🔥 Trigger save on back gesture
        player.release()
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onBack()
    }

    LaunchedEffect(player, isDragging) {
        while (true) {
            if (!isDragging) {
                val current = player.currentPosition
                val total = player.duration.takeIf { it > 0 } ?: 1L
                duration = total
                position = current.coerceIn(0L, total)
            }
            delay(16)
        }
    }

    LaunchedEffect(playbackSpeed) { player.setPlaybackSpeed(playbackSpeed) }
    LaunchedEffect(showControls, isDragging) { if (showControls && !isDragging) { delay(4500); if (!isDragging) showControls = false } }
    LaunchedEffect(showSpeedIsland, playbackSpeed) { if (showSpeedIsland) { delay(3500); showSpeedIsland = false } }
    LaunchedEffect(showAudioMenu) { if (showAudioMenu) { delay(4500); showAudioMenu = false } }
    LaunchedEffect(showSubtitleMenu) { if (showSubtitleMenu) { delay(4500); showSubtitleMenu = false } }
    LaunchedEffect(showPlaylistMenu) { if (showPlaylistMenu) { delay(3500); showPlaylistMenu = false } }
    LaunchedEffect(showSeekUI, seekSeconds, isDragging) { if (showSeekUI && !isDragging) { delay(1200); showSeekUI = false; accumulatedSeekMs = 0L; lastTapDirection = "NONE" } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(isPinchToZoomEnabled) {
                if (!isPinchToZoomEnabled) return@pointerInput
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val canceled = event.changes.any { it.isConsumed }
                        if (!canceled) {
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()
                            val pointerCount = event.changes.size

                            if (pointerCount >= 2) {
                                videoScale = (videoScale * zoomChange).coerceIn(1f, 5f)
                                if (videoScale > 1f) videoPan += panChange else videoPan = Offset.Zero
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                    if (videoScale <= 1f) videoPan = Offset.Zero
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (isHoldFFEnabled) {
                            val releasedEarly = kotlinx.coroutines.withTimeoutOrNull(600) { tryAwaitRelease() }
                            if (releasedEarly == null) {
                                isFastForwarding = true
                                val originalSpeed = player.playbackParameters.speed
                                player.setPlaybackSpeed(holdFFSpeed)
                                tryAwaitRelease()
                                player.setPlaybackSpeed(originalSpeed)
                                isFastForwarding = false
                            }
                        }
                    },
                    onTap = { showControls = !showControls },
                    onDoubleTap = { offset ->
                        if (isSeekEnabled) {
                            val isLeft = offset.x < size.width / 2
                            val seekAmount = skipDuration

                            if (lastTapDirection == "NONE") accumulatedSeekMs = 0L

                            if (isLeft) {
                                if (lastTapDirection != "LEFT") accumulatedSeekMs = 0L
                                accumulatedSeekMs -= seekAmount
                                lastTapDirection = "LEFT"
                            } else {
                                if (lastTapDirection != "RIGHT") accumulatedSeekMs = 0L
                                accumulatedSeekMs += seekAmount
                                lastTapDirection = "RIGHT"
                            }

                            val targetPosition = (player.currentPosition + accumulatedSeekMs).coerceIn(0L, player.duration)
                            player.seekTo(targetPosition)
                            seekDirection = lastTapDirection
                            seekSeconds = (kotlin.math.abs(accumulatedSeekMs) / 1000).toInt()
                            showControls = false
                            showSeekUI = true
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                var dragStartPosition = 0L
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragStartPosition = player.currentPosition
                        smoothedPosition = player.currentPosition
                        showControls = true
                    },
                    onDragEnd = {
                        if (gestureLock == "HORIZONTAL") player.seekTo(smoothedPosition)
                        gestureLock = "NONE"
                        showVolumeUI = false
                        showBrightnessUI = false
                        isDragging = false
                        showSeekUI = false
                    }
                ) { change, dragAmount ->
                    change.consume()
                    accumulatedDragX += dragAmount.x
                    val absX = kotlin.math.abs(dragAmount.x)
                    val absY = kotlin.math.abs(dragAmount.y)

                    if (videoScale > 1f) return@detectDragGestures

                    if (gestureLock == "NONE") {
                        val threshold = 12f
                        if (absX > threshold || absY > threshold) {
                            gestureLock = if (absX > absY) "HORIZONTAL" else "VERTICAL"
                        }
                    }

                    if (gestureLock == "VERTICAL") {
                        val deltaPercent = (-dragAmount.y / size.height) * 100f
                        if (!showVolumeUI && !showBrightnessUI) {
                            val isLeftSide = change.position.x < size.width / 2f
                            if (isLeftSide) { showBrightnessUI = true; showVolumeUI = false }
                            else { showVolumeUI = true; showBrightnessUI = false }
                        }

                        if (showVolumeUI) {
                            volumePercent = (volumePercent + deltaPercent).coerceIn(0f, 100f)
                            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (volumePercent / 100f * maxVol).toInt(), 0)
                        }

                        if (showBrightnessUI) {
                            brightnessPercent = (brightnessPercent + deltaPercent).coerceIn(0f, 100f)
                            val window = (context as Activity).window
                            val params = window.attributes
                            params.screenBrightness = brightnessPercent / 100f
                            window.attributes = params
                        }
                    } else if (gestureLock == "HORIZONTAL") {
                        val duration = player.duration.takeIf { it > 0 } ?: return@detectDragGestures
                        val deltaMs = (dragAmount.x / size.width) * 300_000L
                        smoothedPosition = (smoothedPosition + deltaMs.toLong()).coerceIn(0L, duration)
                        position = smoothedPosition
                        val totalDelta = smoothedPosition - dragStartPosition
                        seekSeconds = (kotlin.math.abs(totalDelta) / 1000).toInt()
                        seekDirection = if (totalDelta >= 0) "RIGHT" else "LEFT"
                        showSeekUI = true
                    }
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                }
            },
            update = { view ->
                view.resizeMode = resizeMode
                view.subtitleView?.visibility = if (customSubtitles.isNotEmpty()) View.GONE else View.VISIBLE

                view.subtitleView?.apply {
                    val nativeTextColor = subTextColor.colorValue.toArgb()
                    val nativeBgColor = subBgColor.colorValue.copy(alpha = subBgOpacity).toArgb()
                    val nativeBorderColor = subBorderColor.colorValue.toArgb()

                    val typeface = if (subFont.fontResId != -1) {
                        androidx.core.content.res.ResourcesCompat.getFont(context, subFont.fontResId)
                    } else {
                        android.graphics.Typeface.DEFAULT
                    }

                    val edgeType = if (subBorderWidth > 0f) androidx.media3.ui.CaptionStyleCompat.EDGE_TYPE_OUTLINE else androidx.media3.ui.CaptionStyleCompat.EDGE_TYPE_NONE
                    val style = androidx.media3.ui.CaptionStyleCompat(
                        nativeTextColor,
                        nativeBgColor,
                        android.graphics.Color.TRANSPARENT,
                        edgeType,
                        nativeBorderColor,
                        typeface
                    )

                    setStyle(style)
                    setFixedTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, subSize)

                    val paddingPx = (subPosY * context.resources.displayMetrics.density).toInt()
                    setPadding(0, 0, 0, paddingPx)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = videoScale
                    scaleY = videoScale
                    translationX = videoPan.x
                    translationY = videoPan.y
                }
        )

        if (customSubtitles.isNotEmpty()) {
            val adjustedPosition = position - subtitleSyncMs
            val currentSubtitle = customSubtitles.find { adjustedPosition in it.startTime..it.endTime }

            if (currentSubtitle != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (showControls) 130.dp else subPosY.dp)
                        .offset(x = subPosX.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(subBgColor.colorValue.copy(alpha = subBgOpacity))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (subBorderWidth > 0f) {
                        Text(
                            text = currentSubtitle.text,
                            color = subBorderColor.colorValue,
                            style = TextStyle(
                                fontFamily = subFont.getFontFamily(),
                                fontSize = subSize.sp,
                                fontWeight = FontWeight.Bold,
                                drawStyle = Stroke(width = subBorderWidth)
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        text = currentSubtitle.text,
                        color = subTextColor.colorValue,
                        style = TextStyle(
                            fontFamily = subFont.getFontFamily(),
                            fontSize = subSize.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showSeekUI,
            enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.8f, transformOrigin = if (seekDirection == "RIGHT") TransformOrigin(1f, 0.5f) else TransformOrigin(0f, 0.5f)),
            exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.8f, transformOrigin = if (seekDirection == "RIGHT") TransformOrigin(1f, 0.5f) else TransformOrigin(0f, 0.5f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                KnightGlassCard(
                    modifier = Modifier
                        .align(if (seekDirection == "RIGHT") Alignment.CenterEnd else Alignment.CenterStart)
                        .padding(horizontal = 32.dp)
                        .offset(y = if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) 90.dp else 0.dp),
                    cornerRadius = 24.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 18.dp)
                    ) {
                        Icon(
                            imageVector = if (seekDirection == "LEFT") Icons.Default.FastRewind else Icons.Default.FastForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "${seekSeconds}s", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showVolumeUI || showBrightnessUI,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().offset(y = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) (-26).dp else 0.dp)
            ) {
                if (showVolumeUI) VerticalPill(Alignment.CenterEnd, volumePercent, Icons.Default.VolumeUp)
                if (showBrightnessUI) VerticalPill(Alignment.CenterStart, brightnessPercent, Icons.Default.Brightness6)
            }
        }

        AnimatedVisibility(
            visible = isFastForwarding,
            enter = fadeIn(tween(150)) + slideInVertically(initialOffsetY = { -50 }),
            exit = fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { -50 })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                KnightGlassCard(cornerRadius = 24.dp) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${String.format("%.1f", holdFFSpeed)}x",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showSpeedIsland,
            enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.85f),
            exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.85f)
        ) {
            Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
                KnightGlassCard(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 56.dp, end = horizontalPadding),
                    cornerRadius = 36.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier.padding(horizontal = 19.dp, vertical = 18.dp)
                    ) {
                        Text(
                            text = "–", color = MaterialTheme.colorScheme.primary, fontSize = 32.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.pointerInput(Unit) { detectTapGestures(onTap = { playbackSpeed = (playbackSpeed - 0.1f).coerceAtLeast(0.5f); showSpeedIsland = true }) }
                        )
                        Text(text = String.format("%.1f", playbackSpeed), color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "+", color = MaterialTheme.colorScheme.primary, fontSize = 32.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.pointerInput(Unit) { detectTapGestures(onTap = { playbackSpeed = (playbackSpeed + 0.1f).coerceAtMost(5.0f); showSpeedIsland = true }) }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = showAudioMenu, enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.85f), exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.85f)) {
            Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
                KnightGlassCard(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 56.dp, end = horizontalPadding).width(if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) 200.dp else 240.dp).heightIn(max = 400.dp),
                    cornerRadius = 26.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState())) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { audioLauncher.launch("audio/*") }.padding(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add Custom Audio", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "-50ms", tint = Color.White, modifier = Modifier.clickable { audioSyncMs -= 50; showAudioMenu = true })
                            Text("Sync: ${audioSyncMs}ms", color = Color.White, fontSize = 12.sp)
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "+50ms", tint = Color.White, modifier = Modifier.clickable { audioSyncMs += 50; showAudioMenu = true })
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                        audioTracks.forEach { group ->
                            for (i in 0 until group.length) {
                                val format = group.getTrackFormat(i)
                                val trackName = when {
                                    !format.label.isNullOrBlank() -> format.label!!
                                    !format.language.isNullOrBlank() -> format.language!!.uppercase()
                                    format.channelCount > 0 -> "Audio (${if (format.channelCount == 6) "5.1" else if (format.channelCount == 8) "7.1" else "${format.channelCount}ch"})"
                                    else -> "Audio Track ${i + 1}"
                                }
                                val isSelected = group.isTrackSelected(i)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon().setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, listOf(i))).build()
                                    }.padding(vertical = 4.dp)
                                ) {
                                    Checkbox(checked = isSelected, onCheckedChange = null, modifier = Modifier.size(20.dp), colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary, uncheckedColor = Color.White))
                                    Spacer(Modifier.width(8.dp))
                                    Text(text = trackName, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = showSubtitleMenu, enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.85f), exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.85f)) {
            Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
                KnightGlassCard(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 56.dp, end = horizontalPadding).width(if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) 200.dp else 240.dp).heightIn(max = 400.dp),
                    cornerRadius = 26.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState())) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { subtitleLauncher.launch("*/*") }.padding(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add Custom .SRT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "-50ms", tint = Color.White, modifier = Modifier.clickable { subtitleSyncMs -= 50; showSubtitleMenu = true })
                            Text("Sync: ${subtitleSyncMs}ms", color = Color.White, fontSize = 12.sp)
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "+50ms", tint = Color.White, modifier = Modifier.clickable { subtitleSyncMs += 50; showSubtitleMenu = true })
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable {
                                customSubtitles = emptyList()
                                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true).build()
                            }.padding(vertical = 4.dp)
                        ) {
                            val isOffSelected = customSubtitles.isEmpty() && subtitleTracks.none { group -> (0 until group.length).any { group.isTrackSelected(it) } }
                            Checkbox(checked = isOffSelected, onCheckedChange = null, modifier = Modifier.size(20.dp), colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary, uncheckedColor = Color.White))
                            Spacer(Modifier.width(8.dp))
                            Text(text = "OFF", color = Color.White, maxLines = 1)
                        }
                        if (customSubtitles.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Checkbox(checked = true, onCheckedChange = null, modifier = Modifier.size(20.dp), colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary, uncheckedColor = Color.White))
                                Spacer(Modifier.width(8.dp))
                                Text(text = "Custom Loaded", color = Color.White, maxLines = 1)
                            }
                        }
                        subtitleTracks.forEach { group ->
                            for (i in 0 until group.length) {
                                val format = group.getTrackFormat(i)
                                val subtitleName = when {
                                    !format.label.isNullOrBlank() -> format.label!!
                                    !format.language.isNullOrBlank() -> format.language!!.uppercase()
                                    else -> "Subtitle ${i + 1}"
                                }
                                val isSelected = customSubtitles.isEmpty() && group.isTrackSelected(i)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        customSubtitles = emptyList()
                                        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false).setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, listOf(i))).build()
                                    }.padding(vertical = 4.dp)
                                ) {
                                    Checkbox(checked = isSelected, onCheckedChange = null, modifier = Modifier.size(20.dp), colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary, uncheckedColor = Color.White))
                                    Spacer(Modifier.width(8.dp))
                                    Text(text = subtitleName, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = showPlaylistMenu, enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.85f), exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.85f)) {
            Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
                KnightGlassCard(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 56.dp, end = horizontalPadding, bottom = 110.dp).width(if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) 220.dp else 260.dp).heightIn(max = 320.dp),
                    cornerRadius = 26.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState())) {
                        playlist.forEachIndexed { index, video ->
                            val isCurrent = index == currentWindowIndex
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    customSubtitles = emptyList()
                                    customAudioUri = null
                                    player.seekTo(index, 0L)
                                    player.prepare()
                                    player.playWhenReady = true
                                    currentWindowIndex = index
                                    showPlaylistMenu = false
                                }.padding(vertical = 6.dp)
                            ) {
                                Icon(imageVector = if (isCurrent) Icons.Default.PlayArrow else Icons.Default.Movie, contentDescription = null, tint = if (isCurrent) MaterialTheme.colorScheme.primary else Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(text = video.title, color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = showControls, enter = fadeIn(tween(320)), exit = fadeOut(tween(320))) {
            Box(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    IconButton(
                        onClick = { if (player.hasPreviousMediaItem()) { player.seekToPreviousMediaItem(); player.playWhenReady = true } },
                        modifier = Modifier.size(64.dp)
                    ) { Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.fillMaxSize()) }

                    IconButton(
                        onClick = { if (player.isPlaying) player.pause() else player.play(); showControls = true },
                        modifier = Modifier.size(92.dp)
                    ) {
                        AnimatedContent(targetState = isPlayingState, transitionSpec = { fadeIn(tween(120)) togetherWith fadeOut(tween(120)) }, label = "play_pause") { playing ->
                            Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.fillMaxSize())
                        }
                    }

                    IconButton(
                        onClick = { if (player.hasNextMediaItem()) { player.seekToNextMediaItem(); player.playWhenReady = true } },
                        modifier = Modifier.size(64.dp)
                    ) { Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.fillMaxSize()) }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(start = horizontalPadding, end = horizontalPadding, top = 4.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        saveCurrentProgress() // 🔥 Trigger save on top bar arrow
                        player.release()
                        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(dynamicIconSize))
                    }
                    Text(text = playlist[currentWindowIndex].title, color = Color.White, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        topIconOrder.forEach { iconName ->
                            when (iconName) {
                                "SPEED" -> Icon(Icons.Default.Speed, null, tint = Color.White, modifier = Modifier.size(dynamicIconSize).pointerInput(Unit) { detectTapGestures(onTap = { showSpeedIsland = !showSpeedIsland; showAudioMenu = false; showSubtitleMenu = false; showPlaylistMenu = false }) })
                                "AUDIO" -> Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size(dynamicIconSize).clickable { showAudioMenu = !showAudioMenu; showSubtitleMenu = false; showSpeedIsland = false; showPlaylistMenu = false })
                                "SUBTITLES" -> Icon(Icons.Default.Subtitles, null, tint = Color.White, modifier = Modifier.size(dynamicIconSize).clickable { showSubtitleMenu = !showSubtitleMenu; showAudioMenu = false; showSpeedIsland = false; showPlaylistMenu = false })
                                "PLAYLIST" -> Icon(Icons.Default.PlaylistPlay, null, tint = Color.White, modifier = Modifier.size(dynamicIconSize).clickable { showPlaylistMenu = !showPlaylistMenu; showAudioMenu = false; showSubtitleMenu = false; showSpeedIsland = false })
                            }
                        }
                    }
                }

                Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 24.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding)) {
                        Slider(
                            value = remember(position, duration) { if (duration > 0) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f },
                            onValueChange = { fraction ->
                                isDragging = true
                                val newPos = (fraction * duration).toLong().coerceIn(0L, duration);
                                position = newPos
                            },
                            onValueChangeFinished = {
                                player.seekTo(position)
                                isDragging = false
                            },
                            modifier = Modifier.fillMaxWidth().height(seekbarThickness.dp).graphicsLayer { renderEffect = null },
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary, inactiveTrackColor = Color.White.copy(0.28f))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(formatTime(position), color = Color.White)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(formatTime(duration), color = Color.White)
                                Spacer(Modifier.width(12.dp))

                                IconButton(onClick = {
                                    resizeMode = when (resizeMode) {
                                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                        else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    }
                                }) { Icon(Icons.Default.Crop, contentDescription = "Resize", tint = Color.White, modifier = Modifier.size(dynamicIconSize)) }

                                IconButton(onClick = {
                                    activity.requestedOrientation = if (isFullscreen) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                    isFullscreen = !isFullscreen
                                }) { Icon(Icons.Default.ScreenRotation, contentDescription = "Rotate", tint = Color.White, modifier = Modifier.size(dynamicIconSize)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val hours = totalSec / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}

@Composable
fun VerticalPill(alignment: Alignment, percent: Float, icon: ImageVector) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = alignment) {
        Box(
            modifier = Modifier.padding(start = if (alignment == Alignment.CenterStart && isLandscape) 48.dp else 24.dp, end = if (alignment == Alignment.CenterEnd && isLandscape) 48.dp else 24.dp).width(44.dp).height(220.dp).clip(RoundedCornerShape(28.dp)).background(Color.Black.copy(alpha = 0.45f))
        ) {
            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(percent / 100f).background(Color.White.copy(alpha = 0.95f)))
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp).size(20.dp))
        }
    }
}
