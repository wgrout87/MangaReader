package com.wgrou.mangareader.data

import android.content.Context
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ProgressManager(
    context: Context
) {

    private val progressFile =
        File(
            context.filesDir,
            "reading_progress.json"
        )

    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

    private fun loadProgress(): MutableList<ReadingProgress> {

        if (!progressFile.exists()) {
            return mutableListOf()
        }

        val text =
            progressFile.readText()

        if (text.isBlank()) {
            return mutableListOf()
        }

        return try {

            json
                .decodeFromString<List<ReadingProgress>>(
                    text
                )
                .toMutableList()

        } catch (error: Exception) {

            println(
                "Failed to load reading progress: ${error.message}"
            )

            mutableListOf()

        }

    }

    private fun saveProgress(
        progress: List<ReadingProgress>
    ) {

        val text =
            json.encodeToString(
                progress
            )

        progressFile.writeText(
            text
        )

    }

    fun saveProgress(
        seriesTitle: String,
        chapter: Float,
        page: Int
    ) {

        val progress =
            loadProgress()

        val existingIndex =
            progress.indexOfFirst {
                it.seriesTitle == seriesTitle
            }

        val existing =
            if (existingIndex >= 0) {
                progress[existingIndex]
            } else {
                null
            }

        val chapterProgress =
            existing
                ?.chapters
                ?.toMutableList()
                ?: mutableListOf()

        val chapterIndex =
            chapterProgress.indexOfFirst {
                it.chapter == chapter
            }

        val existingChapter =
            if (chapterIndex >= 0) {
                chapterProgress[chapterIndex]
            } else {
                null
            }

        val updatedChapter =
            ChapterProgress(
                chapter = chapter,
                page = page,
                completed = existingChapter?.completed ?: false
            )

        if (chapterIndex >= 0) {

            chapterProgress[chapterIndex] =
                updatedChapter

        } else {

            chapterProgress.add(
                updatedChapter
            )

        }

        val updated =
            ReadingProgress(
                seriesTitle = seriesTitle,
                chapter = chapter,
                page = page,
                lastRead = System.currentTimeMillis(),
                active = true,
                rereading = existing?.rereading ?: false,
                chapters = chapterProgress
            )

        if (existingIndex >= 0) {

            progress[existingIndex] =
                updated

        } else {

            progress.add(
                updated
            )

        }

        saveProgress(
            progress
        )

    }

    fun getProgress(
        seriesTitle: String
    ): ReadingProgress? {

        return loadProgress()
            .find {
                it.seriesTitle == seriesTitle
            }

    }

    fun getRecentProgress(
        limit: Int = 5
    ): List<ReadingProgress> {

        return loadProgress()
            .filter {
                it.lastRead != null
            }
            .sortedByDescending {
                it.lastRead
            }
            .take(limit)

    }

    fun getAllProgress(): List<ReadingProgress> {

        return loadProgress()

    }

    fun markChapterCompleted(
        seriesTitle: String,
        chapter: Float,
        isFinalChapter: Boolean
    ) {

        val progress =
            loadProgress()

        val seriesIndex =
            progress.indexOfFirst {
                it.seriesTitle == seriesTitle
            }

        if (seriesIndex < 0) {
            return
        }

        val seriesProgress =
            progress[seriesIndex]

        val chapters =
            seriesProgress.chapters
                .toMutableList()

        val chapterIndex =
            chapters.indexOfFirst {
                it.chapter == chapter
            }

        if (chapterIndex < 0) {
            return
        }

        val existingChapter =
            chapters[chapterIndex]

        chapters[chapterIndex] =
            existingChapter.copy(
                completed = true
            )

        progress[seriesIndex] =
            seriesProgress.copy(
                chapters = chapters,
                lastRead = System.currentTimeMillis(),
                active =
                    if (isFinalChapter) {
                        false
                    } else {
                        seriesProgress.active
                    },
                rereading =
                    if (isFinalChapter) {
                        false
                    } else {
                        seriesProgress.rereading
                    }
            )

        saveProgress(
            progress
        )

    }

    fun markChapterUnread(
        seriesTitle: String,
        chapter: Float
    ) {

        val progress =
            loadProgress()

        val seriesIndex =
            progress.indexOfFirst {
                it.seriesTitle == seriesTitle
            }

        if (seriesIndex < 0) {
            return
        }

        val seriesProgress =
            progress[seriesIndex]

        val chapters =
            seriesProgress.chapters
                .toMutableList()

        val chapterIndex =
            chapters.indexOfFirst {
                it.chapter == chapter
            }

        if (chapterIndex < 0) {
            return
        }

        chapters.removeAt(
            chapterIndex
        )

        progress[seriesIndex] =
            seriesProgress.copy(
                chapters = chapters
            )

        saveProgress(
            progress
        )

    }

    fun markChapterRead(
        seriesTitle: String,
        chapter: Float,
        page: Int
    ) {

        val progress =
            loadProgress()

        val seriesIndex =
            progress.indexOfFirst {
                it.seriesTitle == seriesTitle
            }

        if (seriesIndex >= 0) {

            val seriesProgress =
                progress[seriesIndex]

            val chapters =
                seriesProgress.chapters
                    .toMutableList()

            val chapterIndex =
                chapters.indexOfFirst {
                    it.chapter == chapter
                }

            val updatedChapter =
                ChapterProgress(
                    chapter = chapter,
                    page = page,
                    completed = true
                )

            if (chapterIndex >= 0) {

                chapters[chapterIndex] =
                    updatedChapter

            } else {

                chapters.add(
                    updatedChapter
                )

            }

            progress[seriesIndex] =
                seriesProgress.copy(
                    chapters = chapters
                )

        } else {

            progress.add(
                ReadingProgress(
                    seriesTitle = seriesTitle,
                    chapter = chapter,
                    page = page,
                    lastRead = null,
                    chapters = listOf(
                        ChapterProgress(
                            chapter = chapter,
                            page = page,
                            completed = true
                        )
                    )
                )
            )

        }

        saveProgress(
            progress
        )

    }

    fun markSeriesRead(
        seriesTitle: String,
        chapters: List<Float>,
        pageCounts: Map<Float, Int>
    ) {

        val progress =
            loadProgress()

        val seriesIndex =
            progress.indexOfFirst {
                it.seriesTitle == seriesTitle
            }

        val chapterProgress =
            chapters.map { chapter ->

                ChapterProgress(
                    chapter = chapter,
                    page = pageCounts[chapter] ?: 0,
                    completed = true
                )

            }

        if (seriesIndex >= 0) {

            val existing =
                progress[seriesIndex]

            progress[seriesIndex] =
                existing.copy(
                    chapters = chapterProgress,
                    active = false
                )

        } else {

            progress.add(
                ReadingProgress(
                    seriesTitle = seriesTitle,
                    chapter = chapters.firstOrNull() ?: 0f,
                    page = 1,
                    lastRead = null,
                    active = false,
                    chapters = chapterProgress
                )
            )

        }

        saveProgress(
            progress
        )

    }

    fun markSeriesUnread(
        seriesTitle: String
    ) {

        val progress =
            loadProgress()

        progress.removeAll {
            it.seriesTitle == seriesTitle
        }

        saveProgress(
            progress
        )

    }

    private fun isSeriesCompleted(
        progress: ReadingProgress
    ): Boolean {

        if (progress.chapters.isEmpty()) {
            return false
        }

        return progress.chapters.all {
            it.completed
        }

    }

    fun setRereading(
        seriesTitle: String,
        rereading: Boolean
    ) {

        val progress =
            loadProgress()

        val seriesIndex =
            progress.indexOfFirst {
                it.seriesTitle == seriesTitle
            }

        if (seriesIndex < 0) {
            return
        }

        val existing =
            progress[seriesIndex]

        progress[seriesIndex] =
            existing.copy(
                rereading = rereading
            )

        saveProgress(
            progress
        )
    }

}