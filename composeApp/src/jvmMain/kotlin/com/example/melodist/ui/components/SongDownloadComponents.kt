package com.example.melodist.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.melodist.player.DownloadState
import com.example.melodist.ui.helpers.rememberSongDownloadState
import com.example.melodist.utils.LocalSnackbarHostState
import com.example.melodist.utils.LocalDownloadViewModel
import com.example.melodist.utils.LocalPlayerViewModel
import com.example.melodist.utils.LocalLibraryViewModel
import com.metrolist.innertube.models.SongItem
import kotlinx.coroutines.launch

@Composable
fun DownloadIndicator(
    state: DownloadState?,
    modifier: Modifier = Modifier
) {
    when (state) {
        is DownloadState.Queued -> Icon(
            Icons.Default.HourglassTop,
            contentDescription = "En cola",
            modifier = modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
            tint = MaterialTheme.colorScheme.primary
        )
        is DownloadState.Failed -> Icon(
            Icons.Default.ErrorOutline,
            contentDescription = "Error de descarga",
            modifier = modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.error
        )
        else -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    song: SongItem,
    onRemoveFromLibrary: (() -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    isLocalPlaylist: Boolean = false,
    showQueueActions: Boolean = true,
    offset: DpOffset = DpOffset.Zero
) {
    val downloadViewModel = LocalDownloadViewModel.current
    val downloadState by rememberSongDownloadState(song.id, downloadViewModel)
    val playerViewModel = LocalPlayerViewModel.current
    val libraryViewModel = LocalLibraryViewModel.current
    val localPlaylists by libraryViewModel.localPlaylists.collectAsState()
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }
    val snackbarHostState = LocalSnackbarHostState.current
    val snackbarScope = rememberCoroutineScope()

    MaterialTheme(
        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(24.dp))
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            offset = offset,
        ) {
            @Composable
            fun StyledMenuItem(
                text: String,
                icon: ImageVector,
                iconTint: Color,
                onClick: () -> Unit
            ) {
                DropdownMenuItem(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    text = { Text(text) },
                    onClick = { onClick(); onDismiss() },
                    leadingIcon = { Icon(icon, null, tint = iconTint) }
                )
            }

            when (downloadState) {
                is DownloadState.Completed -> StyledMenuItem(
                    "Eliminar descarga",
                    Icons.Default.DeleteOutline,
                    MaterialTheme.colorScheme.error
                ) { downloadViewModel.removeDownload(song.id) }

                is DownloadState.Downloading, is DownloadState.Queued -> StyledMenuItem(
                    "Cancelar descarga",
                    Icons.Default.Cancel,
                    MaterialTheme.colorScheme.error
                ) { downloadViewModel.cancelDownload(song.id) }

                else -> StyledMenuItem(
                    "Descargar",
                    Icons.Default.Download,
                    MaterialTheme.colorScheme.primary
                ) { downloadViewModel.downloadSong(song) }
            }

            if (showQueueActions) {
                StyledMenuItem(
                    "Reproducir a continuación",
                    Icons.AutoMirrored.Filled.PlaylistAdd,
                    MaterialTheme.colorScheme.primary
                ) { playerViewModel.playNext(song) }

                StyledMenuItem(
                    "Agregar al final de la cola",
                    Icons.AutoMirrored.Filled.QueueMusic,
                    MaterialTheme.colorScheme.primary
                ) { playerViewModel.addToQueue(song) }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp))
            StyledMenuItem(
                "Añadir a playlist",
                Icons.AutoMirrored.Filled.PlaylistAdd,
                MaterialTheme.colorScheme.primary
            ) { showPlaylistDialog = true }

            onRemoveFromLibrary?.let { remove ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp))
                StyledMenuItem(
                    "Eliminar de la biblioteca",
                    Icons.Default.Delete,
                    MaterialTheme.colorScheme.error,
                    remove
                )
            }

            if (isLocalPlaylist) {
                onRemoveFromPlaylist?.let { remove ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp))
                    StyledMenuItem(
                        "Quitar de playlist",
                        Icons.Default.DeleteOutline,
                        MaterialTheme.colorScheme.error,
                        remove
                    )
                }
            }
        }

        if (showPlaylistDialog) {
            Dialog(onDismissRequest = {
                playlistName = ""
                showPlaylistDialog = false
            }) {
                MaterialTheme(
                    shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(24.dp))
                ) {
                    androidx.compose.material3.Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp)
                        ) {
                            Text("Añadir a playlist", style = MaterialTheme.typography.titleLarge)

                            OutlinedTextField(
                                value = playlistName,
                                onValueChange = { playlistName = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Nueva playlist") },
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    val name = playlistName.trim()
                                    if (name.isNotEmpty()) {
                                        libraryViewModel.createLocalPlaylistWithSong(name, song)
                                        playlistName = ""
                                        showPlaylistDialog = false
                                        snackbarScope.launch {
                                            snackbarHostState.showSnackbar("Playlist creada y canción añadida")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Crear y añadir")
                            }

                            HorizontalDivider()

                            Text("Playlists locales", style = MaterialTheme.typography.labelLarge)

                            if (localPlaylists.isNotEmpty()) {
                                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                                    localPlaylists.forEach { playlist ->
                                        TextButton(
                                            onClick = {
                                                libraryViewModel.addSongToLocalPlaylist(playlist.id, song)
                                                showPlaylistDialog = false
                                                snackbarScope.launch {
                                                    snackbarHostState.showSnackbar("Añadida a ${playlist.title}")
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(playlist.title)
                                        }
                                    }
                                }
                            } else {
                                Text("No hay playlists locales todavía.")
                            }

                            TextButton(
                                onClick = {
                                    playlistName = ""
                                    showPlaylistDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cerrar")
                            }
                        }
                    }
                }
            }
        }
    }
}
