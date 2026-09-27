package com.wgrou.mangareader.data

import java.io.File

sealed class ChapterLoadResult {

    data class Success(
        val pages: List<File>
    ) : ChapterLoadResult()

    data class Error(
        val message: String
    ) : ChapterLoadResult()
}

class ChapterLoader(
    private val libraryManager: LibraryManager
) {

    fun load(
        series: Series,
        chapter: Float
    ): ChapterLoadResult {

        return when (
            val result = libraryManager.getPages(
                series,
                chapter
            )
        ) {

            is PageLoadResult.Success -> {
                ChapterLoadResult.Success(
                    pages = result.pages
                )
            }

            PageLoadResult.MissingChapter -> {
                ChapterLoadResult.Error(
                    message = "The files for Chapter ${chapter} could not be found."
                )
            }

            PageLoadResult.NoPages -> {
                ChapterLoadResult.Error(
                    message = "No readable pages were found for Chapter ${chapter}."
                )
            }
        }
    }
}