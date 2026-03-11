package com.example.melodist.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.melodist.viewmodels.PlayerUiState

@Composable
fun MiniPlayer(
    state: PlayerUiState,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClickExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    MiniPlayerContent(
        state = state,
        onTogglePlayPause = onTogglePlayPause,
        onNext = onNext,
        onPrevious = onPrevious,
        onClickExpand = onClickExpand,
        modifier = modifier
    )
}