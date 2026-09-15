package com.wgrou.mangareader.data

import kotlinx.serialization.Serializable

@Serializable
data class ChapterProgress(
    val chapter: Float,
    val page: Int,
    val completed: Boolean = false
)