package com.example.melodist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.example.melodist.data.AppDirs
import com.example.melodist.data.AppPreferences
import com.example.melodist.data.account.AccountManager
import com.example.melodist.di.appModule
import com.example.melodist.navigation.RootComponent
import com.example.melodist.player.PlayerService
import com.example.melodist.utils.UpdateService
import com.example.melodist.viewmodels.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import com.kdroid.composetray.tray.api.Tray
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.JRootPane

private val logger = Logger.getLogger("Melodist")

fun main() {
    setupEnvironments()
    
    val koinApp = try {
        startKoin { modules(appModule) }
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "Error iniciando dependencias", e)
        throw e
    }

    try {
        koinApp.koin.get<PlayerService>().init()
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "Error iniciando servicios de audio", e)
    }

    val playerViewModel = koinApp.koin.get<PlayerViewModel>()
    val mediaKeyListener = MediaKeyListener(
        onPlayPause = { playerViewModel.togglePlayPause() },
        onNext      = { playerViewModel.next() },
        onPrevious  = { playerViewModel.previous() },
        onStop      = { playerViewModel.stop() },
    ).apply { register() }

    application {
        val lifecycle = remember { LifecycleRegistry() }
        val rootComponent = remember {
            RootComponent(
                componentContext = DefaultComponentContext(lifecycle = lifecycle),
                musicRepository = koinApp.koin.get(),
                searchRepository = koinApp.koin.get()
            )
        }

        val windowState = rememberWindowState(width = 1200.dp, height = 600.dp)
        var isVisible by remember { mutableStateOf(true) }
        val scope = rememberCoroutineScope()
        val minimizeToTray by AppPreferences.minimizeToTray.collectAsState()

        // --- Lógica de Actualización ---
        var updateRelease by remember { mutableStateOf<com.example.melodist.utils.GitHubRelease?>(null) }
        LaunchedEffect(Unit) {
            updateRelease = UpdateService.checkForUpdates()
        }

        fun doExit() {
            mediaKeyListener.unregister()
            try { koinApp.koin.get<PlayerService>().release() } catch (_: Exception) {}
            stopKoin()
            exitApplication()
        }

        if (!isVisible || minimizeToTray) {
            val playerState by playerViewModel.uiState.collectAsState()
            val isPlaying = playerState.playbackState == com.example.melodist.player.PlaybackState.PLAYING

            Tray(
                icon = Icons.Filled.MusicNote,
                tooltip = playerState.currentSong?.title ?: "Melodist",
                primaryAction = { isVisible = !isVisible },
            ) {
                Item(label = if (isPlaying) "Pausar" else "Reproducir", onClick = { playerViewModel.togglePlayPause() })
                Item(label = "Siguiente", onClick = { playerViewModel.next() })
                Item(label = "Anterior", onClick = { playerViewModel.previous() })
                Divider()
                Item(label = "Abrir Melodist", onClick = { isVisible = true })
                Item(label = "Salir", onClick = { doExit() })
            }
        }

        Window(
            onCloseRequest = { if (minimizeToTray) isVisible = false else doExit() },
            visible = isVisible,
            title = "Melodist",
            state = windowState,
            undecorated = true,
            transparent = false,
        ) {
            LaunchedEffect(Unit) {
                window.rootPane?.windowDecorationStyle = JRootPane.NONE
                window.minimumSize = java.awt.Dimension(800, 500)
            }

            val maximizer = remember { WindowMaximizer(windowState, window) }
            var isMaximized by remember { mutableStateOf(false) }

            App(
                rootComponent = rootComponent,
                windowState = windowState,
                isMaximized = isMaximized,
                onMinimize = { windowState.isMinimized = true },
                onMaximizeRestore = {
                    maximizer.toggleMaximize()
                    isMaximized = maximizer.isMaximized
                },
                onClose = { if (minimizeToTray) isVisible = false else doExit() }
            )

            // Diálogo de actualización
            updateRelease?.let { release ->
                AlertDialog(
                    onDismissRequest = { updateRelease = null },
                    title = { Text("Actualización disponible") },
                    text = { Text("Hay una nueva versión disponible: ${release.tagName}. ¿Deseas instalarla ahora?") },
                    confirmButton = {
                        Button(onClick = {
                            val asset = release.assets.find { it.name.endsWith(".msi") }
                            asset?.let { 
                                scope.launch(Dispatchers.IO) {
                                    UpdateService.downloadAndInstall(it)
                                }
                            }
                            updateRelease = null
                        }) { Text("Actualizar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { updateRelease = null }) { Text("Más tarde") }
                    }
                )
            }
        }
    }
}

private fun setupEnvironments() {
    AppDirs.ensureDirectories()
    val tmpDir = AppDirs.dataRoot.resolve("tmp").also { it.mkdirs() }
    System.setProperty("java.io.tmpdir", tmpDir.absolutePath)
    AccountManager.init()
    
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        logger.log(Level.SEVERE, "Error crítico en ${thread.name}", throwable)
    }
}
