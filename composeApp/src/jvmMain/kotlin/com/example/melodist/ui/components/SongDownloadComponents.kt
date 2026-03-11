package com.example.melodist.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.melodist.player.DownloadState
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
    downloadState: DownloadState?,
    onDownload: () -> Unit,
    onRemoveDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onAddToQueue: (() -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
    onRemoveFromLibrary: (() -> Unit)? = null,
) {
    SongContextMenuContent(
        expanded = expanded,
        onDismiss = onDismiss,
        song = song,
        downloadState = downloadState,
        onDownload = onDownload,
        onRemoveDownload = onRemoveDownload,
        onCancelDownload = onCancelDownload,
        onAddToQueue = onAddToQueue,
        onPlayNext = onPlayNext,
        onRemoveFromLibrary = onRemoveFromLibrary,
    )
}
