package com.origeek.pickerDemo

import android.os.Bundle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.accompanist.insets.statusBarsPadding
import com.origeek.imagePicker.config.ImagePickerConfig
import com.origeek.imagePicker.config.registerImagePicker
import com.origeek.imagePicker.ui.rememberCoilImagePainter
import com.origeek.imagePicker.ui.rememberHugeImagePainter
import com.origeek.imagePicker.util.hideSystemUI
import com.origeek.imagePicker.util.showSystemUI
import com.origeek.imageViewer.ImagePreviewer
import com.origeek.imageViewer.TransformImageView
import com.origeek.imageViewer.rememberPreviewerState
import com.origeek.imageViewer.rememberTransformItemState
import com.origeek.pickerDemo.base.BaseActivity
import com.origeek.ui.common.LazyGridLayout
import com.origeek.ui.common.ScaleGrid
import kotlinx.coroutines.launch
import java.util.*
import java.util.stream.Collectors

const val SYSTEM_UI_VISIBILITY = "SYSTEM_UI_VISIBILITY"

data class SelectedImage(
    val id: String,
    val path: String,
)

class SelectorActivity : BaseActivity() {

    private var systemUIVisible = true

    private val selectedList = mutableStateListOf<SelectedImage>()

    private val launcher = registerImagePicker { paths ->
        paths?.let {
            selectedList.addAll(paths.stream().map {
                SelectedImage(
                    id = UUID.randomUUID().toString(),
                    path = it,
                )
            }.collect(Collectors.toList()))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
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
    list: List<SelectedImage>,
    onImageViewVisible: (Boolean) -> Unit = {},
    goPicker: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val imageViewerState = rememberPreviewerState()
    val getKey: () -> Any = { list[imageViewerState.currentPage].id }
    imageViewerState.enableVerticalDrag { getKey() }
    LaunchedEffect(key1 = imageViewerState.visibleTarget, block = {
        if (imageViewerState.visibleTarget != null) {
            onImageViewVisible(imageViewerState.visibleTarget ?: imageViewerState.visible)
        }
    })
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .statusBarsPadding()
    ) {
        val lineCount = 4
        val p = 0.6.dp
        LazyGridLayout(
            modifier = Modifier.fillMaxSize(),
            columns = lineCount,
            size = list.size,
            padding = p,
        ) { index ->
            val selectedImage = list[index]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                val itemState = rememberTransformItemState()
                ScaleGrid(onTap = {
                    scope.launch {
                        imageViewerState.openTransform(index, itemState)
                    }
                }) {
                    TransformImageView(
                        itemState = itemState,
                        previewerState = imageViewerState,
                        painter = rememberCoilImagePainter(path = selectedImage.path),
                        key = selectedImage.id
                    )
                }
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
            val path = list[index].path
            rememberHugeImagePainter(path = path)
                ?: rememberCoilImagePainter(path = path)
        },
        onTap = {
            scope.launch {
                imageViewerState.closeTransform(key = getKey())
            }
        }
    )
}