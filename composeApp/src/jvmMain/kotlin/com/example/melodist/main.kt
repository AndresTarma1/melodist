package com.example.melodist

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.example.melodist.data.AppDirs
import com.example.melodist.data.AppPreferences
import com.example.melodist.data.account.AccountManager
import com.example.melodist.di.appModule
import com.example.melodist.navigation.NavigationDesktop
import com.example.melodist.navigation.RootComponent
import com.example.melodist.player.PlaybackState
import com.example.melodist.player.PlayerService
import com.example.melodist.player.WindowsMediaSession
import com.example.melodist.ui.components.LocalArtworkColors
import com.example.melodist.ui.components.rememberArtworkColors
import com.example.melodist.ui.themes.MelodistTheme
import com.example.melodist.utils.LocalDownloadViewModel
import com.example.melodist.utils.LocalPlayerViewModel
import com.example.melodist.viewmodels.DownloadViewModel
import com.example.melodist.viewmodels.PlayerViewModel
import com.kdroid.composetray.tray.api.Tray
import melodist.composeapp.generated.resources.Res
import melodist.composeapp.generated.resources.music_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.TitleBarScope
import org.jetbrains.jewel.window.styling.DecoratedWindowMetrics
import org.jetbrains.jewel.window.styling.DecoratedWindowStyle
import org.jetbrains.jewel.window.styling.TitleBarColors
import org.jetbrains.jewel.window.styling.TitleBarStyle
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import java.time.temporal.TemporalAdjusters.previous

fun main() {
    setupEnvironments()

    val koinApp = startKoin { modules(appModule) }.also {
        runCatching { it.koin.get<PlayerService>().init() }
    }

    val playerViewModel = koinApp.koin.get<PlayerViewModel>()
    val downloadViewModel = koinApp.koin.get<DownloadViewModel>()

    koinApp.koin.get<WindowsMediaSession>().apply {
        initialize()
        setCallbacks(
            onPlay = { playerViewModel.togglePlayPause() },
            onPause = { playerViewModel.togglePlayPause() },
            onNext = { playerViewModel.next() },
            onPrevious = { playerViewModel.previous() },
            onStop = { playerViewModel.stop() },
        )
        setPositionProvider { playerViewModel.progressState.value.positionMs }
    }

    application {
        val lifecycle = remember { LifecycleRegistry() }
        val rootComponent = remember {
            RootComponent(
                componentContext = DefaultComponentContext(lifecycle),
                musicRepository = koinApp.koin.get(),
                searchRepository = koinApp.koin.get(),
            )
        }

        val windowState = rememberWindowState(width = 1200.dp, height = 600.dp)
        var isVisible by remember { mutableStateOf(true) }
        val minimizeToTray by AppPreferences.minimizeToTray.collectAsState()

        fun doExit() {
            koinApp.koin.get<WindowsMediaSession>().release()
            runCatching { koinApp.koin.get<PlayerService>().release() }
            stopKoin()
            exitApplication()
        }

        // ── Tray ─────────────────────────────────────────────────────────────
        if (!isVisible || minimizeToTray) {
            val trayState by playerViewModel.uiState.collectAsState()
            val isPlaying = trayState.playbackState == PlaybackState.PLAYING
            Tray(
                icon = Icons.Filled.MusicNote,
                tooltip = trayState.currentSong?.title ?: "Melodist",
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

        // ── Theme state ───────────────────────────────────────────────────────
        val playerState by playerViewModel.uiState.collectAsState()
        val artworkColors = rememberArtworkColors(playerState.currentSong?.thumbnail)
        val isDark = !artworkColors.isLight

        // ── Window ────────────────────────────────────────────────────────────
        // MelodistTheme ANTES de IntUiTheme para poder leer
        // MaterialTheme.colorScheme.surface al construir el TitleBarStyle
        MelodistTheme(artworkColors = artworkColors) {
            // surface = color del NavigationRail → la TitleBar tendrá el mismo fondo
            val railBackground = MaterialTheme.colorScheme.surface

            val titleBarStyle = if (isDark)
                TitleBarStyle.dark(
                    colors = TitleBarColors.dark(
                        backgroundColor = railBackground,
                        inactiveBackground = railBackground.copy(alpha = 0.85f),
                        borderColor = MaterialTheme.colorScheme.surface
                    )
                )
            else
                TitleBarStyle.light(
                    colors = TitleBarColors.light(
                        backgroundColor = railBackground,
                        inactiveBackground = railBackground.copy(alpha = 0.85f),
                        borderColor = MaterialTheme.colorScheme.surface
                    )
                )

            IntUiTheme(
                theme = if (isDark)
                    JewelTheme.darkThemeDefinition()
                else
                    JewelTheme.lightThemeDefinition(),
                styling = ComponentStyling.decoratedWindow(
                    titleBarStyle = titleBarStyle,
                ),
            ) {
                CompositionLocalProvider(
                    LocalArtworkColors provides artworkColors,
                    LocalPlayerViewModel provides playerViewModel,
                    LocalDownloadViewModel provides downloadViewModel,
                ) {
                    DecoratedWindow(
                        onCloseRequest = { if (minimizeToTray) isVisible = false else doExit() },
                        state = windowState,
                        visible = isVisible,
                        title = "Melodist",
                        icon = painterResource(Res.drawable.music_icon),
                    ) {
                        TitleBar {
                            MelodistTitleBar(
                                currentSong = playerState.currentSong?.title,
                                isPlaying = playerState.playbackState == PlaybackState.PLAYING,
                            )
                        }

                        NavigationDesktop(rootComponent)


                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TitleBar content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TitleBarScope.MelodistTitleBar(
    currentSong: String?,
    isPlaying: Boolean,
) {
    // Izquierda: ícono + nombre app
    Row(
        modifier = Modifier.align(Alignment.Start).padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Melodist",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }

    // Centro: canción actual animada
    AnimatedContent(
        targetState = currentSong,
        transitionSpec = {
            (fadeIn() + slideInVertically { it / 2 })
                .togetherWith(fadeOut() + slideOutVertically { -it / 2 })
        },
        modifier = Modifier.align(Alignment.CenterHorizontally),
    ) { song ->
        if (song != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.GraphicEq else Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = song,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 300.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Setup
// ─────────────────────────────────────────────────────────────────────────────

private fun setupEnvironments() {
    AppDirs.ensureDirectories()
    System.setProperty(
        "java.io.tmpdir",
        AppDirs.dataRoot.resolve("tmp").also { it.mkdirs() }.absolutePath,
    )
    AccountManager.init()
}