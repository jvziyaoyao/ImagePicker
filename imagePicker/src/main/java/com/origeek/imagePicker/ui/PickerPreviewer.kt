package com.origeek.imagePicker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.origeek.imagePicker.config.NO_LIMIT
import com.origeek.imagePicker.domain.model.PhotoQueryEntity
import com.origeek.imagePicker.util.findWindow
import com.origeek.imagePicker.util.hideSystemUI
import com.origeek.imagePicker.util.showSystemUI
import com.origeek.imageViewer.*
import kotlinx.coroutines.launch

class PickerPreviewerState internal constructor() {

    // 当前页码
    val index: Int
        get() = state.currentPage

    // 当前显示标识
    val visible: Boolean
        get() = state.visible

    // 当前显示的目标标识
    val visibleTarget: Boolean?
        get() = state.visibleTarget

    // 是否支持关闭
    val canClose: Boolean
        get() = state.canClose

    // 是否正在执行动画
    val animating: Boolean
        get() = state.animating

    var getKey: ((Int) -> Any)? = null

    // 预览组件状态
    internal lateinit var state: ImagePreviewerState

    private var isOpenTransform: Boolean = false

    fun clearTransformItems() = state.clearTransformItems()

    suspend fun show(index: Int, itemState: TransformItemState?) {
        isOpenTransform = itemState != null
        if (itemState != null) {
            state.openTransform(index, itemState)
        } else {
            state.open(index)
        }
    }

    suspend fun hide() {
        if (getKey != null && isOpenTransform) {
            state.closeTransform(getKey!!.invoke(index))
        } else {
            state.close()
        }
    }

    suspend fun scroll(index: Int) {
        state.animateScrollToPage(index)
    }
}

@Composable
fun rememberPickerPreviewerState(getKey: (Int) -> Any): PickerPreviewerState {
    val previewerState = rememberPreviewerState()
    previewerState.enableVerticalDrag { getKey(it) }
    val pickerState = remember { PickerPreviewerState() }
    pickerState.getKey = getKey
    pickerState.state = previewerState
    return pickerState
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PickerPreviewer(
    previewerState: PickerPreviewerState,
    limit: Int,
    filled: Boolean,
    previewListMode: PreviewListMode,
    imageLoader: @Composable (model: Any) -> Painter,
    hugeImageLoader: @Composable (path: String) -> Any,
    showList: List<PhotoQueryEntity>,
    checkList: List<PhotoQueryEntity>,
    onCheck: (PhotoQueryEntity, Boolean) -> Unit,
    commit: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val window = LocalContext.current.findWindow()
    var fullScreen by rememberSaveable { mutableStateOf(false) }
    var imageState by remember { mutableStateOf<ImageViewerState?>(null) }
    var scale by remember { mutableStateOf(1F) }
    LaunchedEffect(key1 = imageState?.scale?.value) {
        imageState?.let {
            if (it.scale.value > scale && it.scale.value > 1F) {
                fullScreen = true
            }
            scale = it.scale.value
        }
    }
    if (fullScreen || previewerState.canClose || previewerState.animating) BackHandler {
        if (fullScreen) {
            fullScreen = false
        } else if (previewerState.canClose) {
            scope.launch {
                previewerState.hide()
            }
        }
    }
    LaunchedEffect(fullScreen) {
        if (window == null) return@LaunchedEffect
        if (fullScreen) {
            hideSystemUI(window)
        } else {
            showSystemUI(window)
        }
    }
    LaunchedEffect(previewerState.visibleTarget) {
        if (window == null) return@LaunchedEffect
        if (previewerState.visibleTarget == false) {
            fullScreen = false
        }
    }
    ImagePreviewer(
        count = showList.size,
        state = previewerState.state,
        imageLoader = {
            hugeImageLoader(showList[it].path ?: "")
        },
        onTap = {
            fullScreen = !fullScreen
        },
        foreground = { size, page ->
            PreviewForeground(
                // 这里的page必须用目标的page
                index = previewerState.index,
                size = size,
                limit = limit,
                filled = filled,
                showList = showList,
                checkList = checkList,
                previewListMode = previewListMode,
                fullScreen = fullScreen,
                imageLoader = imageLoader,
                onBack = {
                    scope.launch {
                        previewerState.hide()
                    }
                },
                commit = commit,
                onCheck = onCheck,
                onPreviewItemClick = {
                    val index = showList.indexOf(it)
                    if (index == -1) return@PreviewForeground
                    scope.launch {
                        previewerState.scroll(index)
                    }
                }
            )
        },
        background = { _, _ ->
            val backgroundColor by animateColorAsState(
                targetValue = if (fullScreen) {
                    ConfigContent.current.backgroundColorDark
                } else {
                    ConfigContent.current.backgroundColor
                }
            )
            Box(
                modifier = Modifier
                    .background(backgroundColor)
                    .fillMaxSize()
            )
        },
        currentViewerState = {
            imageState = it
        },
        exit = fadeOut(animationSpec = spring(stiffness = 1000F))
                + scaleOut(
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
    )
}

@Composable
fun PreviewForeground(
    index: Int,
    size: Int,
    limit: Int,
    filled: Boolean,
    fullScreen: Boolean,
    showList: List<PhotoQueryEntity>,
    checkList: List<PhotoQueryEntity>,
    previewListMode: PreviewListMode,
    imageLoader: @Composable (model: Any) -> Painter,
    onBack: () -> Unit,
    commit: () -> Unit,
    onCheck: (PhotoQueryEntity, Boolean) -> Unit,
    onPreviewItemClick: (PhotoQueryEntity) -> Unit,
) {
    if (showList.isEmpty()) {
        onBack()
        return
    }
    val page = if (index > showList.size - 1) showList.size - 1 else index
    val currentItem = showList[page]
    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !fullScreen,
            modifier = Modifier.fillMaxWidth(),
            enter = fadeIn() + slideInVertically { h -> -h },
            exit = fadeOut() + slideOutVertically { h -> -h },
        ) {
            PreviewNav(
                title = "${page + 1}/$size",
                confirm = if (limit == NO_LIMIT) "确认(${checkList.size})" else "确认(${checkList.size}/$limit)",
                showConfirm = checkList.isNotEmpty(),
                onBack = onBack,
                commit = commit,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        AnimatedVisibility(
            visible = !fullScreen,
            modifier = Modifier.fillMaxWidth(),
            enter = fadeIn() + slideInVertically { h -> h },
            exit = fadeOut() + slideOutVertically { h -> h },
        ) {
            PreviewTab(
                filled = filled,
                currentItem = currentItem,
                checkList = checkList,
                showList = showList,
                previewListMode = previewListMode,
                imageLoader = imageLoader,
                onAction = onCheck,
                onPreviewItemClick = onPreviewItemClick,
            )
        }
    }
}

@Composable
fun PreviewNav(
    modifier: Modifier = Modifier,
    title: String = "",
    confirm: String = "",
    showConfirm: Boolean = false,
    onBack: () -> Unit = {},
    commit: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { }
            }
            .background(ConfigContent.current.previewSurfaceColor)
            .statusBarsPadding()
            .padding(horizontal = 6.dp, vertical = 10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .padding(start = 6.dp, end = 12.dp)
                .clip(CircleShape)
                .size(38.dp)
                .clickable {
                    onBack()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = null,
                tint = LocalTextStyle.current.color.copy(0.6f)
            )
        }
        Text(
            text = title,
            modifier = Modifier.padding(vertical = 6.dp),
            color = LocalTextStyle.current.color.copy(0.8f),
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        AnimatedVisibility(
            visible = showConfirm,
            enter = fadeIn() + slideInHorizontally { w -> w },
            exit = fadeOut() + slideOutHorizontally { w -> w }) {
            Text(
                text = confirm,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        commit()
                    }
                    .padding(commonTextPadding),
                color = LocalTextStyle.current.color.copy(0.8f),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun PreviewTab(
    filled: Boolean,
    currentItem: PhotoQueryEntity,
    showList: List<PhotoQueryEntity>,
    checkList: List<PhotoQueryEntity>,
    previewListMode: PreviewListMode,
    imageLoader: @Composable (model: Any) -> Painter,
    onAction: (PhotoQueryEntity, Boolean) -> Unit,
    onPreviewItemClick: (PhotoQueryEntity) -> Unit,
) {
    val check = checkList.contains(currentItem)
    val hideCheck = !check && filled
    Column(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures { }
            }
            .background(ConfigContent.current.previewSurfaceColor)
            .fillMaxWidth()
    ) {
        PreviewTabRow(
            currentItem = currentItem,
            showList = showList,
            checkList = checkList,
            previewListMode = previewListMode,
            imageLoader = imageLoader,
            onItemClick = onPreviewItemClick
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    paddingValues = rememberInsetsPaddingValues(
                        insets = LocalWindowInsets.current.navigationBars,
                        applyBottom = true,
                        additionalBottom = 10.dp,
                        additionalTop = 10.dp,
                        additionalStart = 6.dp,
                        additionalEnd = 6.dp
                    )
                ),
            horizontalArrangement = Arrangement.End,
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(!hideCheck) {
                        onAction(currentItem, !check)
                    }
                    .padding(commonTextPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CheckButton(
                    check = check,
                    hideCircle = false,
                    checkColor = if (!hideCheck) {
                        ConfigContent.current.tabCheckColor
                    } else {
                        ConfigContent.current.tabCheckColor.copy(0.1f)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "选择",
                    modifier = Modifier,
                    color = if (!hideCheck) LocalTextStyle.current.color else LocalTextStyle.current.color.copy(
                        0.2f
                    )
                )
            }
        }
    }
}

@Composable
fun PreviewTabRow(
    currentItem: PhotoQueryEntity,
    showList: List<PhotoQueryEntity>,
    checkList: List<PhotoQueryEntity>,
    previewListMode: PreviewListMode,
    imageLoader: @Composable (model: Any) -> Painter,
    onItemClick: (PhotoQueryEntity) -> Unit,
) {
    val listState = rememberLazyListState()
    val displayList = when (previewListMode) {
        PreviewListMode.IMAGE_LIST -> checkList
        PreviewListMode.CHECKED_LIST -> showList
    }
    LaunchedEffect(key1 = currentItem, key2 = displayList.size, block = {
        val index = displayList.indexOf(currentItem)
        if (index != -1) listState.animateScrollToItem(index)
    })
    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        contentPadding = PaddingValues(start = 12.dp),
        content = {
            items(displayList.size) { index ->
                val item = displayList[index]
                val borderColor by animateColorAsState(
                    targetValue = if (item == currentItem) {
                        ConfigContent.current.tabImageCheckBorderColor
                    } else {
                        Color.Transparent
                    }
                )
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, end = 8.dp)
                        .border(2.dp, borderColor)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(48.dp)
                    ) {
                        Image(
                            painter = imageLoader(item.path!!),
                            contentDescription = null,
                            modifier = Modifier
                                .clickable {
                                    onItemClick(item)
                                }
                                .fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        val maskerColor =
                            if (checkList.indexOf(item) == -1) {
                                ConfigContent.current.tabImageMaskerUncheckedColor
                            } else {
                                ConfigContent.current.tabImageMaskerColor
                            }
                        Box(
                            modifier = Modifier
                                .background(maskerColor)
                                .fillMaxSize()
                        )
                    }
                }
            }
        })
}