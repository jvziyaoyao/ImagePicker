package com.origeek.imagePicker.vm

import android.util.Log
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
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class PickerViewModel(
    private val albumUseCase: AlbumUseCase,
) : ViewModel() {

    // 加载标识
    var loading by mutableStateOf(false)

    // 相册列表
    val albumList = mutableStateListOf<AlbumEntity>()

    // 选中列表
    val checkList = mutableStateListOf<PhotoQueryEntity>()

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
        // 如果本来是空列表，直接填充
        if (albumList.isEmpty()) {
            albumList.addAll(allAlbums)
        } else {
            copyToCurrentAlbumList(allAlbums)
        }
    }

    /**
     * 复制到当前相册列表
     * @param allAlbums List<AlbumEntity>
     */
    private fun copyToCurrentAlbumList(allAlbums: List<AlbumEntity>) {
        val currentAlbum = albumList[selectedAlbumIndex]
        val oldAlbumsMap =
            albumList.stream().collect(Collectors.toMap(AlbumEntity::path, Function.identity()))
        for (album in allAlbums) {
            val oldAlbum = oldAlbumsMap[album.path]
            if (oldAlbum?.path == currentAlbum.path) {
                val newList = album.list
                val oldList = oldAlbum.list
                val oldListMap = oldList.stream().collect(
                    Collectors.toMap(
                        PhotoQueryEntity::path,
                        Function.identity()
                    )
                )
                val addList =
                    newList.stream().map { oldListMap[it.path] ?: it }.collect(Collectors.toList())
                oldList as ArrayList
                oldList.clear()
                oldList.addAll(addList)
                continue
            }
            if (oldAlbum != null) albumList.remove(oldAlbum)
            albumList.add(album)
        }
    }

    /**
     * 初始化
     */
    suspend fun initial() {
        loading = true
        loadFromTemp()

        Log.i("TAG", "initial: ------------------>")
        albumList.forEach {
            Log.i("TAG", "initial: albumList ${it.name} - ${UUID.randomUUID()}")
        }

        if (albumList.isNotEmpty()) loading = false
        loadFromDatabase()

        Log.i("TAG", "initial: ------------------>")
        albumList.forEach {
            Log.i("TAG", "initial: albumList ${it.name} - ${UUID.randomUUID()}")
        }

        loading = false
    }

    /**
     * 更新当前相册列表
     */
    suspend fun update() {
        loadFromDatabase()

        Log.i("TAG", "initial: ------------------>")
        albumList.forEach {
            Log.i("TAG", "initial: albumList ${it.name} - ${UUID.randomUUID()}")
        }
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



