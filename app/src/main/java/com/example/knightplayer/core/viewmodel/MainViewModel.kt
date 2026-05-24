package com.example.knightplayer.core.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.knightplayer.core.data.LocalMediaRepository
import com.example.knightplayer.core.models.MediaCollection
import com.example.knightplayer.core.models.MediaEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    //////////////////////////////////////////////////////////
    // 📂 LIBRARY STATE
    //////////////////////////////////////////////////////////
    private val _folders = MutableStateFlow<List<MediaCollection>>(emptyList())
    val folders: StateFlow<List<MediaCollection>> = _folders.asStateFlow()

    //////////////////////////////////////////////////////////
    // 🎬 PLAYER STATE (Shared across the whole app)
    //////////////////////////////////////////////////////////
    private val _playingPlaylist = MutableStateFlow<List<MediaEntry>?>(null)
    val playingPlaylist: StateFlow<List<MediaEntry>?> = _playingPlaylist.asStateFlow()

    var playingIndex = 0
        private set

    var resumePosition = 0L
        private set

    //////////////////////////////////////////////////////////
    // ⚙️ ACTIONS
    //////////////////////////////////////////////////////////

    /**
     * Scans the device for media on a background thread.
     * Because this is in the ViewModelScope, it won't be cancelled if the user rotates the phone!
     */
    fun scanLocalMedia(context: Context) {
        viewModelScope.launch {
            _folders.value = LocalMediaRepository.getAllLocalMedia(context)
        }
    }

    /**
     * Triggers the fullscreen player with the selected playlist.
     */
    fun openPlayer(playlist: List<MediaEntry>, startIndex: Int, position: Long = 0L) {
        playingIndex = startIndex
        resumePosition = position
        _playingPlaylist.value = playlist
    }

    /**
     * Closes the player and returns to the previous screen.
     */
    fun closePlayer() {
        _playingPlaylist.value = null
    }
}
