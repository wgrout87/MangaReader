package com.wgrou.mangareader.data

import kotlinx.serialization.Serializable

@Serializable
data class ReadingProgress(
    val seriesTitle: String,
    val chapter: Float,
    val page: Int,
    val lastRead: Long? = null,
    val active: Boolean = false,
    val rereading: Boolean = false,
    val chapters: List<ChapterProgress> = emptyList()
)