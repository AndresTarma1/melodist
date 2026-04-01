package com.example.melodist.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import com.example.melodist.player.DownloadState
import com.example.melodist.ui.components.song.DownloadIndicatorContent
import com.example.melodist.ui.components.song.SongContextMenuContent
import com.metrolist.innertube.models.SongItem

@Composable
fun DownloadIndicator(
    state: DownloadState?,
    modifier: Modifier = Modifier
) {
    DownloadIndicatorContent(
        state = state,
        modifier = modifier
    )
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun SongContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    song: SongItem,
    onRemoveFromLibrary: (() -> Unit)? = null,
    offset: DpOffset = DpOffset.Zero
) {
    SongContextMenuContent(
        expanded = expanded,
        onDismiss = onDismiss,
        song = song,
        onRemoveFromLibrary = onRemoveFromLibrary,
        offset = offset
    )
}
