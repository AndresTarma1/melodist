package com.example.melodist.player

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Low-level VLC wrapper — owns the native media player instance.
 *
 * Call [release] when the application exits.
 */
class PlayerService {

    private val log = Logger.getLogger("PlayerService")

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _volume = MutableStateFlow(100)
    val volume: StateFlow<Int> = _volume.asStateFlow()

    private var factory: MediaPlayerFactory? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vlcAvailable = false
    private var initAttempted = false

    /** Hilo dedicado para todas las operaciones nativas de VLC — evita bloquear la UI o coroutines. */
    private val vlcDispatcher = java.util.concurrent.Executors.newSingleThreadExecutor { r ->
        Thread(r, "vlc-native").also { it.isDaemon = true }
    }.asCoroutineDispatcher()

    private val scope = CoroutineScope(vlcDispatcher + SupervisorJob())
    private var tickJob: Job? = null

    /** When true, suppresses the VLC 'stopped' event from resetting state to IDLE.
     *  Set by [stopAudioOnly], cleared by [play] once the new media starts. */
    @Volatile
    private var isTransitioning = false

    // ─── Initialization ────────────────────────────────────

    fun init() {
        if (initAttempted) return
        initAttempted = true

        try {
            val bundledPath = findBundledVlc()
            if (bundledPath != null) {
                log.info("Usando VLC embebido en: $bundledPath")
                System.setProperty("jna.library.path", bundledPath)
                val pluginsDir = File(bundledPath, "plugins")
                if (pluginsDir.isDirectory) {
                    System.setProperty("VLC_PLUGIN_PATH", pluginsDir.absolutePath)
                }
            } else {
                log.info("No se encontró VLC embebido, intentando VLC del sistema...")
                val found = NativeDiscovery().discover()
                log.info("Resultado NativeDiscovery (VLC sistema): $found")
            }

            val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0"
            val referer  = "https://music.youtube.com/"
            factory = MediaPlayerFactory(
                "--no-video",
                "--quiet",
                "--no-lua",
                "--http-user-agent=$userAgent",
                "--http-referrer=$referer",
            )
            mediaPlayer = factory!!.mediaPlayers().newMediaPlayer()
            attachListeners()
            startPositionTicker()
            vlcAvailable = true
            log.info("VLC initialized successfully")
        } catch (e: Exception) {
            log.log(Level.SEVERE, "Failed to initialize VLC. Audio playback will not work.", e)
            vlcAvailable = false
        }
    }

    private fun findBundledVlc(): String? {
        // Permite forzar la ruta vía propiedad/env.
        val overrideProp = System.getProperty("melodist.vlc.path")
        val overrideEnv  = System.getenv("MELODIST_VLC_PATH")
        if (!overrideProp.isNullOrBlank()) {
            val dir = File(overrideProp)
            if (dir.isDirectory) {
                log.info("VLC override (prop melosdist.vlc.path): ${dir.absolutePath}")
                return dir.absolutePath
            }
            log.warning("VLC override (prop) inválido: ${dir.absolutePath}")
        }
        if (!overrideEnv.isNullOrBlank()) {
            val dir = File(overrideEnv)
            if (dir.isDirectory) {
                log.info("VLC override (env MELODIST_VLC_PATH): ${dir.absolutePath}")
                return dir.absolutePath
            }
            log.warning("VLC override (env) inválido: ${dir.absolutePath}")
        }

        val exeDir = try {
            File(PlayerService::class.java.protectionDomain.codeSource.location.toURI()).parentFile
        } catch (_: Exception) { null }

        val userDir = File(System.getProperty("user.dir"))
        val parentUserDir = userDir.parentFile
        val resourcesDir = System.getProperty("compose.application.resources.dir")

        val candidates = listOfNotNull(
            // Caso 1: DLLs directamente en resources/ (instalado via appResourcesRootDir)
            if (!resourcesDir.isNullOrBlank()) File(resourcesDir) else null,
            // Caso 2: DLLs en resources/vlc/ (subcarpeta)
            if (!resourcesDir.isNullOrBlank()) File(resourcesDir, "vlc") else null,
            // Caso 3: desarrollo — carpeta vlc/ en el directorio de trabajo
            File("vlc"),
            File(userDir, "vlc"),
            parentUserDir?.resolve("vlc"),
            exeDir?.resolve("vlc"),
            exeDir?.resolve("resources"),
            exeDir?.parentFile?.resolve("resources"),
        )

        val found = candidates.firstOrNull { dir ->
            dir.isDirectory && (
                dir.resolve("libvlc.dll").exists() ||
                dir.resolve("libvlccore.dll").exists() ||
                dir.resolve("libvlc.dylib").exists() ||
                dir.resolve("libvlc.so").exists()
            )
        }
        found?.let { log.info("VLC embebido encontrado en: ${it.absolutePath}") }
        if (found == null) {
            log.info("VLC embebido no encontrado en los candidatos revisados")
        }
        return found?.absolutePath
    }

    // ─── Playback controls ────────────────────────────────

    fun play(url: String) {
        init()
        if (!vlcAvailable || mediaPlayer == null) {
            log.warning("VLC not available, cannot play: $url")
            _playbackState.value = PlaybackState.ERROR
            return
        }
        log.info("Playing: ${url.take(20)}...")
        _playbackState.value = PlaybackState.LOADING
        _position.value = 0L
        _duration.value = 0L
        isTransitioning = false
        scope.launch {
            try {
                try {
                    if (mediaPlayer!!.status().isPlaying) {
                        mediaPlayer!!.controls().stop()
                    }
                } catch (_: Exception) { }
                mediaPlayer!!.media().play(url)
            } catch (e: Exception) {
                log.log(Level.SEVERE, "Error calling VLC play", e)
                _playbackState.value = PlaybackState.ERROR
            }
        }
    }

    fun pause() {
        if (!vlcAvailable) return
        _playbackState.value = PlaybackState.PAUSED
        scope.launch {
            try { mediaPlayer?.controls()?.pause() } catch (e: Exception) { log.log(Level.WARNING, "Error pausing", e) }
        }
    }

    fun resume() {
        if (!vlcAvailable) return
        _playbackState.value = PlaybackState.PLAYING
        scope.launch {
            try { mediaPlayer?.controls()?.play() } catch (e: Exception) { log.log(Level.WARNING, "Error resuming", e) }
        }
    }

    fun togglePlayPause() {
        when (_playbackState.value) {
            PlaybackState.PLAYING -> pause()
            PlaybackState.PAUSED -> resume()
            else -> { /* ignore */ }
        }
    }

    fun stop() {
        isTransitioning = false
        _playbackState.value = PlaybackState.IDLE
        _position.value = 0L
        _duration.value = 0L
        if (vlcAvailable) {
            scope.launch {
                try { mediaPlayer?.controls()?.stop() } catch (e: Exception) { log.log(Level.WARNING, "Error stopping", e) }
            }
        }
    }

    fun stopAudioOnly() {
        if (!vlcAvailable) return
        isTransitioning = true
        _position.value = 0L
        scope.launch {
            try {
                if (mediaPlayer?.status()?.isPlaying == true || mediaPlayer?.status()?.isPlayable == true) {
                    mediaPlayer?.controls()?.stop()
                }
            } catch (_: Exception) { }
        }
    }

    fun seekTo(millis: Long) {
        if (!vlcAvailable) return
        val dur = _duration.value
        if (dur > 0) {
            scope.launch {
                try {
                    mediaPlayer?.controls()?.setPosition(millis.toFloat() / dur.toFloat())
                } catch (e: Exception) { log.log(Level.WARNING, "Error seeking", e) }
            }
        }
    }

    fun setVolume(value: Int) {
        val clamped = value.coerceIn(0, 200)
        _volume.value = clamped
        if (!vlcAvailable) return
        scope.launch {
            try { mediaPlayer?.audio()?.setVolume(clamped) } catch (e: Exception) { log.log(Level.WARNING, "Error setting volume", e) }
        }
    }

    // ─── Lifecycle ─────────────────────────────────────────

    fun release() {
        tickJob?.cancel()
        scope.cancel()
        vlcDispatcher.close()
        try { mediaPlayer?.release() } catch (_: Exception) {}
        try { factory?.release() } catch (_: Exception) {}
        mediaPlayer = null
        factory = null
    }

    // ─── Internal ──────────────────────────────────────────

    private fun attachListeners() {
        mediaPlayer?.events()?.addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
            override fun playing(mediaPlayer: MediaPlayer) {
                _playbackState.value = PlaybackState.PLAYING
            }

            override fun paused(mediaPlayer: MediaPlayer) {
                _playbackState.value = PlaybackState.PAUSED
            }

            override fun stopped(mediaPlayer: MediaPlayer) {
                if (isTransitioning) return
                _playbackState.value = PlaybackState.IDLE
                _position.value = 0L
            }

            override fun finished(mediaPlayer: MediaPlayer) {
                _playbackState.value = PlaybackState.ENDED
            }

            override fun error(mediaPlayer: MediaPlayer) {
                log.warning("VLC playback error")
                _playbackState.value = PlaybackState.ERROR
            }

            override fun buffering(mediaPlayer: MediaPlayer, newCache: Float) {
                if (newCache < 100f) {
                    _playbackState.value = PlaybackState.BUFFERING
                } else if (_playbackState.value == PlaybackState.BUFFERING) {
                    _playbackState.value = PlaybackState.PLAYING
                }
            }

            override fun lengthChanged(mediaPlayer: MediaPlayer, newLength: Long) {
                _duration.value = newLength
            }
        })
    }

    private fun startPositionTicker() {
        tickJob = scope.launch {
            while (isActive) {
                val mp = mediaPlayer
                if (mp != null && vlcAvailable &&
                    (_playbackState.value == PlaybackState.PLAYING || _playbackState.value == PlaybackState.BUFFERING)
                ) {
                    try {
                        val dur = mp.status().length()
                        val pos = (mp.status().position() * dur).toLong()
                        _duration.value = dur
                        _position.value = pos
                    } catch (_: Exception) { }
                }
                delay(1000) // 1000ms — less frequent to reduce UI load
            }
        }
    }
}
