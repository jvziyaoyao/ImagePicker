package com.origeek.imagePicker.vm

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.origeek.imagePicker.config.ImagePickerConfig
import com.origeek.imagePicker.config.NO_LIMIT
import com.origeek.imagePicker.model.AlbumEntity
import com.origeek.imagePicker.model.PhotoQueryEntity
import com.origeek.imagePicker.util.ContextUtil
import com.origeek.imagePicker.util.joiner
import kotlinx.coroutines.*
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.function.Function
import java.util.stream.Collectors

class PickerViewModel : ViewModel(), CoroutineScope by MainScope() {

    private val tempFilePath = "${ContextUtil.getApplicationByReflect().cacheDir}/photos.json"

    // 原始列表
    private val photos = mutableStateListOf<PhotoQueryEntity>()

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
     * 初始化
     */
    fun initial() {
        launch {
            loadFromTemp()
            loadFromDatabase()
        }
    }

    /**
     * 从缓存文件加载
     */
    private suspend fun loadFromTemp() {
        val t01 = System.currentTimeMillis()
        loading = true
        val allImages = copyFromTempFile()
        photos.clear()
        photos.addAll(allImages)

        albumList.clear()
        albumList.addAll(getAlbumList())

        if (albumList.isNotEmpty()) loading = false
        val t02 = System.currentTimeMillis()
        Log.i("TAG", "initial: loadFromTemp ${t02 - t01}")
    }

    /**
     * 从手机相册加载
     */
    private suspend fun loadFromDatabase() {
        val t01 = System.currentTimeMillis()
        val allImages = getAllImages()
        photos.clear()
        photos.addAll(allImages)
        // 保存到缓存
        copy2TempFile()
        // 获取全部相册
        val allAlbums = getAlbumList()
        // 如果本来是空列表，直接填充
        if (albumList.isEmpty()) {
            albumList.addAll(allAlbums)
            loading = false
            return
        }
        val currentAlbum = albumList[selectedAlbumIndex]
        val oldAlbumsMap =
            albumList.stream().collect(Collectors.toMap(AlbumEntity::path, Function.identity()))
        allAlbums.forEach { album ->
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
                val addList = newList.stream().map { oldListMap[it.path] ?: it }.collect(Collectors.toList())
                oldList as ArrayList
                oldList.clear()
                oldList.addAll(addList)
                return@forEach
            }
            if (oldAlbum != null) albumList.remove(oldAlbum)
            albumList.add(album)
        }
        loading = false
        val t02 = System.currentTimeMillis()
        Log.i("TAG", "initial: loadFromDatabase ${t02 - t01}")
    }

    /**
     * 从缓存文件夹提取
     * @return List<PhotoQueryEntity>
     */
    private fun copyFromTempFile(): List<PhotoQueryEntity> {
        return try {
            val file = File(tempFilePath)
            val reader = FileReader(file)
            val json = reader.readText()
            val allImages: List<PhotoQueryEntity> =
                Gson().fromJson(json, object : TypeToken<List<PhotoQueryEntity>>() {}.type)
            reader.close()
            allImages
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 复制到缓存文件夹
     */
    private fun copy2TempFile() {
        val file = File(tempFilePath)
        val writer = FileWriter(file)
        val json = Gson().toJson(photos)
        writer.write(json)
        writer.flush()
        writer.close()
    }

    /**
     * 获取最近图片列表
     * @return AlbumEntity
     */
    private suspend fun getLatestList(): AlbumEntity? = coroutineScope {
        withContext(Dispatchers.Default) {
            val images = photos.stream().limit(1000).collect(Collectors.toList())
            if (images.isEmpty()) return@withContext null
            AlbumEntity("/Latest", "Latest", images)
        }
    }

    /**
     * 获取相册
     * @return List<AlbumEntity>
     */
    private suspend fun getAlbumList(): List<AlbumEntity> = coroutineScope {
        async {
            val albumMap = HashMap<String, ArrayList<PhotoQueryEntity>>()
            photos.forEach {
                if (it.path.isNullOrEmpty()) return@forEach
                val index = it.path!!.lastIndexOf("/")
                if (index == -1) return@forEach
                val folderName = it.path!!.substring(0, index)
                var folderItems = albumMap[folderName]
                if (folderItems == null) {
                    folderItems = ArrayList()
                    albumMap[folderName] = folderItems
                }
                folderItems.add(it)
            }
            val resList = albumMap.entries.stream().map {
                val splits = it.key.split("/")
                val albumName = splits[splits.lastIndex]
                AlbumEntity(it.key, albumName, it.value)
            }.collect(Collectors.toList())
            val latestAlbum = getLatestList()
            if (latestAlbum != null) resList.add(0, latestAlbum)
            resList
        }.await()
    }

    /**
     * 获取全部图片
     * @return List<PhotoQueryEntity>
     */
    @SuppressLint("Recycle")
    private suspend fun getAllImages(): List<PhotoQueryEntity> {
        return coroutineScope {
            async(Dispatchers.IO) {
                val context = ContextUtil.getApplicationByReflect()
                val contentResolver: ContentResolver = context.contentResolver
                val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                // 在这里过滤选择的mimeType
                val filterMineType = pickerConfig?.filterMineType ?: emptyList()
                val mimeTypeFilter = joiner(filterMineType, ",", "'","'")
                val selection =
                    if (mimeTypeFilter.isNotBlank()) "${MediaStore.MediaColumns.MIME_TYPE} NOT IN ($mimeTypeFilter)" else null
                val query = contentResolver.query(
                    uri,
                    null,
                    selection,
                    null,
                    MediaStore.MediaColumns.DATE_ADDED + " DESC"
                ) ?: return@async emptyList()
                val columnNames = query.columnNames
                val list = ArrayList<PhotoQueryEntity>()
                while (query.moveToNext()) {
                    val photoQueryEntity = PhotoQueryEntity()
                    for (name in columnNames) {
                        val columnIndex = query.getColumnIndex(name)
                        when (name) {
                            MediaStore.Images.Media.DISPLAY_NAME -> {
                                photoQueryEntity.name = query.getString(columnIndex)
                            }
                            MediaStore.Images.Media.MIME_TYPE -> {
                                photoQueryEntity.mimeType = query.getString(columnIndex)
                            }
                            MediaStore.Images.Media.WIDTH -> {
                                photoQueryEntity.width = query.getLong(columnIndex)
                            }
                            MediaStore.Images.Media.HEIGHT -> {
                                photoQueryEntity.height = query.getLong(columnIndex)
                            }
                            MediaStore.Images.Media.DATA -> {
                                photoQueryEntity.path = query.getString(columnIndex)
                            }
                            MediaStore.Images.Media.SIZE -> {
                                photoQueryEntity.size = query.getLong(columnIndex)
                            }
                            MediaStore.Images.Media.DATE_ADDED -> {
                                photoQueryEntity.time = query.getLong(columnIndex)
                            }
                            MediaStore.Images.Media._ID -> {
                                photoQueryEntity.id = query.getLong(columnIndex)
                            }
                        }
                    }
                    list.add(photoQueryEntity)
                }
                return@async list
            }.await()
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



