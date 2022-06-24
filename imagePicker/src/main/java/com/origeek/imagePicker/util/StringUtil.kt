package com.origeek.imagePicker.util

import java.lang.StringBuilder

/**
 * 对字符串进行拼接
 */
fun joiner(list: List<String>, separate: String = "", arrowStart: String = "", arrowEnd: String = ""): String {
    if (list.isEmpty()) return ""
    val str = StringBuilder()
    for ((index,item) in list.withIndex()) {
        str.append(arrowStart)
        str.append(item)
        str.append(arrowEnd)
        if (index != list.size -1) str.append(separate)
    }
    return str.toString()
}