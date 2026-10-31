package com.localmusic.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.localmusic.app.data.migration.ImportSummary
import com.localmusic.app.data.migration.PlaylistImportResult
import com.localmusic.app.data.model.PlaylistWithCount
import com.localmusic.app.data.model.Song

/**
 * 歌单图标选择区：仅支持相册选图。
 * 选图后弹全屏裁剪页，完成后 icon 更新为 file:// 路径。
 */
@Composable
private fun PlaylistIconSection(
    icon: String,
    onIconChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var cropUri by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) cropUri = uri }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 当前图标预览 + 相册选图按钮
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (PlaylistIcons.isCustomImage(icon)) {
                    AsyncImage(
                        model = icon,
                        contentDescription = "当前图标",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = if (PlaylistIcons.isCustomImage(icon)) "已选择图标" else "从相册选择图标",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Button(
                onClick = {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(if (PlaylistIcons.isCustomImage(icon)) "更换" else "选择", modifier = Modifier.padding(start = 4.dp))
            }
        }
    }

    cropUri?.let { uri ->
        PlaylistIconCropDialog(
            imageUri = uri,
            onCancel = { cropUri = null },
            onConfirm = { path ->
                onIconChange(path)
                cropUri = null
            }
        )
    }
}

/**
 * 新建歌单对话框：输入名称 + 选择图标。
 * @param parentName 非空时表示在该歌单下创建子歌单
 * @param onConfirm 返回 (名称, 图标 key 或 file:// 路径)
 */
@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    parentName: String? = null
) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (parentName != null) "新建子歌单" else "新建歌单") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (parentName != null) {
                    Text(
                        text = "位于「$parentName」下",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("歌单名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                PlaylistIconSection(
                    icon = icon,
                    onIconChange = { icon = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), icon) },
                enabled = name.isNotBlank()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/** 编辑歌单对话框：改名 + 换图标（预设或相册图）。 */
@Composable
fun RenamePlaylistDialog(
    initialName: String,
    initialIcon: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var icon by remember(initialIcon) { mutableStateOf(initialIcon) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑歌单") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("歌单名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                PlaylistIconSection(
                    icon = icon,
                    onIconChange = { icon = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), icon) },
                enabled = name.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun AddToPlaylistDialog(
    playlists: List<PlaylistWithCount>,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit
) {
    // 仅展示用户自建歌单（主收藏是自动的）
    val userPlaylists = playlists.filter { !it.isBuiltIn }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加到歌单") },
        text = {
            if (userPlaylists.isEmpty()) {
                Text("还没有自建歌单，请先在「歌单」页创建一个")
            } else {
                LazyColumn {
                    items(userPlaylists, key = { it.id }) { playlist ->
                        Text(
                            text = "${playlist.name} (${playlist.songCount})",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(playlist.id) }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 从曲库多选歌曲添加到指定歌单。
 * @param allSongs 曲库全部歌曲
 * @param existingIds 该歌单已包含的歌曲 id（默认勾选/置灰，避免重复添加）
 * @param onConfirm 返回新勾选的歌曲 id 列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongsToPlaylistDialog(
    allSongs: List<Song>,
    existingIds: Set<Long>,
    onConfirm: (List<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // 用 stateMapOf 让单个 key 变更只重组对应 item
    val checked = remember { mutableStateMapOf<Long, Boolean>() }
    LaunchedEffect(allSongs) {
        allSongs.forEach { song ->
            if (checked[song.id] == null) checked[song.id] = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("添加歌曲到歌单")
                val selectedCount = checked.count { it.value }
                Button(
                    onClick = {
                        onConfirm(checked.filter { it.value }.keys.toList())
                    },
                    enabled = selectedCount > 0
                ) {
                    Text("添加${if (selectedCount > 0) " ($selectedCount)" else ""}")
                }
            }

            if (allSongs.isEmpty()) {
                Text(
                    text = "曲库为空，请先导入音乐",
                    modifier = Modifier.padding(24.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                    items(allSongs, key = { it.id }) { song ->
                        val alreadyIn = song.id in existingIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !alreadyIn) {
                                    checked[song.id] = !(checked[song.id] ?: false)
                                }
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = if (alreadyIn) true else (checked[song.id] ?: false),
                                onCheckedChange = if (alreadyIn) null else {
                                    { checked[song.id] = it }
                                }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = if (alreadyIn) "已在歌单中" else song.artist,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 编辑歌曲信息对话框——直接修改音乐文件本身的元数据（ID3/Vorbis tag），不只前端显示。
 * @param song 要编辑的歌曲
 * @param saving 是否正在写入文件
 * @param onConfirm 返回新的 title/artist/album，调用方负责调 ViewModel 写入
 */
@Composable
fun EditSongInfoDialog(
    song: Song,
    saving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, artist: String, album: String) -> Unit
) {
    var title by remember(song.id, song.title, song.artist, song.album) { mutableStateOf(song.title) }
    var artist by remember(song.id, song.artist) { mutableStateOf(song.artist) }
    var album by remember(song.id, song.album) { mutableStateOf(song.album) }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("编辑歌曲信息") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    singleLine = true,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("艺术家") },
                    singleLine = true,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("专辑") },
                    singleLine = true,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                )
                if (saving) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title.trim(), artist.trim(), album.trim()) },
                enabled = !saving && title.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) {
                Text("取消")
            }
        }
    )
}

/**
 * 导入结果对话框：展示每个歌单的匹配情况。
 * @param summary 导入结果摘要
 * @param onDismiss 关闭对话框
 */
@Composable
fun ImportResultDialog(
    summary: ImportSummary,
    onDismiss: () -> Unit
) {
    val totalMatched = summary.playlistResults.sumOf { it.matchedSongs }
    val totalUnmatched = summary.playlistResults.sumOf { it.unmatchedSongs }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入完成") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 总览
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "共 ${summary.totalPlaylists} 个歌单",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "成功匹配 $totalMatched 首，未匹配 $totalUnmatched 首",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 每个歌单的详情
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(summary.playlistResults) { result ->
                        PlaylistImportItem(result)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("知道了")
            }
        }
    )
}

@Composable
private fun PlaylistImportItem(result: PlaylistImportResult) {
    val progress = if (result.totalSongs > 0) {
        result.matchedSongs.toFloat() / result.totalSongs.toFloat()
    } else {
        1f
    }
    val allMatched = result.unmatchedSongs == 0 && result.totalSongs > 0
    
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.playlistName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (allMatched) "✓ 全部匹配" else "${result.matchedSongs}/${result.totalSongs}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (allMatched) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = if (allMatched) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            if (result.unmatchedSongs > 0 && result.unmatchedSongNames.isNotEmpty()) {
                Text(
                    text = "未匹配歌曲：",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                result.unmatchedSongNames.take(3).forEach { name ->
                    Text(
                        text = "• $name",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (result.unmatchedSongNames.size > 3) {
                    Text(
                        text = "• 还有 ${result.unmatchedSongNames.size - 3} 首...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
