package com.example.melodist.player

import com.sun.jna.Pointer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MpvAudioPlayer {
    private var handle: Pointer? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    fun init() {
        if (handle != null) return
        try {
            handle = MpvLib.INSTANCE.mpv_create()
            handle?.let {
                MpvLib.INSTANCE.mpv_set_property_string(it, "video", "no")
                MpvLib.INSTANCE.mpv_set_property_string(it, "audio-channels", "stereo")
                MpvLib.INSTANCE.mpv_initialize(it)
            }
        } catch (e: Exception) {
            // Error handling could be added here if needed
        }
    }

    fun openUri(uri: String) {
        init()
        handle?.let {
            MpvLib.INSTANCE.mpv_command(it, arrayOf("loadfile", uri, "replace"))
            MpvLib.INSTANCE.mpv_set_property_string(it, "pause", "no")
            _isPlaying.value = true
        }
    }

    fun play() {
        handle?.let {
            MpvLib.INSTANCE.mpv_set_property_string(it, "pause", "no")
            _isPlaying.value = true
        }
    }

    fun pause() {
        handle?.let {
            MpvLib.INSTANCE.mpv_set_property_string(it, "pause", "yes")
            _isPlaying.value = false
        }
    }

    fun stop() {
        handle?.let {
            MpvLib.INSTANCE.mpv_command(it, arrayOf("stop"))
            _isPlaying.value = false
        }
    }

    fun seekTo(percent: Float) {
        handle?.let {
            val position = (percent * 100).coerceIn(0f, 100f)
            MpvLib.INSTANCE.mpv_command(it, arrayOf("seek", "$position", "absolute-percent"))
        }
    }

    var volume: Float
        get() {
            val volStr = handle?.let { MpvLib.INSTANCE.mpv_get_property_string(it, "volume") } ?: "100"
            return volStr.toFloatOrNull() ?: 100f
        }
        set(value) {
            handle?.let {
                val vol = (value * 100).toInt().coerceIn(0, 100)
                MpvLib.INSTANCE.mpv_set_property_string(it, "volume", "$vol")
            }
        }

    fun getDuration(): Long {
        val durStr = handle?.let { MpvLib.INSTANCE.mpv_get_property_string(it, "duration") } ?: "0"
        return (durStr.toDoubleOrNull() ?: 0.0).toLong() * 1000L
    }

    fun getCurrentPosition(): Long {
        val posStr = handle?.let { MpvLib.INSTANCE.mpv_get_property_string(it, "time-pos") } ?: "0"
        return (posStr.toDoubleOrNull() ?: 0.0).toLong() * 1000L
    }

    fun dispose() {
        handle?.let {
            MpvLib.INSTANCE.mpv_terminate_destroy(it)
            handle = null
        }
    }
}
