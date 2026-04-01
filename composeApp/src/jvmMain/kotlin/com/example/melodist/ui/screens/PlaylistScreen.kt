package com.example.melodist.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.melodist.navigation.Route
import com.example.melodist.ui.components.BlurredImageBackground
import com.example.melodist.ui.components.PlaylistScreenSkeleton
import com.example.melodist.utils.LocalPlayerViewModel
import com.example.melodist.viewmodels.PlaylistState
import com.example.melodist.viewmodels.PlaylistViewModel
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.pages.PlaylistPage


data class PlaylistActions(
    val onBack: () -> Unit,
    val onNavigate: (Route) -> Unit,
    val onToggleSave: () -> Unit,
    val onPlayAll: () -> Unit,
    val onShuffle: () -> Unit,
    val onLoadMore: () -> Unit
)

@Composable
fun PlaylistScreenRoute(
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: PlaylistViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    val successState = uiState as? PlaylistState.Success
    val playerViewModel = LocalPlayerViewModel.current

    val actions = remember(viewModel) {
        PlaylistActions(
            onBack = onBack,
            onNavigate = onNavigate,
            onToggleSave = { viewModel.toggleSave() },
            onLoadMore = { viewModel.loadMoreSongs() },
            onPlayAll = {
                val state = successState ?: return@PlaylistActions
                viewModel.playAllSongs(shuffle = false) { allSongs, startIndex ->
                    playerViewModel.playPlaylist(
                        allSongs,
                        startIndex,
                        state.playlistPage.playlist.id,
                        state.playlistPage.playlist.title
                    )
                }
            },
            onShuffle = {
                val state = successState ?: return@PlaylistActions
                viewModel.playAllSongs(shuffle = true) { allSongs, startIndex ->
                    playerViewModel.playPlaylist(
                        allSongs,
                        startIndex,
                        state.playlistPage.playlist.id,
                        state.playlistPage.playlist.title
                    )
                }
            }
        )
    }

    val songs by viewModel.songs.collectAsState()
    val hasMoreSongs by viewModel.hasMoreSongs.collectAsState()


    PlaylistScreen(
        uiState = uiState,
        songs = songs,
        hasMore = hasMoreSongs,
        isSaved = successState?.isSaved ?: false,
        isSaving = successState?.isSaving ?: false,
        isLoadingForPlay = successState?.isLoadingForPlay ?: false,
        actions = actions
    )
}

@Composable
fun PlaylistScreen(
    uiState: PlaylistState,
    songs: List<SongItem> = emptyList(),
    hasMore: Boolean = false,
    isSaved: Boolean = false,
    isSaving: Boolean = false,
    isLoadingForPlay: Boolean = false,
    actions: PlaylistActions
) {
    val thumbnailUrl = (uiState as? PlaylistState.Success)?.playlistPage?.playlist?.thumbnail

    BlurredImageBackground(
        imageUrl = thumbnailUrl,
        modifier = Modifier.fillMaxSize(),
        darkOverlayAlpha = 0.52f,
        gradientFraction = 0.45f
    ) {
        when (uiState) {
            is PlaylistState.Loading -> PlaylistScreenSkeleton()
            is PlaylistState.Success -> PlaylistScreenContent(
                playlistPage = uiState.playlistPage,
                songs = songs,
                hasMore = hasMore,
                isSaved = isSaved,
                isSaving = isSaving,
                isLoadingForPlay = isLoadingForPlay,
                actions = actions
            )

            is PlaylistState.Error -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(uiState.message, color = MaterialTheme.colorScheme.error)
            }
        }

        IconButton(
            onClick = actions.onBack,
            modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PlaylistScreenContent(
    playlistPage: PlaylistPage,
    songs: List<SongItem>,
    hasMore: Boolean = false,
    isSaved: Boolean = false,
    isSaving: Boolean = false,
    isLoadingForPlay: Boolean = false,
    actions: PlaylistActions
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 800.dp

        PlaylistWide(
            playlistPage = playlistPage,
            songs = songs,
            hasMore = hasMore,
            isSaved = isSaved,
            isSaving = isSaving,
            isLoadingForPlay = isLoadingForPlay,
            actions = actions
        )
    }

}
