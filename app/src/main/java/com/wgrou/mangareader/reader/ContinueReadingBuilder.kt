package com.wgrou.mangareader.reader

import com.wgrou.mangareader.data.ContinueReadingItem
import com.wgrou.mangareader.data.LibraryManager
import com.wgrou.mangareader.data.ReadingProgress

class ContinueReadingBuilder(
    private val libraryManager: LibraryManager
) {

    fun build(
        allProgress: List<ReadingProgress>
    ): List<ContinueReadingItem> {

        return allProgress
            .filter {
                it.lastRead != null &&
                        it.active
            }
            .sortedByDescending {
                it.lastRead
            }
            .mapNotNull { progress ->

                val series =
                    libraryManager.getSeriesByTitle(
                        progress.seriesTitle
                    )
                        ?: return@mapNotNull null

                val chapters =
                    series.chapters.sorted()

                // Saved progress may refer to a chapter that no longer
                // exists after the library has been changed or resynced.
                if (progress.chapter !in chapters) {
                    return@mapNotNull null
                }

                val savedChapterProgress =
                    progress.chapters.find {
                        it.chapter == progress.chapter
                    }

                val currentIndex =
                    chapters.indexOf(
                        progress.chapter
                    )

                val nextChapter =
                    if (
                        savedChapterProgress?.completed == true &&
                        currentIndex >= 0
                    ) {
                        chapters.getOrNull(
                            currentIndex + 1
                        )
                    } else {
                        null
                    }

                ContinueReadingItem(
                    seriesTitle = progress.seriesTitle,
                    chapter =
                        nextChapter
                            ?: progress.chapter,
                    page =
                        if (nextChapter != null) {
                            1
                        } else {
                            progress.page
                        },
                    progress = progress
                )
            }
            .take(5)
    }
}