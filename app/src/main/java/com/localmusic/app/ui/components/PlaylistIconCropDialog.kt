package com.localmusic.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * 歌单图标裁剪页（仿系统相册/抖音裁剪 UI）
 *
 * 从相册选图后全屏进入：方形裁剪框 + 拖动/双指缩放 + 底部放大缩小/旋转按钮，
 * 「完成」后按框选区域裁出 256×256 JPEG 存入 filesDir/playlist_icons/，
 * 返回 file:// URI 字符串写入歌单 icon 字段。
 */
@Composable
fun PlaylistIconCropDialog(
    imageUri: Uri,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUri) {
        bitmap = withContext(Dispatchers.IO) { decodeSampled(context, imageUri, 2048) }
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val bmp = bitmap
            if (bmp == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            } else {
                CropContent(
                    context = context,
                    bitmap = bmp,
                    onCancel = onCancel,
                    onConfirm = onConfirm
                )
            }
        }
    }
}

@Composable
private fun CropContent(
    context: Context,
    bitmap: Bitmap,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }

    // 当前展示的 bitmap（旋转按钮会原地旋转它）
    var workBitmap by remember(bitmap) { mutableStateOf(bitmap) }
    var rotation by remember(bitmap) { mutableIntStateOf(0) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 裁剪框：屏宽 80% 的正方形
        val boxSize = maxWidth * 0.8f
        val boxPx = with(density) { boxSize.toPx() }

        val w = workBitmap.width.toFloat()
        val h = workBitmap.height.toFloat()
        val fitScale = min(boxPx / w, boxPx / h)

        var scale by remember(workBitmap) { mutableFloatStateOf(fitScale) }
        var offsetX by remember(workBitmap) { mutableFloatStateOf(0f) }
        var offsetY by remember(workBitmap) { mutableFloatStateOf(0f) }

        // 图片层（拖动 + 双指缩放）
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(boxSize)
                .clipToBounds()
                .pointerInput(workBitmap) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val oldScale = scale
                        val newScale = (oldScale * zoom).coerceIn(fitScale, fitScale * 6f)
                        scale = newScale
                        offsetX = offsetX * (newScale / oldScale) + pan.x
                        offsetY = offsetY * (newScale / oldScale) + pan.y
                        val maxOx = max(0f, (w * newScale - boxPx) / 2f)
                        val maxOy = max(0f, (h * newScale - boxPx) / 2f)
                        offsetX = offsetX.coerceIn(-maxOx, maxOx)
                        offsetY = offsetY.coerceIn(-maxOy, maxOy)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawImage(
                    image = workBitmap.asImageBitmap(),
                    dstOffset = IntOffset(
                        x = ((boxPx - w * scale) / 2f + offsetX).toInt(),
                        y = ((boxPx - h * scale) / 2f + offsetY).toInt()
                    ),
                    dstSize = IntSize(
                        width = (w * scale).toInt(),
                        height = (h * scale).toInt()
                    )
                )
            }
        }

        // 遮罩 + 裁剪框边框 + 九宫格线
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cw = size.width
            val ch = size.height
            val left = (cw - boxPx) / 2f
            val top = (ch - boxPx) / 2f
            val right = left + boxPx
            val bottom = top + boxPx
            val mask = Color(0xAA000000)

            drawRect(mask, topLeft = Offset(0f, 0f), size = Size(cw, top))
            drawRect(mask, topLeft = Offset(0f, bottom), size = Size(cw, ch - bottom))
            drawRect(mask, topLeft = Offset(0f, top), size = Size(left, boxPx))
            drawRect(mask, topLeft = Offset(right, top), size = Size(cw - right, boxPx))

            drawRect(
                color = Color.White,
                topLeft = Offset(left, top),
                size = Size(boxPx, boxPx),
                style = Stroke(width = 2.dp.toPx())
            )
            val line = Color.White.copy(alpha = 0.45f)
            for (i in 1..2) {
                val x = left + boxPx * i / 3f
                val y = top + boxPx * i / 3f
                drawLine(line, Offset(x, top), Offset(x, bottom), strokeWidth = 1.dp.toPx())
                drawLine(line, Offset(left, y), Offset(right, y), strokeWidth = 1.dp.toPx())
            }
        }

        // 顶部：返回 / 标题 / 完成
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onCancel() }
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "返回", tint = Color.White, modifier = Modifier.size(22.dp))
                Text("返回", color = Color.White, fontSize = 15.sp, modifier = Modifier.padding(start = 4.dp))
            }
            Text(
                text = "移动缩放，框选图标区域",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(enabled = !saving) {
                        if (saving) return@clickable
                        saving = true
                        scope.launch {
                            val path = saveCrop(context, workBitmap, w, h, boxPx, scale, offsetX, offsetY)
                            saving = false
                            if (path != null) onConfirm(path)
                        }
                    }
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "完成", tint = Color(0xFFFE2C55), modifier = Modifier.size(22.dp))
                Text("完成", color = Color(0xFFFE2C55), fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }
        }

        // 底部工具栏：放大 缩小 | 左旋 右旋（仿截图）
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolIcon(Icons.Default.ZoomIn, "放大") {
                val newScale = (scale * 1.25f).coerceAtMost(fitScale * 6f)
                val ratio = newScale / scale
                scale = newScale
                offsetX *= ratio
                offsetY *= ratio
                val maxOx = max(0f, (w * newScale - boxPx) / 2f)
                val maxOy = max(0f, (h * newScale - boxPx) / 2f)
                offsetX = offsetX.coerceIn(-maxOx, maxOx)
                offsetY = offsetY.coerceIn(-maxOy, maxOy)
            }
            ToolDivider()
            ToolIcon(Icons.Default.ZoomOut, "缩小") {
                val newScale = (scale / 1.25f).coerceAtLeast(fitScale)
                val ratio = newScale / scale
                scale = newScale
                offsetX *= ratio
                offsetY *= ratio
                val maxOx = max(0f, (w * newScale - boxPx) / 2f)
                val maxOy = max(0f, (h * newScale - boxPx) / 2f)
                offsetX = offsetX.coerceIn(-maxOx, maxOx)
                offsetY = offsetY.coerceIn(-maxOy, maxOy)
            }
            ToolDivider()
            ToolIcon(Icons.Default.RotateLeft, "左旋") {
                workBitmap = rotateBitmap(workBitmap, -90)
                rotation = (rotation - 90) % 360
            }
            ToolDivider()
            ToolIcon(Icons.Default.RotateRight, "右旋") {
                workBitmap = rotateBitmap(workBitmap, 90)
                rotation = (rotation + 90) % 360
            }
        }
    }
}

@Composable
private fun ToolIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    cd: String,
    onClick: () -> Unit
) {
    Icon(
        imageVector = imageVector,
        contentDescription = cd,
        tint = Color.White,
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick)
            .padding(10.dp)
    )
}

@Composable
private fun ToolDivider() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .width(1.dp)
            .size(24.dp)
            .background(Color.White.copy(alpha = 0.3f))
    )
}

/** 旋转位图 90° 倍数 */
private fun rotateBitmap(src: Bitmap, degrees: Int): Bitmap {
    val m = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
        .also { if (it !== src) src.recycle() }
}

/** 按裁剪框区域从原图裁出 256×256 JPEG，存 filesDir/playlist_icons/，返回 file:// URI */
private suspend fun saveCrop(
    context: Context,
    bitmap: Bitmap,
    w: Float,
    h: Float,
    boxPx: Float,
    scale: Float,
    offsetX: Float,
    offsetY: Float
): String? = withContext(Dispatchers.IO) {
    runCatching {
        val cropCenterX = (w / 2f) - (offsetX / scale)
        val cropCenterY = (h / 2f) - (offsetY / scale)
        val cropSizePx = boxPx / scale
        val half = cropSizePx / 2f

        val sx = (cropCenterX - half).coerceIn(0f, max(0f, w - cropSizePx))
        val sy = (cropCenterY - half).coerceIn(0f, max(0f, h - cropSizePx))
        val cw = cropSizePx.toInt().coerceIn(1, bitmap.width)
        val ch = cropSizePx.toInt().coerceIn(1, bitmap.height)
        val cx = sx.toInt().coerceIn(0, bitmap.width - cw)
        val cy = sy.toInt().coerceIn(0, bitmap.height - ch)

        val cropped = Bitmap.createBitmap(bitmap, cx, cy, cw, ch)
        val out = Bitmap.createScaledBitmap(cropped, 256, 256, true)
        if (cropped !== out) cropped.recycle()

        val dir = File(context.filesDir, "playlist_icons").apply { mkdirs() }
        val file = File(dir, "icon_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { fos ->
            out.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }
        out.recycle()
        Uri.fromFile(file).toString()
    }.getOrNull()
}

/** 采样解码，限制最大边 ≤ maxSize，避免大图 OOM */
private fun decodeSampled(context: Context, uri: Uri, maxSize: Int): Bitmap? = runCatching {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

    var sample = 1
    while (bounds.outWidth / sample > maxSize || bounds.outHeight / sample > maxSize) {
        sample *= 2
    }
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
}.getOrNull()
