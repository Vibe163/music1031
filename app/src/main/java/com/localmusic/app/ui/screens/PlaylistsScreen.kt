package com.localmusic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localmusic.app.data.model.ALL_SONGS_PLAYLIST_ID
import com.localmusic.app.data.model.FAVORITES_PLAYLIST_ID
import com.localmusic.app.data.model.PlaylistWithCount
import com.localmusic.app.data.model.Song
import com.localmusic.app.ui.components.AlbumArt
import com.localmusic.app.ui.components.PlaylistIconDisplay

/** 树中一行歌单：附带层级信息 */
private data class PlaylistNode(
    val playlist: PlaylistWithCount,
    val depth: Int,
    val childCount: Int
)

/** 把扁平歌单列表按 parentId 展开为可见行（仅展开的分支才包含子节点） */
private fun buildVisibleTree(
    all: List<PlaylistWithCount>,
    expanded: Set<Long>
): List<PlaylistNode> {
    val user = all.filter { it.id > 0L } // 用户歌单 id 从 1 开始（0=主收藏，负数=虚拟歌单）
    val childrenMap = user.groupBy { it.parentId }
    val result = mutableListOf<PlaylistNode>()

    fun walk(parentId: Long?, depth: Int) {
        val siblings = (childrenMap[parentId] ?: emptyList()).sortedBy { it.createdAt }
        for (p in siblings) {
            val kids = childrenMap[p.id].orEmpty()
            result += PlaylistNode(p, depth, kids.size)
            if (p.id in expanded) walk(p.id, depth + 1)
        }
    }
    walk(null, 0)
    return result
}

/**
 * 歌单页 —— 「我的」风格：
 * 收藏/本地快捷入口 + 最近播放横滑 + 自建歌单列表。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    playlists: List<PlaylistWithCount>,
    recentlyPlayed: List<Song>,
    onPlaylistClick: (Long) -> Unit,
    onOpenLocalMusic: () -> Unit,
    onPlayRecent: (Int) -> Unit,
    onCreatePlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(setOf<Long>()) }

    val favorites = playlists.firstOrNull { it.id == FAVORITES_PLAYLIST_ID }
    val library = playlists.firstOrNull { it.id == ALL_SONGS_PLAYLIST_ID }
    val nodes = remember(playlists, expanded) { buildVisibleTree(playlists, expanded) }
    val userPlaylistCount = playlists.count { it.id > 0L }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("歌单") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePlaylist,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建歌单", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp)
        ) {
            // ========== 收藏 / 本地 快捷入口 ==========
            item(key = "shortcuts") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ShortcutIcon(
                        icon = Icons.Default.Favorite,
                        label = "收藏",
                        count = favorites?.songCount ?: 0,
                        onClick = { onPlaylistClick(FAVORITES_PLAYLIST_ID) },
                        modifier = Modifier.weight(1f)
                    )
                    ShortcutIcon(
                        icon = Icons.Default.Download,
                        label = "本地",
                        count = library?.songCount ?: 0,
                        onClick = onOpenLocalMusic,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ========== 最近播放 ==========
            if (recentlyPlayed.isNotEmpty()) {
                item(key = "recent_header") {
                    SectionHeader(
                        title = "最近播放",
                        count = recentlyPlayed.size,
                        modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
                    )
                }
                item(key = "recent_row") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        itemsIndexed(
                            recentlyPlayed,
                            key = { _, s -> "recent_${s.id}" }
                        ) { index, song ->
                            RecentSongCard(
                                song = song,
                                onClick = { onPlayRecent(index) }
                            )
                        }
                    }
                }
            }

            // ========== 自建歌单 ==========
            item(key = "user_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "自建歌单",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (userPlaylistCount > 0) {
                        Text(
                            text = " $userPlaylistCount",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = onCreatePlaylist,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "新建歌单",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (nodes.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp, bottom = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "还没有歌单，点右下角创建",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(nodes, key = { it.playlist.id }) { node ->
                    UserPlaylistRow(
                        node = node,
                        expanded = node.playlist.id in expanded,
                        onToggleExpand = {
                            expanded = if (node.playlist.id in expanded) expanded - node.playlist.id
                            else expanded + node.playlist.id
                        },
                        onClick = { onPlaylistClick(node.playlist.id) }
                    )
                }
            }
        }
    }
}

/** 收藏/本地 圆形图标入口 */
@Composable
private fun ShortcutIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 分区标题：名称 + 数量 */
@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 最近播放卡片：封面 + 标题 */
@Composable
private fun RecentSongCard(
    song: Song,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(96.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AlbumArt(
            albumArtPath = song.albumArtPath,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(10.dp)),
            cornerRadius = 10.dp
        )
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** 自建歌单行：缩略图/图标 + 名称 + 首数，支持子歌单展开 */
@Composable
private fun UserPlaylistRow(
    node: PlaylistNode,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onClick: () -> Unit
) {
    val playlist = node.playlist
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(start = (node.depth * 20).dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 有子歌单：展开/收起箭头
        if (node.childCount > 0) {
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = if (expanded) "收起" else "展开",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onToggleExpand)
            )
        } else {
            Spacer(Modifier.width(8.dp))
        }
        PlaylistIconDisplay(
            iconKey = playlist.icon,
            size = 48.dp,
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val sub = if (node.childCount > 0) " · ${node.childCount} 个子歌单" else ""
            Text(
                text = "${playlist.songCount}首$sub",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
