package com.example.melodist.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.melodist.navigation.Route
import com.example.melodist.ui.components.artwork.LocalArtworkColors
import com.example.melodist.ui.components.player.WideLayout
import com.example.melodist.viewmodels.PlayerProgressState
import com.example.melodist.viewmodels.PlayerUiState

// ─────────────────────────────────────────────────────────────────────────────
// NOW PLAYING PANEL — shell + fondo + selección responsive de layout
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NowPlayingPanel(
    state: PlayerUiState,
    progressState: PlayerProgressState,
    onCollapse: () -> Unit,
    onNavigate: ((Route) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val song = state.currentSong ?: return
    val artworkColors = LocalArtworkColors.current


    Box(modifier = modifier.fillMaxSize()) {
        WideLayout(
            state = state,
            progressState = progressState,
            song = song,
            onCollapse = onCollapse,
            artworkColors = artworkColors,
            onNavigate = onNavigate
        )

    }
}

