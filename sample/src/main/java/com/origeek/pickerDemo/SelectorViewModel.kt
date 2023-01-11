package com.origeek.pickerDemo

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

/**
 * @program: ImagePicker
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-26 10:34
 **/

class SelectorViewModel : ViewModel() {

    // 选择中列表
    val selectedList = mutableStateListOf<SelectedImage>()

    /**
     * 添加到列表
     * @param list List<SelectedImage>
     */
    fun addList(list: List<SelectedImage>) {
        selectedList.addAll(list)
    }

}