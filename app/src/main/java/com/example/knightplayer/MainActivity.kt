package com.example.knightplayer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.example.knightplayer.components.KnightGlassCard
import com.example.knightplayer.components.NetworkStreamDialog
import com.example.knightplayer.core.data.HistoryManager
import com.example.knightplayer.core.models.MediaCollection
import com.example.knightplayer.core.models.MediaEntry
import com.example.knightplayer.core.models.MediaSourceType
import com.example.knightplayer.core.viewmodel.MainViewModel
import com.example.knightplayer.ui.theme.KnightPlayerTheme
import com.example.knightplayer.ui.theme.ThemeModeOption
import com.example.knightplayer.ui.theme.ThemePreferences
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var externalVideoUri: Uri? = null
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) setAppContent() else finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        externalVideoUri = intent?.data

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        ThemePreferences.init(this)
        checkPermission()
    }

    private fun checkPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            setAppContent()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    private fun setAppContent() {
        setContent {
            val themeMode by ThemePreferences.themeModeFlow.collectAsState()
            val accentColor by ThemePreferences.accentColorFlow.collectAsState()

            val isDarkTheme = when (themeMode) {
                ThemeModeOption.LIGHT -> false
                ThemeModeOption.DARK -> true
                ThemeModeOption.SYSTEM -> isSystemInDarkTheme()
            }

            KnightPlayerTheme(
                darkTheme = isDarkTheme,
                accentColor = accentColor.colorValue
            ) {
                AppUI(externalVideoUri)
            }
        }
    }
}

fun getFileNameFromUri(context: Context, uri: Uri): String {
    var name = "External Video"
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && index != -1) {
            name = it.getString(index)
        }
    }
    return name
}

@OptIn(UnstableApi::class)
@Composable
fun AppUI(
    externalVideoUri: Uri?,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(1) }
    var searchQuery by remember { mutableStateOf("") }
    var currentScreen by remember { mutableStateOf("settings") }
    var openedFolder by remember { mutableStateOf<MediaCollection?>(null) }

    val folders by viewModel.folders.collectAsState()
    val playingPlaylist by viewModel.playingPlaylist.collectAsState()

    val allVideos = folders.flatMap { it.entries }
    val filteredVideos = allVideos.filter { it.title.contains(searchQuery, ignoreCase = true) }
    val rotation = remember { androidx.compose.animation.core.Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Add this inside AppUI
    var showStreamDialog by remember { mutableStateOf(false) }

    if (showStreamDialog) {
        NetworkStreamDialog(
            onDismiss = { showStreamDialog = false },
            onPlay = { url ->
                showStreamDialog = false
                // 🔥 This treats the URL just like a local file
                viewModel.openPlayer(
                    playlist = listOf(
                        MediaEntry(
                            id = url, // The URL is the unique ID
                            title = "Network Stream",
                            uri = android.net.Uri.parse(url),
                            sourceType = com.example.knightplayer.core.models.MediaSourceType.NETWORK
                        )
                    ),
                    startIndex = 0
                )
            }
        )
    }

    HistoryManager.init(context)
    val historyList by HistoryManager.historyFlow.collectAsState()
    val lastWatched = historyList.firstOrNull()

    // 🔥 NEW: Tracks the exact timestamp of the video we dismissed.
    // This ensures if you watch the same video AGAIN, it gets a new timestamp and reappears!
    var dismissedTimestamp by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        viewModel.scanLocalMedia(context)
    }

    LaunchedEffect(externalVideoUri) {
        if (externalVideoUri != null) {
            viewModel.openPlayer(
                playlist = listOf(
                    MediaEntry(
                        id = externalVideoUri.toString(),
                        uri = externalVideoUri,
                        title = getFileNameFromUri(context, externalVideoUri),
                        sourceType = MediaSourceType.LOCAL
                    )
                ),
                startIndex = 0
            )
        }
    }

    // 🔥 FIX: Universal hardware back-button handling
    BackHandler(enabled = (openedFolder != null || selectedTab != 1 || currentScreen != "settings") && playingPlaylist == null) {
        if (selectedTab == 3 && currentScreen != "settings") {
            // 1. If inside Audio/Playback settings -> Go back to Main Settings list
            currentScreen = "settings"
        } else if (openedFolder != null) {
            // 2. If viewing videos in a folder -> Go back to Folder List
            openedFolder = null
        } else {
            // 3. If on History, Search, or Settings -> Go back to the Library Tab (Home)
            selectedTab = 1
            currentScreen = "settings"
        }
    }

    if (playingPlaylist != null) {
        VideoPlayerScreen(
            playlist = playingPlaylist!!,
            startIndex = viewModel.playingIndex,
            startPosition = viewModel.resumePosition,
            onBack = { viewModel.closePlayer() }
        )
        return
    }

    // 🔥 FIX: Handle all Dynamic Titles
    val title = when {
        selectedTab == 3 && currentScreen == "appearance" -> "Appearance"
        selectedTab == 3 && currentScreen == "playback" -> "Playback"
        selectedTab == 3 && currentScreen == "audio" -> "Audio" // 🔥 NEW
        selectedTab == 3 && currentScreen == "subtitles" -> "Subtitles" // 🔥 NEW
        openedFolder != null -> openedFolder!!.name
        else -> when (selectedTab) {
            0 -> "History"
            1 -> "Library"
            2 -> "Search"
            3 -> "Settings"
            else -> "Knight OS"
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔥 ADDED Modifier.weight(1f) HERE
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (selectedTab == 3 && currentScreen != "settings") {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Ensure this is imported
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { currentScreen = "settings" }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1, // 🔥 Prevents the text from growing too much
                        overflow = TextOverflow.Ellipsis // 🔥 Adds "..." if text is too long
                    )
                }

                // This row will now stay pinned to the right
                if (selectedTab != 3) {
                    Row {
                        // 🔥 ADD THIS: The Network Stream Icon (Only shows on Library tab)
                        if (selectedTab == 1) {
                            IconButton(onClick = { showStreamDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = "Stream",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Inside your topBar Row
                        IconButton(
                            onClick = {
                                // 🔥 Trigger the spin!
                                scope.launch {
                                    rotation.animateTo(
                                        targetValue = 360f,
                                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 800)
                                    )
                                    rotation.snapTo(0f) // Reset to 0 after spin
                                }
                                viewModel.scanLocalMedia(context)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.rotate(rotation.value) // 🔥 The magic line
                            )
                        }

                        IconButton(onClick = {
                            selectedTab = 3; openedFolder = null; currentScreen = "settings"
                        }) {
                            Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        bottomBar = {
            // 🔥 THE FIX: Hide Navbar in ANY sub-setting screen
            if (!(selectedTab == 3 && currentScreen != "settings")) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(70.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 🔥 Changed from List to History icon
                            NavIcon(Icons.Default.History, selectedTab == 0) { selectedTab = 0; openedFolder = null; currentScreen = "settings" }

                            // 🔥 Changed from Home to VideoLibrary icon
                            NavIcon(Icons.Default.VideoLibrary, selectedTab == 1) { selectedTab = 1; openedFolder = null; currentScreen = "settings" }

                            // Search stays the same
                            NavIcon(Icons.Default.Search, selectedTab == 2) { selectedTab = 2; openedFolder = null; currentScreen = "settings" }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {

            // 1. YOUR TABS (Notice the MiniPlayer is completely gone from inside here!)
            when {
                selectedTab == 1 && openedFolder == null -> {
                    FolderList(folders) { openedFolder = it }
                }

                openedFolder != null -> {
                    VideoListScreen(
                        videos = openedFolder!!.entries,
                        onVideoClick = { video, index, playlist ->
                            viewModel.openPlayer(playlist, index)
                        }
                    )
                }

                selectedTab == 2 -> {
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search library...", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(25.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        VideoListScreen(
                            videos = filteredVideos,
                            onVideoClick = { video, index, playlist ->
                                viewModel.openPlayer(playlist, index)
                            }
                        )
                    }
                }

                // ⚙️ SETTINGS ROUTING FIX
                selectedTab == 3 -> {
                    when (currentScreen) {
                        "appearance" -> AppearanceScreen()
                        "playback" -> PlaybackSettingsScreen()
                        "audio" -> AudioSettingsScreen()
                        "subtitles" -> SubtitleSettingsScreen()
                        else -> SettingsScreen(onNavigate = { currentScreen = it })
                    }
                }

                else -> {
                    HistoryScreen(onVideoClick = { history ->
                        viewModel.openPlayer(
                            playlist = listOf(
                                MediaEntry(
                                    id = history.videoId.toString(),
                                    title = history.title,
                                    uri = Uri.parse(history.uri),
                                    duration = history.duration
                                )
                            ),
                            startIndex = 0,
                            position = history.position
                        )
                    })
                }
            }

            // 2. 🔥 THE UNIVERSAL MINIPLAYER OVERLAY
            // This lives OUTSIDE the 'when' block so it floats over all tabs!
            if (lastWatched != null &&
                lastWatched.timestamp != dismissedTimestamp && // Stays dead if you swiped it
                selectedTab != 3 && // Hides cleanly when you open Settings
                openedFolder == null // Hides when you are inside a folder
            ) {
                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 115.dp)) {
                    MiniPlayer(
                        history = lastWatched,
                        onClick = {
                            viewModel.openPlayer(
                                playlist = listOf(
                                    MediaEntry(
                                        id = lastWatched.videoId.toString(),
                                        title = lastWatched.title,
                                        uri = Uri.parse(lastWatched.uri),
                                        duration = lastWatched.duration
                                    )
                                ),
                                startIndex = 0,
                                position = lastWatched.position
                            )
                        },
                        onExpand = {
                            viewModel.openPlayer(
                                playlist = listOf(
                                    MediaEntry(
                                        id = lastWatched.videoId.toString(),
                                        title = lastWatched.title,
                                        uri = Uri.parse(lastWatched.uri),
                                        duration = lastWatched.duration
                                    )
                                ),
                                startIndex = 0,
                                position = lastWatched.position
                            )
                        },
                        onDismiss = {
                            // Tells the app: "I swiped this specific session away!"
                            dismissedTimestamp = lastWatched.timestamp
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NavIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun FolderList(folders: List<MediaCollection>, onClick: (MediaCollection) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(folders) { folder ->
            KnightGlassCard(
                modifier = Modifier.fillMaxWidth().aspectRatio(0.9f),
                cornerRadius = 14.dp,
                onClick = { onClick(folder) }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(folder.posterUri)
                            .decoderFactory(VideoFrameDecoder.Factory())
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        alpha = 0.4f
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 30f
                            ))
                    )

                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp).padding(bottom = 6.dp)
                        )
                        Text(
                            text = folder.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${folder.entries.size} Videos",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
