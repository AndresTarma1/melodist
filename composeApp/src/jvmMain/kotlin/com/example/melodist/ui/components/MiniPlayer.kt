package com.example.melodist.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.melodist.ui.components.player.MiniPlayerContent
import com.example.melodist.viewmodels.PlayerProgressState

@Composable
fun MiniPlayer(
    progressState: PlayerProgressState,
    onClickExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    MiniPlayerContent(
        progressState = progressState,
        onClickExpand = onClickExpand,
        modifier = modifier
    )
}