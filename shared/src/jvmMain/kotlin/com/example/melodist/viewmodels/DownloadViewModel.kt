package com.example.melodist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melodist.db.DatabaseDao
import com.example.melodist.db.entities.SongEntity
import com.example.melodist.db.entities.SongWithRelations
import com.example.melodist.player.DownloadService
import com.example.melodist.player.DownloadState
import com.metrolist.innertube.models.Album
import com.metrolist.innertube.models.Artist
import com.metrolist.innertube.models.SongItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel that exposes download state to the UI.
 * Registered as a singleton in Koin so download state is shared app-wide.
 */
class DownloadViewModel(
    private val downloadService: DownloadService,
    private val databaseDao: DatabaseDao? = null
) : ViewModel() {

    /** Per-song download states (songId → DownloadState). */
    val downloadStates: StateFlow<Map<String, DownloadState>> =
        downloadService.downloadStates
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /** Total cache size as human-readable string. */
    private val _cacheSizeText = MutableStateFlow(DownloadService.formatSize(DownloadService.getCacheSizeBytes()))
    val cacheSizeText: StateFlow<String> = _cacheSizeText.asStateFlow()

    /** Downloaded songs from DB as SongItems (for the Library Downloads tab). */
    val downloadedSongs: StateFlow<List<SongItem>> = databaseDao?.downloadedSongsWithRelations()
        ?.map { relations -> relations.map { it.toSongItem() } }
        ?.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        ?: MutableStateFlow<List<SongItem>>(emptyList())

    /** Total downloaded song count. */
    val downloadedCount: StateFlow<Int> = downloadedSongs
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    /** Songs currently being downloaded (metadata for UI display). */
    val pendingSongItems: StateFlow<Map<String, SongItem>> =
        downloadService.pendingSongItems
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /**
     * Albums that are fully downloaded (all songs present).
     * Groups downloaded songs by albumId, then checks if the count matches the album's songCount in DB.
     * Only includes albums that exist in the DB with a valid songCount > 0.
     */
    data class DownloadedAlbumInfo(
        val albumId: String,
        val albumName: String,
        val thumbnail: String?,
        val songs: List<SongItem>,
        val totalSongCount: Int
    )

    val fullyDownloadedAlbums: StateFlow<List<DownloadedAlbumInfo>> = downloadedSongs
        .map { songs ->
            val grouped = songs.filter { it.album?.id != null }.groupBy { it.album!!.id }
            grouped.mapNotNull { (albumId, albumSongs) ->
                if (albumId == null) return@mapNotNull null
                // Album MUST exist in DB with a known songCount
                val albumEntity = databaseDao?.albumById(albumId)?.firstOrNull()
                    ?: return@mapNotNull null
                val totalCount = albumEntity.songCount
                // Only include if the album has a valid songCount AND all songs are downloaded
                if (totalCount > 0 && albumSongs.size >= totalCount) {
                    DownloadedAlbumInfo(
                        albumId = albumId,
                        albumName = albumEntity.title,
                        thumbnail = albumEntity.thumbnailUrl ?: albumSongs.first().thumbnail,
                        songs = albumSongs,
                        totalSongCount = totalCount
                    )
                } else null
            }.sortedBy { it.albumName }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * Playlists that are fully downloaded (all mapped songs downloaded).
     */
    data class DownloadedPlaylistInfo(
        val playlistId: String,
        val playlistName: String,
        val thumbnail: String?,
        val downloadedSongCount: Int,
        val totalSongCount: Int
    )

    val fullyDownloadedPlaylists: StateFlow<List<DownloadedPlaylistInfo>> = downloadedSongs
        .map { dlSongs ->
            val downloadedIds = dlSongs.map { it.id }.toSet()
            val playlists = databaseDao?.allPlaylists()?.firstOrNull() ?: emptyList()
            playlists.mapNotNull { playlist ->
                val songIdsInPlaylist = databaseDao?.songIdsInPlaylist(playlist.id) ?: emptyList()
                if (songIdsInPlaylist.isEmpty()) return@mapNotNull null
                val allDownloaded = songIdsInPlaylist.all { it in downloadedIds }
                if (allDownloaded) {
                    DownloadedPlaylistInfo(
                        playlistId = playlist.browseId ?: playlist.id,
                        playlistName = playlist.name,
                        thumbnail = playlist.thumbnailUrl,
                        downloadedSongCount = songIdsInPlaylist.size,
                        totalSongCount = playlist.remoteSongCount ?: songIdsInPlaylist.size
                    )
                } else null
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())    // ─── Actions ───────────────────────────────────────────

    fun downloadSong(song: SongItem) {
        downloadService.downloadSong(song)
    }

    fun downloadAll(songs: List<SongItem>) {
        downloadService.downloadAll(songs)
    }

    fun cancelDownload(songId: String) {
        downloadService.cancelDownload(songId)
    }

    fun removeDownload(songId: String) {
        downloadService.removeDownload(songId)
        refreshCacheSize()
    }

    fun clearCache() {
        downloadService.clearCache()
        refreshCacheSize()
    }

    fun isDownloaded(songId: String): Boolean {
        return downloadService.isDownloaded(songId)
    }

    fun getState(songId: String): DownloadState? {
        return downloadStates.value[songId]
    }

    fun refreshCacheSize() {
        _cacheSizeText.value = DownloadService.formatSize(DownloadService.getCacheSizeBytes())
    }
}

/** Converts a [SongWithRelations] to a [SongItem] for UI display, including artists. */
private fun SongWithRelations.toSongItem(): SongItem = SongItem(
    id = song.id,
    title = song.title,
    artists = artists.map { Artist(name = it.name, id = it.id) },
    album = if (song.albumName != null && song.albumId != null) Album(name = song.albumName, id = song.albumId) else null,
    duration = if (song.duration > 0) song.duration else null,
    thumbnail = song.thumbnailUrl ?: "",
    explicit = song.explicit
)

/** Converts a [SongEntity] to a [SongItem] (simplified, no artists). */
private fun SongEntity.toSongItem(): SongItem = SongItem(
    id = id,
    title = title,
    artists = emptyList(),
    album = if (albumName != null && albumId != null) Album(name = albumName, id = albumId) else null,
    duration = if (duration > 0) duration else null,
    thumbnail = thumbnailUrl?: "",
    explicit = explicit
)



