package com.wgrou.mangareader.data

import kotlinx.serialization.Serializable

@Serializable
data class Series(

    val version: Int,
    val id: Long?,
    val slug: String?,
    val title: String,
    val type: String,
    val chapters: List<Float>,
    val folderName: String = ""

)