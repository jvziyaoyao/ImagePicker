package com.origeek.imagePicker.ui

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.size.Size
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.hjq.permissions.XXPermissions
import com.origeek.imagePicker.config.ImagePickerConfig
import com.origeek.imagePicker.config.NO_LIMIT
import com.origeek.imagePicker.model.AlbumEntity
import com.origeek.imagePicker.model.PhotoQueryEntity
import com.origeek.imagePicker.util.*
import com.origeek.imagePicker.vm.PickerViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import kotlin.math.ceil

// 需要权限
val permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)

enum class DecoderMineType(val mimeType: String) {
    JPEG("image/jpeg"),
    PNG("image/png"),
    WEBP("image/webp"),
    SVG("image/svg+xml"),
    ;
}

@SuppressLint("CompositionLocalNaming")
val ConfigContent = compositionLocalOf { ImagePickerConfig() }

@Composable
fun PickerBody(
    viewModel: PickerViewModel,
    config: ImagePickerConfig,
    onBack: () -> Unit,
    commit: () -> Unit,
) {
    CompositionLocalProvider(ConfigContent provides config) {
        ProvideWindowInsets {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = MaterialTheme.colors.isLight
            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = useDarkIcons
                )
            }
            PickerPermissions({
                if (it && viewModel.albumList.isNullOrEmpty()) viewModel.initial()
            }) {
                PickerContent(
                    albums = viewModel.albumList,
                    checkList = viewModel.checkList,
                    albumsLoading = viewModel.loading,
                    selectedAlbumIndex = viewModel.selectedAlbumIndex,
                    limit = viewModel.pickerConfig?.limit ?: NO_LIMIT,
                    filled = viewModel.checkFilled(),
                    onAlbumClick = { viewModel.selectedAlbumIndex = it },
                    onBack = onBack,
                    onCheck = { selectedItem, check ->
                        Log.i("TAG", "PickerBody: ${selectedItem.path}")
                        viewModel.checkPhoto(selectedItem, check)
                    },
                    commit = commit
                )
            }
        }
    }
}

enum class PreviewListMode {
    IMAGE_LIST,
    CHECKED_LIST,
    ;
}

@Composable
fun rememberImageLoader(model: Any): Painter {
    return rememberAsyncImagePainter(model = model, imageLoader = imageLoader)
}

@Composable
fun PickerContent(
    albums: List<AlbumEntity>,
    checkList: List<PhotoQueryEntity>,
    albumsLoading: Boolean,
    selectedAlbumIndex: Int,
    limit: Int,
    filled: Boolean,
    onCheck: (PhotoQueryEntity, Boolean) -> Unit,
    onAlbumClick: (Int) -> Unit,
    onBack: () -> Unit,
    commit: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    // 当前预览列表
    val list = if (albums.isEmpty()) emptyList() else albums[selectedAlbumIndex].list
    // 图片预览状态
    val imagePreviewerState = rememberPickerPreviewerState()
    // 导航栏大小
    var navSize by remember { mutableStateOf(IntSize(0, 0)) }
    // 菜单栏大小
    var tabSize by remember { mutableStateOf(IntSize(0, 0)) }
    // 图片网格状态
    val gridState = rememberLazyListState()
    // 当前预览模式，预览选择列表，预览当前列表
    var previewListMode by rememberSaveable { mutableStateOf(PreviewListMode.IMAGE_LIST) }
    // 显示列表
    val showList = remember { mutableStateListOf<PhotoQueryEntity>() }
    // 视图大小
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    /**
     * 显示预览方法
     */
    fun showPreviewer(mode: PreviewListMode, index: Int) {
        previewListMode = mode
        showList.clear()
        val addList = when (mode) {
            PreviewListMode.IMAGE_LIST -> list
            PreviewListMode.CHECKED_LIST -> checkList
        }
        showList.addAll(addList)
        imagePreviewerState.show(index)
    }

    Box(
        modifier = Modifier
            .background(ConfigContent.current.backgroundColor)
            .fillMaxSize()
            .onSizeChanged {
                containerSize = it
            }
    ) {
        AnimatedVisibility(
            visible = albumsLoading,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ConfigContent.current.loadingColor)
            }
        }
        CenterGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = LocalDensity.current.run { navSize.height.toDp() },
                    bottom = LocalDensity.current.run { tabSize.height.toDp() },
                ),
            lazyListState = gridState,
            list = list,
            filled = filled,
            checkList = checkList,
            onCheck = onCheck,
            imageLoader = {
                rememberImageLoader(it)
            },
        ) { index ->
            showPreviewer(PreviewListMode.IMAGE_LIST, index)
        }
        PickerForeground(
            albums = albums,
            selectedList = checkList,
            selectedAlbumIndex = selectedAlbumIndex,
            limit = limit,
            imageLoader = {
                rememberImageLoader(it)
            },
            onAlbumClick = {
                // 处理相册切换逻辑
                onAlbumClick(it)
                // 使中央的图片网格滚动回最开头
                scope.launch {
                    gridState.scrollToItem(0)
                }
            },
            onBack = onBack,
            onPreview = {
                showPreviewer(PreviewListMode.CHECKED_LIST, 0)
            },
            onNavSize = { navSize = it },
            onTabSize = { tabSize = it },
            commit = commit,
        )

        PickerPreviewer(
            previewerState = imagePreviewerState,
            limit = limit,
            filled = filled,
            imageLoader = {
                rememberImageLoader(it)
            },
            hugeImageLoader = {
                val file by remember { mutableStateOf(File(it)) }
                when (file.getMimeType()) {
                    DecoderMineType.JPEG.mimeType,
                    DecoderMineType.PNG.mimeType -> {
                        rememberHugeImagePainter(path = it)
                            ?: rememberCoilImagePainter(path = it)
                    }
                    DecoderMineType.WEBP.mimeType -> {
                        // 判断图片是否为动态的图片，如果是，就不能用超大图预览
                        val isAnimated = remember { WebpUtil.isWebpAnimated(FileInputStream(file)) }
                        if (isAnimated) {
                            rememberCoilImagePainter(path = it)
                        } else {
                            rememberHugeImagePainter(path = it)
                                ?: rememberCoilImagePainter(path = it)
                        }
                    }
                    DecoderMineType.SVG.mimeType -> {
                        rememberCoilImagePainter(
                            path = it,
                            size = Size(
                                containerSize.width,
                                containerSize.height
                            )
                        )
                    }
                    else -> {
                        rememberCoilImagePainter(path = it)
                    }
                }
            },
            onCheck = onCheck,
            showList = showList,
            checkList = checkList,
            previewListMode = previewListMode,
            commit = commit,
        )
    }

}

@Composable
fun CenterGrid(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    list: List<PhotoQueryEntity>,
    checkList: List<PhotoQueryEntity>,
    filled: Boolean = false,
    imageLoader: @Composable (model: Any) -> Painter,
    onCheck: (PhotoQueryEntity, Boolean) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    onItemClick: (Int) -> Unit,
) {
    val lineCount = 4
    val p = 0.6.dp
    Box(modifier = modifier.fillMaxSize()) {
        GridLayout(
            columns = lineCount,
            size = list.size,
            state = lazyListState,
            contentPadding = contentPadding
        ) { index ->
            val item = list[index]
            if (item.path != null) {
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
                    ImageGrid(
                        image = item.path!!,
                        check = checkList.contains(item),
                        filled = filled,
                        imageLoader = imageLoader,
                        onChangeAction = {
                            onCheck(item, !checkList.contains(item))
                        }) {
                        onItemClick(index)
                    }
                }
            }
        }
    }

}

@Composable
fun GridLayout(
    modifier: Modifier = Modifier,
    columns: Int,
    size: Int,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
    block: @Composable (Int) -> Unit,
) {
    val line = ceil(size.toDouble() / columns).toInt()
    LazyColumn(
        modifier = modifier,
        state = state,
        content = {
            items(count = line, key = { it }) { c ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (r in 0 until columns) {
                        val index = c * columns + r
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            if (index < size) {
                                block(index)
                            }
                        }
                    }
                }
            }
        }, contentPadding = contentPadding
    )
}

val checkMaskerColor = Color(0x8F000000)
val uncheckMaskerColor = Color(0x0F000000)

@Composable
fun ImageGrid(
    modifier: Modifier = Modifier,
    image: Any,
    check: Boolean,
    filled: Boolean = false,
    imageLoader: @Composable (model: Any) -> Painter,
    onChangeAction: () -> Unit = {},
    onClick: () -> Unit,
) {
    Image(
        modifier = modifier
            .clickable {
                onClick()
            }
            .fillMaxSize(),
        painter = imageLoader(image),
        contentScale = ContentScale.Crop,
        contentDescription = null,
    )
    val maskerColor by animateColorAsState(
        targetValue = if (check) checkMaskerColor else uncheckMaskerColor
    )
    Box(
        modifier = Modifier
            .background(maskerColor)
            .fillMaxSize(), contentAlignment = Alignment.TopCenter
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
            CheckButton(
                check = check,
                hideCircle = filled,
                key = image,
                onChangeAction = onChangeAction,
                modifier = Modifier
                    .size(32.dp)
                    .padding(top = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
fun CheckButton(
    modifier: Modifier = Modifier,
    check: Boolean,
    hideCircle: Boolean = false,
    checkColor: Color = ConfigContent.current.checkColorDefault,
    key: Any = Unit,
    onChangeAction: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .pointerInput(key) {
                if (onChangeAction != null) {
                    detectTapGestures {
                        onChangeAction()
                    }
                }
            },
        contentAlignment = Alignment.TopEnd,
    ) {
        AnimatedVisibility(
            visible = check,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = checkColor,
                modifier = Modifier
                    .size(18.dp)
            )
        }
        AnimatedVisibility(
            visible = !check && !hideCircle,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(18.dp)
                    .border(1.dp, checkColor, CircleShape),
            )
        }
    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun PermissionNotGranted(permissionState: MultiplePermissionsState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "👋 获取文件权限，以便访问本地相册！")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            permissionState.launchMultiplePermissionRequest()
        }, colors = ButtonDefaults.buttonColors(backgroundColor = ConfigContent.current.backgroundColor)) {
            Text(text = "🛴 获取权限")
        }
    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun PermissionsNotAvailable() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val ctx = LocalContext.current
        Text(text = "✋ 没有权限，无法访问本地相册！")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            XXPermissions.startPermissionActivity(ctx, permissions)
        }, colors = ButtonDefaults.buttonColors(backgroundColor = ConfigContent.current.backgroundColor)) {
            Text(text = "🚴‍♀️ 获取权限")
        }
    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun PickerPermissions(
    onPermissionChange: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    val permissionState =
        rememberMultiplePermissionsState(
            permissions = permissions
        )
    LaunchedEffect(key1 = Unit, block = {
        permissionState.launchMultiplePermissionRequest()
    })
    LaunchedEffect(key1 = permissionState.allPermissionsGranted, block = {
        onPermissionChange(permissionState.allPermissionsGranted)
    })
    PermissionsRequired(
        multiplePermissionsState = permissionState,
        permissionsNotGrantedContent = {
            PermissionNotGranted(permissionState = permissionState)
        },
        permissionsNotAvailableContent = {
            PermissionsNotAvailable()
        }) {
        content()
    }
}