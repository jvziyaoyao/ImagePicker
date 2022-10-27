package com.origeek.imagePicker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.origeek.imagePicker.config.NO_LIMIT
import com.origeek.imagePicker.domain.model.AlbumEntity
import com.origeek.imagePicker.domain.model.PhotoQueryEntity

@Composable
fun PickerForeground(
    albums: List<AlbumEntity>,
    selectedList: List<PhotoQueryEntity>,
    selectedAlbumIndex: Int,
    limit: Int,
    imageLoader: @Composable (model: Any) -> Painter,
    onAlbumClick: (Int) -> Unit,
    onBack: () -> Unit,
    onPreview: () -> Unit,
    onNavSize: (IntSize) -> Unit,
    onTabSize: (IntSize) -> Unit,
    commit: () -> Unit,
) {
    val maskerColor = Color.Black.copy(0.4f)
    var bSize by remember { mutableStateOf(IntSize(0, 0)) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                bSize = it
            },
    ) {
        var showAction by rememberSaveable { mutableStateOf(false) }
        // 如果打开了列表，返回键支持关闭
        BackHandler(showAction) {
            showAction = false
        }
        SimpleNav(
            modifier = Modifier
                .background(ConfigContent.current.surfaceColor)
                .onSizeChanged {
                    onNavSize(it)
                },
            title = ConfigContent.current.navTitle,
            confirm = if (limit == NO_LIMIT) "确认(${selectedList.size})" else "确认(${selectedList.size}/$limit)",
            showConfirm = selectedList.isNotEmpty(),
            onBack = onBack,
            commit = commit,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AnimatedVisibility(
                visible = showAction,
                modifier = Modifier
                    .fillMaxSize(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures {
                            showAction = false
                        }
                    }
                    .background(maskerColor)
                    .fillMaxSize())
            }
        }
        AnimatedVisibility(
            visible = albums.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth(),
            enter = slideInVertically { h -> h },
            exit = slideOutVertically { h -> h }
        ) {
            BottomHandler(
                modifier = Modifier
                    .background(ConfigContent.current.surfaceColor)
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    },
                list = albums,
                imageLoader = imageLoader,
                onAlbumClick = onAlbumClick,
                selectedAlbumIndex = selectedAlbumIndex,
                albumHeight = LocalDensity.current.run { (bSize.height * 5 / 8).toDp() },
                onSizeChange = {
                    onTabSize(it)
                },
                onPreview = onPreview,
                showAction = showAction,
                showPreview = selectedList.isNotEmpty(),
                onActionChange = { showAction = it },
            )
        }
    }
}

@Composable
fun AlbumList(
    modifier: Modifier = Modifier,
    onAlbumClick: (Int) -> Unit,
    list: List<AlbumEntity>,
    imageLoader: @Composable (model: Any) -> Painter,
) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(), content = {
        items(list.size) { index ->
            val item = list[index]
            Row(
                modifier = Modifier
                    .clickable {
                        onAlbumClick(index)
                    }
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .fillMaxWidth(0.12f)
                        .aspectRatio(1f),
                    painter = imageLoader(item.list.first().path ?: ""),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.name)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "" + (item.list.size),
                        fontSize = 12.sp,
                        color = LocalContentColor.current.copy(0.4f)
                    )
                }
            }
        }
    })
}

@Composable
fun BottomHandler(
    modifier: Modifier = Modifier,
    list: List<AlbumEntity>,
    selectedAlbumIndex: Int,
    onAlbumClick: (Int) -> Unit,
    albumHeight: Dp = 300.dp,
    showAction: Boolean,
    showPreview: Boolean = false,
    imageLoader: @Composable (model: Any) -> Painter,
    onPreview: () -> Unit,
    onActionChange: (Boolean) -> Unit,
    onSizeChange: (IntSize) -> Unit,
) {
    val albumsHeightState by animateDpAsState(targetValue = if (showAction) albumHeight else 0.dp)
    Column(modifier = modifier.fillMaxWidth()) {
        AlbumList(
            list = list,
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(maxHeight = albumsHeightState),
            imageLoader = imageLoader,
            onAlbumClick = {
                onAlbumClick(it)
                onActionChange(false)
            },
        )
        Row(
            modifier = Modifier
                .onSizeChanged {
                    onSizeChange(it)
                }
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        onActionChange(!showAction)
                    }
                    .padding(commonTextPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (list.isEmpty()) "" else list[selectedAlbumIndex].name)
                Spacer(modifier = Modifier.width(8.dp))
                val iconRotateAnimation by animateFloatAsState(targetValue = if (showAction) -180f else 0f)
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = LocalTextStyle.current.color.copy(0.6f),
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer {
                            rotationZ = iconRotateAnimation
                        }
                )
            }
            AnimatedVisibility(
                visible = showPreview,
                enter = fadeIn() + slideInHorizontally { w -> w },
                exit = fadeOut() + slideOutHorizontally { w -> w }
            ) {
                Text(text = "预览", modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        onPreview()
                    }
                    .padding(commonTextPadding))
            }
        }
    }
}

@Composable
fun SimpleNav(
    modifier: Modifier = Modifier,
    title: String = "",
    confirm: String = "",
    showConfirm: Boolean = false,
    onBack: () -> Unit = {},
    commit: () -> Unit = {},
) {
    Row(
        modifier = modifier
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