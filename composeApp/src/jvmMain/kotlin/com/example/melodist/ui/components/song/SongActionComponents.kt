package com.example.melodist.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.unit.DpOffset
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.melodist.player.DownloadState
import com.metrolist.innertube.models.SongItem

@Composable
internal fun DownloadIndicatorContent(
    state: DownloadState?,
    modifier: Modifier = Modifier
) {
    when (state) {
        is DownloadState.Queued -> Icon(
            Icons.Default.HourglassTop,
            contentDescription = "En cola",
            modifier = modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        is DownloadState.Downloading -> Box(modifier = modifier.size(20.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { if (state.progress >= 0f) state.progress else 0f },
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round
            )
        }
        is DownloadState.Completed -> Icon(
            Icons.Default.DownloadDone,
            contentDescription = "Descargada",
            modifier = modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
        is DownloadState.Failed -> Icon(
            Icons.Default.ErrorOutline,
            contentDescription = "Error de descarga",
            modifier = modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        )
        else -> Unit
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
internal fun SongContextMenuContent(
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
    offset: DpOffset = DpOffset.Zero
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = offset,
    ) {
        when (downloadState) {
            is DownloadState.Completed -> DropdownMenuItem(
                text = { Text("Eliminar descarga") },
                onClick = { onRemoveDownload(); onDismiss() },
                leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) }
            )
            is DownloadState.Downloading, is DownloadState.Queued -> DropdownMenuItem(
                text = { Text("Cancelar descarga") },
                onClick = { onCancelDownload(); onDismiss() },
                leadingIcon = { Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.error) }
            )
            else -> DropdownMenuItem(
                text = { Text("Descargar") },
                onClick = { onDownload(); onDismiss() },
                leadingIcon = { Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.primary) }
            )
        }

        onPlayNext?.let { playNext ->
            DropdownMenuItem(
                text = { Text("Reproducir a continuación") },
                onClick = { playNext(); onDismiss() },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null, tint = MaterialTheme.colorScheme.primary) }
            )
        }

        onAddToQueue?.let { addToQueue ->
            DropdownMenuItem(
                text = { Text("Agregar al final de la cola") },
                onClick = { addToQueue(); onDismiss() },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, null, tint = MaterialTheme.colorScheme.onSurface) }
            )
        }

        onRemoveFromLibrary?.let { remove ->
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
                text = { Text("Eliminar de la biblioteca") },
                onClick = { remove(); onDismiss() },
                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
            )
        }
    }
}
