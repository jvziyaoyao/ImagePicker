package com.origeek.imagePicker.domain.repository

import com.origeek.imagePicker.util.ContextUtil
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
     * 保存内容到缓存中
     * @param data String
     */
    fun saveToTemp(data: String)

    /**
     * 从缓存中加载内容
     * @return String
     */
    fun loadFromTemp(): String

}

class TempRepoImpl : TempRepo {

    private val tempFilePath = "${ContextUtil.getApplicationByReflect().cacheDir}/photos.json"

    override fun saveToTemp(data: String) {
        val file = File(tempFilePath)
        val writer = FileWriter(file)
        writer.write(data)
        writer.flush()
        writer.close()
    }

    override fun loadFromTemp(): String {
        val file = File(tempFilePath)
        val reader = FileReader(file)
        val json = reader.readText()
        reader.close()
        return json
    }


}