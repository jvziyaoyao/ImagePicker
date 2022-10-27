package com.origeek.imagePicker.domain.repository

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.origeek.imagePicker.util.ContextUtil
import com.origeek.imagePicker.util.getStoreData
import com.origeek.imagePicker.util.setStoreData
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileReader
import java.io.FileWriter

/**
 * @program: ImagePicker
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-09 16:49
 **/
interface TempRepo {

    /**
     * 报错
     * @param key String
     * @param json String
     */
    suspend fun store(key: String, json: String)

    /**
     * 从缓存中加载
     * @param key String
     * @return String?
     */
    suspend fun restore(key: String): String?

}

class TempRepoImpl(
    private val context: Context
) : TempRepo {

    override suspend fun store(key: String, json: String) {
        context.setStoreData(stringPreferencesKey(key), json)
    }

    override suspend fun restore(key: String): String? {
        return context.getStoreData(stringPreferencesKey(key)).first()
    }

}