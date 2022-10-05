package com.origeek.pickerDemo

import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.accompanist.insets.statusBarsPadding
import com.origeek.imagePicker.config.ImagePickerConfig
import com.origeek.imagePicker.config.launch
import com.origeek.imagePicker.config.registerImagePicker
import com.origeek.imagePicker.ui.GridLayout
import com.origeek.imagePicker.util.rememberCoilImagePainter
import com.origeek.imagePicker.util.rememberHugeImagePainter
import com.origeek.imageViewer.ImagePreviewer
import com.origeek.imageViewer.rememberPreviewerState
import com.origeek.pickerDemo.base.BaseActivity
import com.origeek.pickerDemo.ui.theme.ImageViewerTheme
import kotlinx.coroutines.launch

const val SYSTEM_UI_VISIBILITY = "SYSTEM_UI_VISIBILITY"

class SelectorActivity : BaseActivity() {

    private var systemUIVisible = true

    private val selectedList = mutableStateListOf<String>()

    private val launcher = registerImagePicker { paths ->
        paths?.let { selectedList.addAll(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            ImageViewerTheme {
                SelectorBody(
                    list = selectedList,
                    onImageViewVisible = {
                        handlerSystemUI(!it)
                    }
                ) {
                    val config = ImagePickerConfig(
                        limit = 9,
                        navTitle = "好家伙",
                    )
                    launcher.launch(config)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(SYSTEM_UI_VISIBILITY, systemUIVisible)
        super.onSaveInstanceState(outState)
    }

    private fun handlerSystemUI(visible: Boolean) {
        systemUIVisible = visible
        if (systemUIVisible) {
            showSystemUI(window)
        } else {
            hideSystemUI(window)
        }
    }

}

@Composable
fun SelectorBody(
    list: List<String>,
    onImageViewVisible: (Boolean) -> Unit = {},
    goPicker: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val imageViewerState = rememberPreviewerState()
    LaunchedEffect(key1 = imageViewerState.visible, block = {
        onImageViewVisible(imageViewerState.visible)
    })
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .statusBarsPadding()
    ) {
        val lineCount = 4
        val p = 0.6.dp
        GridLayout(
            columns = lineCount,
            size = list.size,
        ) { index ->
            val path = list[index]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(
                        bottom = p * 2,
                        start = if (index % lineCount == 0) 0.dp else p,
                        end = if (index % lineCount == lineCount - 1) 0.dp else p
                    )
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            scope.launch {
                                imageViewerState.open(index)
                            }
                        }
                        .fillMaxSize(),
                    painter = rememberCoilImagePainter(path = path),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
            }
        }
    }
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (add) = createRefs()
        FloatingActionButton(
            onClick = {
                goPicker()
            },
            backgroundColor = MaterialTheme.colors.primary,
            modifier = Modifier.constrainAs(add) {
                end.linkTo(parent.end, 18.dp)
                linkTo(parent.top, parent.bottom, 0.dp, 0.dp, 0.72f)
            }
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
    ImagePreviewer(
        count = list.size,
        state = imageViewerState,
        imageLoader = { index ->
            rememberHugeImagePainter(path = list[index])
                ?: rememberCoilImagePainter(path = list[index])
        },
        onTap = {
            scope.launch {
                imageViewerState.close()
            }
        }
    )
}

fun hideSystemUI(window: Window) {
    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

fun showSystemUI(window: Window) {
    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).show(WindowInsetsCompat.Type.systemBars())
}