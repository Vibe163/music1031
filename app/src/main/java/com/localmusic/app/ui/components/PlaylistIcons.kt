package com.localmusic.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage

/** 歌单预设图标：key → ImageVector。空 key / 未知 key 回退为默认音乐图标。 */
object PlaylistIcons {

    val presets: List<Pair<String, ImageVector>> = listOf(
        "music" to Icons.AutoMirrored.Filled.QueueMusic,
        "favorite" to Icons.Default.Favorite,
        "star" to Icons.Default.Star,
        "folder" to Icons.Default.Folder,
        "headphones" to Icons.Default.Headphones,
        "mic" to Icons.Default.Mic,
        "album" to Icons.Default.Album,
        "fitness" to Icons.Default.FitnessCenter,
        "car" to Icons.Default.DirectionsCar,
        "work" to Icons.Default.Work,
        "book" to Icons.Default.MenuBook,
        "fire" to Icons.Default.LocalFireDepartment,
        "beach" to Icons.Default.BeachAccess,
        "school" to Icons.Default.School
    )

    private val labels = mapOf(
        "music" to "音乐", "favorite" to "收藏", "star" to "星标", "folder" to "文件夹",
        "headphones" to "耳机", "mic" to "麦克风", "album" to "专辑", "fitness" to "健身",
        "car" to "驾驶", "work" to "工作", "book" to "阅读", "fire" to "热门",
        "beach" to "旅行", "school" to "学习"
    )

    fun fromKey(key: String): ImageVector =
        presets.firstOrNull { it.first == key }?.second
            ?: Icons.AutoMirrored.Filled.QueueMusic

    fun label(key: String): String = labels[key] ?: "音乐"

    /** 是否为自定义相册图片图标（file:// 路径）而非预设矢量图标 */
    fun isCustomImage(key: String): Boolean =
        key.startsWith("file://") || key.startsWith("content://")
}

/**
 * 歌单图标统一显示：自定义相册图用 Coil 加载（圆形裁剪），否则显示预设矢量图标。
 */
@Composable
fun PlaylistIconDisplay(
    iconKey: String,
    size: Dp,
    tint: Color,
    modifier: Modifier = Modifier
) {
    if (PlaylistIcons.isCustomImage(iconKey)) {
        AsyncImage(
            model = iconKey,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        Icon(
            imageVector = PlaylistIcons.fromKey(iconKey),
            contentDescription = null,
            tint = tint,
            modifier = modifier.size(size)
        )
    }
}
