package com.wgrou.mangareader.reader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wgrou.mangareader.data.Series
import java.io.File

class ReaderSessionState {

    var selectedSeries by mutableStateOf<Series?>(null)
        private set

    var selectedChapter by mutableStateOf<Float?>(null)
        private set

    var selectedPages by mutableStateOf<List<File>?>(null)
        private set

    var currentPage by mutableStateOf(1)
        private set

    var continuousReading by mutableStateOf(false)
        private set

    fun openChapter(
        series: Series,
        chapter: Float,
        pages: List<File>,
        page: Int = 1
    ) {
        selectedSeries = series
        selectedChapter = chapter
        selectedPages = pages
        currentPage = page.coerceIn(1, pages.size.coerceAtLeast(1))
    }

    fun closeChapter() {
        selectedPages = null
        selectedChapter = null
        currentPage = 1
        continuousReading = false
    }

    fun updateCurrentPage(page: Int) {
        currentPage = page.coerceIn(
            1,
            selectedPages?.size?.coerceAtLeast(1) ?: 1
        )
    }

    fun updateContinuousReading(enabled: Boolean) {
        continuousReading = enabled
    }

    fun selectSeries(series: Series?) {
        selectedSeries = series
        selectedChapter = null
        selectedPages = null
        currentPage = 1
        continuousReading = false
    }
}