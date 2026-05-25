package com.example.kalah

import android.content.Context
import android.media.MediaPlayer

object MusicManager {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    fun start(context: Context, musicResId: Int) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context.applicationContext, musicResId)
                mediaPlayer?.isLooping = true
            }

            if (!isPlaying) {
                mediaPlayer?.start()
                isPlaying = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pause() {
        try {
            if (mediaPlayer != null && isPlaying) {
                mediaPlayer?.pause()
                isPlaying = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resume() {
        try {
            if (mediaPlayer != null && !isPlaying) {
                mediaPlayer?.start()
                isPlaying = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isPlaying(): Boolean = isPlaying
}