package com.aistudio.unibuddy.qywvsp.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SpotifyState {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<String?>("Not Playing")
    val currentTrack: StateFlow<String?> = _currentTrack.asStateFlow()
    
    private val _currentArtist = MutableStateFlow<String?>("")
    val currentArtist: StateFlow<String?> = _currentArtist.asStateFlow()
    
    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    fun setTrackInfo(track: String?, artist: String?) {
        _currentTrack.value = track
        _currentArtist.value = artist
    }
    
    fun setConnecting(connecting: Boolean) {
        _isConnecting.value = connecting
    }
}
