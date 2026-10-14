package com.wgrou.mangareader.reader

import com.wgrou.mangareader.data.ReadingProgress
import com.wgrou.mangareader.data.Series

sealed class ChapterOpenDecision {

    data object OpenChapter : ChapterOpenDecision()

    data class ResumeChapter(
        val page: Int
    ) : ChapterOpenDecision()

    data object AskReread : ChapterOpenDecision()
}

class ReaderCoordinator {

    fun nextChapter(
        series: Series,
        currentChapter: Float
    ): Float? {
        val chapters = series.chapters.sorted()
        val index = chapters.indexOf(currentChapter)

        return if (index >= 0) {
            chapters.getOrNull(index + 1)
        } else {
            null
        }
    }

    fun previousChapter(
        series: Series,
        currentChapter: Float
    ): Float? {
        val chapters = series.chapters.sorted()
        val index = chapters.indexOf(currentChapter)

        return if (index > 0) {
            chapters.getOrNull(index - 1)
        } else {
            null
        }
    }

    fun decideChapterOpen(
        series: Series,
        chapter: Float,
        seriesProgress: ReadingProgress?
    ): ChapterOpenDecision {

        val finalChapter =
            series.chapters
                .sorted()
                .lastOrNull()

        val finalChapterCompleted =
            finalChapter != null &&
                    seriesProgress
                        ?.chapters
                        ?.find {
                            it.chapter == finalChapter
                        }
                        ?.completed == true

        if (
            seriesProgress != null &&
            !seriesProgress.active &&
            finalChapterCompleted
        ) {
            return ChapterOpenDecision.AskReread
        }

        val chapterProgress =
            seriesProgress
                ?.chapters
                ?.find {
                    it.chapter == chapter
                }

        return if (
            chapterProgress != null &&
            !chapterProgress.completed &&
            chapterProgress.page > 1
        ) {
            ChapterOpenDecision.ResumeChapter(
                page = chapterProgress.page
            )
        } else {
            ChapterOpenDecision.OpenChapter
        }
    }
}