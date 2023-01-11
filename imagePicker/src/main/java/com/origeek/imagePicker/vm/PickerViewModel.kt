package com.origeek.imagePicker.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.origeek.imagePicker.config.ImagePickerConfig
import com.origeek.imagePicker.config.NO_LIMIT
import com.origeek.imagePicker.domain.model.AlbumEntity
import com.origeek.imagePicker.domain.model.PhotoQueryEntity
import com.origeek.imagePicker.domain.useCase.AlbumUseCase

class PickerViewModel(
    private val albumUseCase: AlbumUseCase,
) : ViewModel() {

    // 加载标识
    var loading by mutableStateOf(false)

    // 相册列表
    val albumList = mutableStateListOf<AlbumEntity>()

    // 选中列表
    val checkList = mutableStateListOf<PhotoQueryEntity>()

    // 当前预览列表
    val previewList = mutableStateListOf<PhotoQueryEntity>()

    // 当前选择的相册
    var selectedAlbumIndex by mutableStateOf(0)

    // picker的配置项
    var pickerConfig: ImagePickerConfig? = null

    /**
     * 从缓存文件加载
     */
    private suspend fun loadFromTemp() {
        albumList.clear()
        albumList.addAll(albumUseCase.loadFromTemp())
    }

    /**
     * 从手机相册加载
     */
    private suspend fun loadFromDatabase() {
        val filterMineType = pickerConfig?.filterMineType ?: emptyList()
        val allAlbums = albumUseCase.loadFromDatabase(filterMineType)

        albumList.clear()
        albumList.addAll(allAlbums)
    }

    /**
     * 更新预览列表
     * @param list List<PhotoQueryEntity>
     */
    fun updatePreviewList(list: List<PhotoQueryEntity>) {
        previewList.clear()
        previewList.addAll(list)
    }

    /**
     * 初始化
     */
    suspend fun initial() {
        loading = true
        loadFromTemp()
        if (albumList.isNotEmpty()) loading = false
        loadFromDatabase()
        loading = false
    }

    /**
     * 更新当前相册列表
     */
    suspend fun update() {
        loadFromDatabase()
    }

    /**
     * 选中图片
     * @param selectedItem PhotoQueryEntity
     * @param check Boolean
     */
    fun checkPhoto(selectedItem: PhotoQueryEntity, check: Boolean) {
        if (check && !checkFilled()) {
            checkList.add(selectedItem)
        } else {
            checkList.remove(selectedItem)
        }
    }

    /**
     * 检查看满了没
     * @return Boolean
     */
    fun checkFilled(): Boolean {
        val limit = pickerConfig?.limit ?: NO_LIMIT
        return limit != NO_LIMIT && limit <= checkList.size
    }

}



