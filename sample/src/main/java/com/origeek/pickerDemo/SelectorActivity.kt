package com.origeek.pickerDemo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.accompanist.insets.statusBarsPadding
import com.origeek.imagePicker.config.ImagePickerConfig
import com.origeek.imagePicker.config.registerImagePicker
import com.origeek.imagePicker.ui.rememberCoilImagePainter
import com.origeek.imagePicker.ui.rememberHugeImagePainter
import com.origeek.imageViewer.previewer.ImagePreviewer
import com.origeek.imageViewer.previewer.TransformImageView
import com.origeek.imageViewer.previewer.rememberPreviewerState
import com.origeek.imageViewer.previewer.rememberTransformItemState
import com.origeek.pickerDemo.base.BaseActivity
import com.origeek.ui.common.compose.LazyGridLayout
import com.origeek.ui.common.compose.ScaleGrid
import com.origeek.ui.common.util.findWindow
import com.origeek.ui.common.util.hideSystemUI
import com.origeek.ui.common.util.showSystemUI
import kotlinx.coroutines.launch
import java.util.*
import java.util.stream.Collectors

data class SelectedImage(
    val id: String,
    val path: String,
)

class SelectorActivity : BaseActivity() {

    private val viewModel by viewModels<SelectorViewModel>()

    private val launcher = registerImagePicker { paths ->
        paths?.let {
            val resultList = paths.stream().map {
                SelectedImage(
                    id = UUID.randomUUID().toString(),
                    path = it,
                )
            }.collect(Collectors.toList())
            viewModel.addList(resultList)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBasicContent {
            SelectorBody(
                list = viewModel.selectedList,
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

val backgroundColor = Color(0xFFF4F4F4)
val gridMaskerColor = Color(0x0F000000)

@Composable
fun SelectorBody(
    list: List<SelectedImage>,
    goPicker: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val imageViewerState = rememberPreviewerState()
    val getKey: () -> Any = { list[imageViewerState.currentPage].id }
    imageViewerState.enableVerticalDrag { getKey() }
    val window = LocalContext.current.findWindow()
    LaunchedEffect(key1 = imageViewerState.visibleTarget, block = {
        if (window != null) {
            if (imageViewerState.visibleTarget == true) {
                hideSystemUI(window)
            } else if (imageViewerState.visibleTarget == false) {
                showSystemUI(window)
            }
        }
    })
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .systemBarsPadding()
            .statusBarsPadding()
    ) {
        val lineCount = if (maxWidth > maxHeight) 8 else 4
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(gridMaskerColor)
                )
            }
        }
    }
    if (list.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "🙌 空空如也")
        }
    }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
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
        },
        detectGesture = {
            onTap = {
                scope.launch {
                    imageViewerState.closeTransform(key = getKey())
                }
            }
        },
    )
}