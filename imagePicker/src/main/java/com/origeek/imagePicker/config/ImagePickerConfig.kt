package com.origeek.imagePicker.config

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

const val NO_LIMIT = -1

val gson = Gson()

fun json2Color(json: String): Color {
    return gson.fromJson(json,object : TypeToken<Color>(){}.type)
}

fun toJson(any: Any): String = gson.toJson(any)

data class ImagePickerConfig(
    // 需要过滤的mime-type
    val filterMineType: List<String> = emptyList(),
    // 限制图片选择的数量
    val limit: Int = NO_LIMIT,
    // picker页面标题文字
    var navTitle: String = "",
    // 背景颜色
    var backgroundColor: Color = Color(0xFFF4F4F4),
    // 深色背景
    var backgroundColorDark: Color = Color(0xFF000000),
    // 默认选中颜色
    var checkColorDefault: Color = Color(0xCCFFFFFF),
    // 加载标识的颜色
    var loadingColor: Color = Color(0xCCFFFFFF),
    // 表面颜色
    var surfaceColor: Color = Color(0xFFFFFFFF),
    // 预览表面颜色
    var previewSurfaceColor: Color = Color(0xCCFFFFFF),

    // 方格中选中时的遮罩颜色
    var checkMaskerColor: Color = Color(0x8F000000),
    // 方格中未选中的遮罩颜色
    var uncheckMaskerColor: Color = Color(0x0F000000),

    // tab选中颜色
    var tabCheckColor: Color = Color(0x66000000),
    // tab图片方框颜色
    var tabImageCheckBorderColor: Color = Color(0x33000000),
    // tab图片选中遮罩颜色
    var tabImageMaskerColor: Color = Color(0x14000000),
    // tab图片未选中遮罩颜色
    var tabImageMaskerUncheckedColor: Color = Color(0x99CCCCCC),
) : Parcelable {

    constructor(parcel: Parcel) : this(
        filterMineType = parcel.createStringArrayList() ?: emptyList(),
        limit = parcel.readInt(),
        navTitle = parcel.readString() ?: "",
        backgroundColor = json2Color(parcel.readString()!!),
        backgroundColorDark = json2Color(parcel.readString()!!),
        checkColorDefault = json2Color(parcel.readString()!!),
        loadingColor = json2Color(parcel.readString()!!),
        surfaceColor = json2Color(parcel.readString()!!),
        previewSurfaceColor = json2Color(parcel.readString()!!),

        checkMaskerColor = json2Color(parcel.readString()!!),
        uncheckMaskerColor = json2Color(parcel.readString()!!),

        tabCheckColor = json2Color(parcel.readString()!!),
        tabImageCheckBorderColor = json2Color(parcel.readString()!!),
        tabImageMaskerColor = json2Color(parcel.readString()!!),
        tabImageMaskerUncheckedColor = json2Color(parcel.readString()!!),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(filterMineType)
        parcel.writeInt(limit)
        parcel.writeString(navTitle)

        parcel.writeString(toJson(backgroundColor))
        parcel.writeString(toJson(backgroundColorDark))
        parcel.writeString(toJson(checkColorDefault))
        parcel.writeString(toJson(loadingColor))
        parcel.writeString(toJson(surfaceColor))
        parcel.writeString(toJson(previewSurfaceColor))

        parcel.writeString(toJson(checkMaskerColor))
        parcel.writeString(toJson(uncheckMaskerColor))

        parcel.writeString(toJson(tabCheckColor))
        parcel.writeString(toJson(tabImageCheckBorderColor))
        parcel.writeString(toJson(tabImageMaskerColor))
        parcel.writeString(toJson(tabImageMaskerUncheckedColor))
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImagePickerConfig> {
        override fun createFromParcel(parcel: Parcel): ImagePickerConfig {
            return ImagePickerConfig(parcel)
        }

        override fun newArray(size: Int): Array<ImagePickerConfig?> {
            return arrayOfNulls(size)
        }
    }
}