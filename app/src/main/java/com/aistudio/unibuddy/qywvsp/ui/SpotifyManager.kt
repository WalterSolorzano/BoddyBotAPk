package com.aistudio.unibuddy.qywvsp.ui

import android.content.Context
import android.util.Log
import com.aistudio.unibuddy.qywvsp.BuildConfig
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote

object SpotifyManager {
    private const val CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID
    private const val REDIRECT_URI = "com.aistudio.unibuddy.qywvsp://callback"
    private var spotifyAppRemote: SpotifyAppRemote? = null

    fun connect(context: Context) {
        if (CLIENT_ID.isEmpty() || CLIENT_ID == "YOUR_SPOTIFY_CLIENT_ID") {
            Log.e("SpotifyManager", "Missing Spotify Client ID. Set it in .env")
            return
        }

        SpotifyState.setConnecting(true)
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                SpotifyState.setConnected(true)
                SpotifyState.setConnecting(false)
                
                // Subscribe to PlayerState
                spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { playerState ->
                    val track = playerState.track
                    if (track != null) {
                        SpotifyState.setTrackInfo(track.name, track.artist.name)
                    } else {
                        SpotifyState.setTrackInfo("Not Playing", "")
                    }
                    SpotifyState.setPlaying(!playerState.isPaused)
                }
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("SpotifyManager", "Connection to Spotify failed", throwable)
                SpotifyState.setConnected(false)
                SpotifyState.setConnecting(false)
            }
        })
    }

    fun play(uri: String) {
        spotifyAppRemote?.playerApi?.play(uri)
    }

    fun resume() {
        spotifyAppRemote?.playerApi?.resume()
    }

    fun pause() {
        spotifyAppRemote?.playerApi?.pause()
    }

    fun skipNext() {
        spotifyAppRemote?.playerApi?.skipNext()
    }

    fun skipPrevious() {
        spotifyAppRemote?.playerApi?.skipPrevious()
    }

    fun disconnect() {
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        SpotifyState.setConnected(false)
        SpotifyState.setPlaying(false)
    }
}
