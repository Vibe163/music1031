package com.localmusic.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * 专辑封面：圆角裁剪 + crossfade + 自适应占位音符（约容器 45%）。
 */
@Composable
fun AlbumArt(
    albumArtPath: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    val shape = RoundedCornerShape(cornerRadius)
    val colorScheme = MaterialTheme.colorScheme
    val brush = remember(colorScheme) {
        Brush.linearGradient(
            listOf(colorScheme.primaryContainer, colorScheme.surfaceVariant)
        )
    }
    val context = LocalContext.current
    var loadFailed by remember(albumArtPath) { mutableStateOf(false) }
    val showArt = !albumArtPath.isNullOrBlank() && !loadFailed

    BoxWithConstraints(
        modifier = modifier
            .clip(shape)
            .background(brush),
        contentAlignment = Alignment.Center
    ) {
        if (showArt) {
            val request = remember(albumArtPath) {
                ImageRequest.Builder(context)
                    .data(albumArtPath)
                    .crossfade(220)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = { loadFailed = true },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val iconSize = (minOf(maxWidth, maxHeight) * 0.45f).coerceAtLeast(12.dp)
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = colorScheme.primary.copy(alpha = 0.75f),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
