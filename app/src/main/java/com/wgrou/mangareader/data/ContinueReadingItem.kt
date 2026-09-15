package com.wgrou.mangareader.data

data class ContinueReadingItem(
    val seriesTitle: String,
    val chapter: Float,
    val page: Int,
    val progress: ReadingProgress
)