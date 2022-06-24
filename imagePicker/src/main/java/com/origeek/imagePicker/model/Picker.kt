package com.origeek.imagePicker.model

data class PhotoQueryEntity(
    var id: Long? = null,
    var mimeType: String? = null,
    var width: Long? = null,
    var height: Long? = null,
    var name: String? = null,
    var path: String? = null,
    var size: Long? = null,
    var time: Long? = null,
)

data class AlbumEntity(
    var path: String,
    var name: String,
    var list: List<PhotoQueryEntity>
)