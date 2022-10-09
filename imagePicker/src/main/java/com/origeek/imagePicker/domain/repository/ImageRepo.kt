package com.origeek.imagePicker.domain.repository

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.MediaStore
import com.origeek.imagePicker.domain.model.PhotoQueryEntity
import com.origeek.imagePicker.util.ContextUtil
import com.origeek.imagePicker.util.joiner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * @program: ImagePicker
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-09 16:29
 **/

interface ImageRepo {

    /**
     * 获取本地的全部图片
     * @param filterMineType List<String>
     * @return List<PhotoQueryEntity>
     */
    suspend fun getAllImages(filterMineType: List<String> = emptyList()): List<PhotoQueryEntity>

}

/**
 * ImageRepo默认实现
 */
class ImageRepoImpl : ImageRepo {

    @SuppressLint("Recycle")
    override suspend fun getAllImages(filterMineType: List<String>): List<PhotoQueryEntity> {
        return coroutineScope {
            async(Dispatchers.IO) {
                val context = ContextUtil.getApplicationByReflect()
                val contentResolver: ContentResolver = context.contentResolver
                val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                // 在这里过滤选择的mimeType
                val mimeTypeFilter = joiner(filterMineType, ",", "'", "'")
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

}