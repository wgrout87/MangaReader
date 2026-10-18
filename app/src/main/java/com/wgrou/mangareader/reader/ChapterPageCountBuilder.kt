package com.wgrou.mangareader.reader

import com.wgrou.mangareader.data.LibraryManager
import com.wgrou.mangareader.data.Series

class ChapterPageCountBuilder(
    private val libraryManager: LibraryManager
) {

    fun build(
        series: Series
    ): Map<Float, Int> {

        return series.chapters
            .associateWith { chapter ->
                libraryManager.getPageCount(
                    series,
                    chapter
                )
            }
    }
}