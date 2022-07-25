package com.origeek.imagePicker.util

import okhttp3.internal.and
import java.io.File
import java.io.InputStream
import java.net.URLConnection

fun File.getMimeType(): String {
    val fileNameMap = URLConnection.getFileNameMap()
    return fileNameMap.getContentTypeFor(this.name)
}

fun isWebpAnimated(inputStream: InputStream): Boolean {
    var result = false
    try {
        inputStream.skip(12)
        val buf = ByteArray(4)
        val i: Int = inputStream.read(buf)
        if ("VP8X" == String(buf, 0, i)) {
            inputStream.skip(12)
            result = ((inputStream.read(buf) == 4) && (buf[3] and 0x00000002 != 0))
        }
    } catch (e: Exception) {
    } finally {
        try {
            inputStream.close()
        } catch (e: Exception) {
        }
    }
    return result
}