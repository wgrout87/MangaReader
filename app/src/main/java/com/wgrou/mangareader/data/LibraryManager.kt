package com.wgrou.mangareader.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File
import com.wgrou.mangareader.data.PageLoadResult

class LibraryManager(
    private val context: Context
) {

    private val libraryFolder =
        File(context.filesDir, "Library")

    init {
        libraryFolder.mkdirs()
    }
    fun getSeries(): List<Series> {

        return libraryFolder
            .listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull { folder ->

                try {

                    readSeries(folder)

                } catch (error: Exception) {

                    println(
                        "Failed to load series from ${folder.name}: ${error.message}"
                    )

                    null

                }

            }
            ?.sortedBy { series ->
                series.title.lowercase()
            }
            ?: emptyList()

    }

    private fun readSeries(
        folder: File
    ): Series {

        val jsonText =
            File(
                folder,
                "series.json"
            ).readText()

        val series =
            Json {
                ignoreUnknownKeys = true
            }.decodeFromString<Series>(
                jsonText
            )

        return series.copy(
            folderName = folder.name
        )

    }

    fun getPages(
        series: Series,
        chapter: Float
    ): PageLoadResult {

        val chapterFolder =
            File(
                File(
                    libraryFolder,
                    series.folderName
                ),
                "Chapter ${formatChapterNumber(chapter)}"
            )

        if (!chapterFolder.exists()) {

            return PageLoadResult.MissingChapter

        }

        val pages =
            chapterFolder
                .listFiles()
                ?.filter { file ->
                    file.isFile &&
                            file.name != "chapter.json"
                }
                ?.sortedBy { it.name }
                ?: emptyList()

        if (pages.isEmpty()) {

            return PageLoadResult.NoPages

        }

        return PageLoadResult.Success(
            pages
        )

    }

    fun getSeriesByTitle(
        title: String
    ): Series? {

        return getSeries()
            .find { series ->
                series.title == title
            }

    }

    private fun formatChapterNumber(
        chapter: Float
    ): String {

        val wholeNumber = chapter.toInt()

        return if (chapter == wholeNumber.toFloat()) {

            wholeNumber
                .toString()
                .padStart(3, '0')

        } else {

            val decimal =
                chapter
                    .toString()
                    .substringAfter(".")

            "${wholeNumber.toString().padStart(3, '0')}.$decimal"

        }

    }

    fun getPageCount(
        series: Series,
        chapter: Float
    ): Int {

        return when (
            val result =
                getPages(
                    series,
                    chapter
                )
        ) {

            is PageLoadResult.Success ->
                result.pages.size

            PageLoadResult.MissingChapter ->
                0

            PageLoadResult.NoPages ->
                0

        }

    }

}