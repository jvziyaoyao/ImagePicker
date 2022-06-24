package com.origeek.imagePicker.config

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.intellij.lang.annotations.JdkConstants

const val NO_LIMIT = -1

data class Padding(
    val start: Int = 18,
    val end: Int = 18,
    val top: Int = 8,
    val bottom: Int = 8,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(start)
        parcel.writeInt(end)
        parcel.writeInt(top)
        parcel.writeInt(bottom)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Padding> {
        override fun createFromParcel(parcel: Parcel): Padding {
            return Padding(parcel)
        }

        override fun newArray(size: Int): Array<Padding?> {
            return arrayOfNulls(size)
        }
    }

}

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

    // 文本内容边距
    var textPadding: Padding = Padding(),
    // 背景颜色
    var backgroundColor: Color = Color(0xFFF4F4F4),
    // 默认选中颜色
    var checkColorDefault: Color = Color.White.copy(0.8f),
    // 加载标识的颜色
    var loadingColor: Color = Color.White.copy(0.8f),

    // 预览表面颜色
    var surfaceColor: Color = Color.White.copy(0.8F),
    // tab选中颜色
    var tabCheckColor: Color = Color.Black.copy(0.4F),
    // tab图片方框颜色
    var tabImageCheckBorderColor: Color = Color.Black.copy(0.2F),
    // tab图片选中遮罩颜色
    var tabImageMaskerColor: Color = Color.Black.copy(0.04F),
    // tab图片未选中遮罩颜色
    var tabImageMaskerUncheckedColor: Color = Color.LightGray.copy(0.6F),
    // 预览窗口全屏下背景颜色
    var previewBackgroundColorDark: Color = Color.Black,
    // 预览窗口非全屏下背景颜色
    var previewBackgroundColorLight: Color = Color(0xFFF4F4F4),
) : Parcelable {

    val commonTextPadding = PaddingValues(
        start = textPadding.start.dp,
        end = textPadding.end.dp,
        top = textPadding.top.dp,
        bottom = textPadding.bottom.dp,
    )

    constructor(parcel: Parcel) : this(
        filterMineType = parcel.createStringArrayList() ?: emptyList(),
        limit = parcel.readInt(),
        navTitle = parcel.readString() ?: "",
        textPadding = parcel.readParcelable<Padding>(Padding::class.java.classLoader) ?: Padding(),

        backgroundColor = json2Color(parcel.readString()!!),
        checkColorDefault = json2Color(parcel.readString()!!),
        loadingColor = json2Color(parcel.readString()!!),
        surfaceColor = json2Color(parcel.readString()!!),
        tabCheckColor = json2Color(parcel.readString()!!),
        tabImageCheckBorderColor = json2Color(parcel.readString()!!),
        tabImageMaskerColor = json2Color(parcel.readString()!!),
        tabImageMaskerUncheckedColor = json2Color(parcel.readString()!!),
        previewBackgroundColorDark = json2Color(parcel.readString()!!),
        previewBackgroundColorLight = json2Color(parcel.readString()!!),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(filterMineType)
        parcel.writeInt(limit)
        parcel.writeString(navTitle)
        parcel.writeParcelable(textPadding,0)

        parcel.writeString(toJson(backgroundColor))
        parcel.writeString(toJson(checkColorDefault))
        parcel.writeString(toJson(loadingColor))
        parcel.writeString(toJson(surfaceColor))

        parcel.writeString(toJson(tabCheckColor))
        parcel.writeString(toJson(tabImageCheckBorderColor))
        parcel.writeString(toJson(tabImageMaskerColor))
        parcel.writeString(toJson(tabImageMaskerUncheckedColor))

        parcel.writeString(toJson(previewBackgroundColorDark))
        parcel.writeString(toJson(previewBackgroundColorLight))
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