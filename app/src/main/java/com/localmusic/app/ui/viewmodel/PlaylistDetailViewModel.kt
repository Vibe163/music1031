package com.localmusic.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.localmusic.app.LocalMusicApp
import com.localmusic.app.data.model.FAVORITES_PLAYLIST_ID
import com.localmusic.app.data.model.Song
import com.localmusic.app.data.model.toSong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlaylistDetailUiState(
    val playlistName: String = "",
    val songs: List<Song> = emptyList(),
    val isFavorites: Boolean = false,
    val isLoading: Boolean = true,
    /** 预设图标 key */
    val icon: String = "",
    /** 直接子歌单 */
    val children: List<com.localmusic.app.data.model.PlaylistWithCount> = emptyList()
)

class PlaylistDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app = application as LocalMusicApp
    private val repository = app.repository
    private val playlistId: Long = checkNotNull(savedStateHandle["playlistId"])

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    /** 整个曲库（供"添加歌曲到歌单"对话框列出可选歌曲）。 */
    val allSongs: StateFlow<List<Song>> = repository.observeSongs()
        .map { list -> list.map { it.toSong() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 直接子歌单（随歌单表变化实时刷新） */
    val children: StateFlow<List<com.localmusic.app.data.model.PlaylistWithCount>> =
        repository.observePlaylists()
            .map { list -> list.filter { it.parentId == playlistId }.sortedBy { it.createdAt } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (playlistId == FAVORITES_PLAYLIST_ID) {
                // 主收藏 = 红心收藏的歌曲
                repository.observeFavoriteSongs().map { list -> list.map { it.toSong() } }
                    .collect { songs ->
                        _uiState.value = PlaylistDetailUiState(
                            playlistName = "主收藏",
                            songs = songs,
                            isFavorites = true,
                            isLoading = false
                        )
                    }
            } else {
                combine(
                    combine(
                        repository.observePlaylistSongIds(playlistId),
                        repository.observeSongs()
                    ) { songIds, allSongs ->
                        val songMap = allSongs.associateBy { it.id }
                        songIds.mapNotNull { songMap[it] }.map { it.toSong() }
                    },
                    repository.observePlaylists(),
                    children
                ) { songs, allPlaylists, childPlaylists ->
                    val playlist = allPlaylists.firstOrNull { it.id == playlistId }
                    val nameIcon = (playlist?.name ?: "歌单") to (playlist?.icon ?: "")
                    Pair(nameIcon, songs) to childPlaylists
                }.collect { (nameIconAndSongs, childPlaylists) ->
                    val (nameIcon, songs) = nameIconAndSongs
                    val (name, icon) = nameIcon
                    _uiState.value = PlaylistDetailUiState(
                        playlistName = name,
                        songs = songs,
                        isFavorites = false,
                        isLoading = false,
                        icon = icon,
                        children = childPlaylists
                    )
                }
            }
        }
    }

    fun renamePlaylist(name: String) {
        if (name.isBlank() || playlistId == FAVORITES_PLAYLIST_ID) return
        viewModelScope.launch {
            repository.renamePlaylist(playlistId, name)
        }
    }

    fun updateIcon(icon: String) {
        if (playlistId == FAVORITES_PLAYLIST_ID) return
        viewModelScope.launch {
            repository.updatePlaylistIcon(playlistId, icon)
        }
    }

    /** 在当前歌单下创建子歌单 */
    fun createSubPlaylist(name: String, icon: String) {
        if (name.isBlank() || playlistId == FAVORITES_PLAYLIST_ID) return
        viewModelScope.launch {
            repository.createPlaylist(name, icon, parentId = playlistId)
        }
    }

    /** 移除歌曲：主收藏 → 取消红心收藏；用户歌单 → 仅解除关联。 */
    fun removeSong(songId: Long) {
        viewModelScope.launch {
            if (playlistId == FAVORITES_PLAYLIST_ID) {
                repository.toggleFavorite(songId, false)
            } else {
                repository.removeSongFromPlaylist(playlistId, songId)
            }
        }
    }

    /** 批量添加歌曲到当前歌单（仅用户歌单；主收藏走导入，此处忽略）。 */
    fun addSongs(songIds: List<Long>) {
        if (playlistId == FAVORITES_PLAYLIST_ID || songIds.isEmpty()) return
        viewModelScope.launch {
            repository.addSongsToPlaylist(playlistId, songIds)
        }
    }
}
