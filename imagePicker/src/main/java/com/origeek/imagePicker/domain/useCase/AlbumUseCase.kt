package com.origeek.imagePicker.domain.useCase

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.origeek.imagePicker.domain.model.AlbumEntity
import com.origeek.imagePicker.domain.model.PhotoQueryEntity
import com.origeek.imagePicker.domain.repository.ImageRepo
import com.origeek.imagePicker.domain.repository.TempRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.stream.Collectors

/**
 * @program: ImagePicker
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-09 16:39
 **/
interface AlbumUseCase {

    /**
     * 从缓存中加载
     * @return List<AlbumEntity>
     */
    suspend fun loadFromTemp(): List<AlbumEntity>

    /**
     * 从数据库中加载
     * @param filterMineType List<String>
     * @return List<AlbumEntity>
     */
    suspend fun loadFromDatabase(
        filterMineType: List<String> = emptyList()
    ): List<AlbumEntity>

}

class AlbumUseCaseImpl(
    private val imageRepo: ImageRepo,
    private val tempRepo: TempRepo,
) : AlbumUseCase {

    private fun copyFromTemp(): List<PhotoQueryEntity> {
        val json = tempRepo.loadFromTemp()
        return Gson().fromJson(json, object : TypeToken<List<PhotoQueryEntity>>() {}.type)
    }

    private fun copyToTemp(photos: List<PhotoQueryEntity>) {
        val json = Gson().toJson(photos)
        tempRepo.saveToTemp(json)
    }

    private suspend fun getLatestList(photos: List<PhotoQueryEntity>): AlbumEntity? =
        coroutineScope {
            withContext(Dispatchers.Default) {
                val images = photos.stream().limit(1000).collect(Collectors.toList())
                if (images.isEmpty()) return@withContext null
                AlbumEntity("/Latest", "Latest", images)
            }
        }

    private suspend fun getAlbumList(photos: List<PhotoQueryEntity>): List<AlbumEntity> =
        coroutineScope {
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
                val latestAlbum = getLatestList(photos)
                if (latestAlbum != null) resList.add(0, latestAlbum)
                resList
            }.await()
        }

    override suspend fun loadFromTemp(): List<AlbumEntity> {
        val photos = copyFromTemp()
        return getAlbumList(photos)
    }

    override suspend fun loadFromDatabase(
        filterMineType: List<String>
    ): List<AlbumEntity> {
        val photos = imageRepo.getAllImages(filterMineType)
        // 保存到缓存
        copyToTemp(photos)
        // 获取全部相册
        return getAlbumList(photos)
    }

}