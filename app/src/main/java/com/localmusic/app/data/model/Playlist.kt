package com.localmusic.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    /** 预设图标 key（见 PlaylistIcons），空串 = 默认音乐图标 */
    val icon: String = "",
    /** 父歌单 id；null = 根级歌单（支持多级子歌单，类似文件夹） */
    val parentId: Long? = null
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSong(
    val playlistId: Long,
    val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)
