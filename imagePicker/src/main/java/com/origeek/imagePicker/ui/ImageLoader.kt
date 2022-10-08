package com.origeek.imagePicker.ui

import android.graphics.BitmapRegionDecoder
import android.os.Build.VERSION.SDK_INT
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.origeek.imagePicker.R
import com.origeek.imagePicker.util.ContextUtil
import com.origeek.imageViewer.ImageDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 图片加载器
val imageLoader = ImageLoader.Builder(ContextUtil.getApplicationByReflect())
    .components {
        // 增加gif的支持
        if (SDK_INT >= 28) {
            add(ImageDecoderDecoder.Factory())
        } else {
            add(GifDecoder.Factory())
        }
        // 增加svg的支持
        add(SvgDecoder.Factory())
    }
    .error(R.drawable.ic_error)
    .build()

@Composable
fun rememberCoilImagePainter(
    path: String,
    size: Size = Size.ORIGINAL
): Painter {
    // 加载图片
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(path)
        .size(size)
        .build()
    // 获取图片的初始大小
    return rememberAsyncImagePainter(model = imageRequest, imageLoader = imageLoader)
}


@Composable
fun rememberHugeImagePainter(path: String): Any? {
    val defaultPainter = painterResource(id = R.drawable.ic_empty_image)
    var painter by remember { mutableStateOf<Any?>(defaultPainter) }
    LaunchedEffect(path) {
        launch(Dispatchers.IO) {
            val imageDecoder = try {
                val decoder = BitmapRegionDecoder.newInstance(path, false)
                ImageDecoder(decoder = decoder)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            launch(Dispatchers.Main) {
                painter = imageDecoder
            }
        }
    }
    return painter
}