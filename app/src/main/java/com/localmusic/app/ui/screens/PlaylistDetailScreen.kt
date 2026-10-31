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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.localmusic.app.ui.components.CreatePlaylistDialog
import com.localmusic.app.ui.components.PlaylistIconDisplay
import com.localmusic.app.ui.components.PlaylistIcons
import com.localmusic.app.ui.components.RenamePlaylistDialog
import com.localmusic.app.ui.viewmodel.PlaylistDetailViewModel

private val ScreenBg = Color(0xFF0C0C0F)
private val SurfaceGray = Color(0xFF1C1C21)
private val TextSecondary = Color(0xFF9A9AA0)
private val AccentGold = Color(0xFFE8C766)

/**
 * 歌单详情页 —— 深色沉浸式头图风格
 *
 * 顶部大图（自定义图标或渐变兜底）+ 标题叠图 + 操作按钮行 + 编号歌曲列表。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    viewModel: PlaylistDetailViewModel,
    onBack: () -> Unit,
    onSongClick: (Int) -> Unit,
    onEditSong: (com.localmusic.app.data.model.Song) -> Unit,
    onToggleFavorite: (com.localmusic.app.data.model.Song) -> Unit,
    onShareSong: (com.localmusic.app.data.model.Song) -> Unit,
    onPlayNextSong: (com.localmusic.app.data.model.Song) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onAddSongs: () -> Unit,
    onDeletePlaylist: () -> Unit,
    onChildPlaylistClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showCreateSub by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = ScreenBg,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (!uiState.isFavorites) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "更多", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("编辑歌单") },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        showRenameDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("新建子歌单") },
                                    leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        showCreateSub = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("删除歌单") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onDeletePlaylist()
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ========== 头图区 ==========
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    // 背景：自定义图标铺满 / 否则深紫渐变兜底
                    if (PlaylistIcons.isCustomImage(uiState.icon)) {
                        AsyncImage(
                            model = uiState.icon,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF4A2A6B),
                                            Color(0xFF1A1030),
                                            ScreenBg
                                        )
                                    )
                                )
                        )
                    }
                    // 底部渐变遮罩 → 融入黑底
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xCC0C0C0F),
                                        ScreenBg
                                    )
                                )
                            )
                    )

                    // 标题 + 元信息（叠在图底部）
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = uiState.playlistName,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PlaylistIconDisplay(
                                iconKey = uiState.icon,
                                size = 22.dp,
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "本地歌单",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${uiState.songs.size}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // ========== 四个操作按钮 ==========
            if (!uiState.isFavorites) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionPill(Icons.Default.PlayArrow, "播放全部", Modifier.weight(1f), onClick = onPlayAll, enabled = uiState.songs.isNotEmpty())
                        ActionPill(Icons.Default.Shuffle, "随机", Modifier.weight(1f), onClick = onShuffle, enabled = uiState.songs.isNotEmpty())
                        ActionPill(Icons.Default.Add, "加歌", Modifier.weight(1f), onClick = onAddSongs, enabled = true)
                        ActionPill(Icons.Default.CreateNewFolder, "子歌单", Modifier.weight(1f), onClick = { showCreateSub = true }, enabled = true)
                    }
                }
            }

            // ========== 全部播放行 ==========
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentGold)
                            .clickable(enabled = uiState.songs.isNotEmpty()) { onPlayAll() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "全部播放",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "全部播放",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = " ${uiState.songs.size}",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // ========== 子歌单区 ==========
            if (uiState.children.isNotEmpty()) {
                item {
                    Text(
                        text = "子歌单",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                    )
                }
                items(uiState.children, key = { "child_${it.id}" }) { child ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChildPlaylistClick(child.id) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PlaylistIconDisplay(
                            iconKey = child.icon,
                            size = 40.dp,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = child.name,
                                color = Color.White,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${child.songCount} 首",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                }
            }

            // ========== 空态 ==========
            if (uiState.songs.isEmpty() && uiState.children.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp, bottom = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (uiState.isFavorites) "还没有收藏的歌曲" else "歌单还是空的",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 15.sp
                            )
                            if (!uiState.isFavorites) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "点上方「加歌」从曲库添加",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // ========== 歌曲列表（编号样式） ==========
            itemsIndexed(
                uiState.songs,
                key = { _, s -> "song_${s.id}" },
                contentType = { _, _ -> "song" }
            ) { index, song ->
                var menuOpen by remember { mutableStateOf(false) }
                SongRow(
                    index = index + 1,
                    title = song.title,
                    subtitle = "${song.artist} - ${song.album}".trim(' ', '-'),
                    onClick = { onSongClick(index) },
                    menuExpanded = menuOpen,
                    onMenuToggle = { menuOpen = it },
                    onPlay = { onSongClick(index) },
                    onPlayNext = { onPlayNextSong(song) },
                    onShare = { onShareSong(song) },
                    onEdit = { onEditSong(song) },
                    onRemove = { viewModel.removeSong(song.id) }
                )
            }
        }
    }

    if (showRenameDialog) {
        RenamePlaylistDialog(
            initialName = uiState.playlistName,
            initialIcon = uiState.icon,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName, newIcon ->
                viewModel.renamePlaylist(newName)
                viewModel.updateIcon(newIcon)
                showRenameDialog = false
            }
        )
    }

    if (showCreateSub) {
        CreatePlaylistDialog(
            onDismiss = { showCreateSub = false },
            onConfirm = { name, icon ->
                viewModel.createSubPlaylist(name, icon)
                showCreateSub = false
            },
            parentName = uiState.playlistName
        )
    }
}

/** 圆角胶囊操作按钮（截图中头图下方一排） */
@Composable
private fun ActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceGray)
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (enabled) Color.White else Color.White.copy(alpha = 0.35f),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = if (enabled) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.35f),
            fontSize = 11.sp
        )
    }
}

/** 编号歌曲行：序号 + 标题 + 艺术家-专辑 + ⋮ 菜单 */
@Composable
private fun SongRow(
    index: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    menuExpanded: Boolean,
    onMenuToggle: (Boolean) -> Unit,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.width(28.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank() && subtitle != " - ") {
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Box {
            IconButton(onClick = { onMenuToggle(true) }, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "更多",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { onMenuToggle(false) }
            ) {
                DropdownMenuItem(text = { Text("播放") }, onClick = { onMenuToggle(false); onPlay() })
                DropdownMenuItem(text = { Text("下一首播放") }, onClick = { onMenuToggle(false); onPlayNext() })
                DropdownMenuItem(text = { Text("分享") }, onClick = { onMenuToggle(false); onShare() })
                DropdownMenuItem(text = { Text("编辑信息") }, onClick = { onMenuToggle(false); onEdit() })
                DropdownMenuItem(
                    text = { Text("移除", color = MaterialTheme.colorScheme.error) },
                    onClick = { onMenuToggle(false); onRemove() }
                )
            }
        }
    }
}
