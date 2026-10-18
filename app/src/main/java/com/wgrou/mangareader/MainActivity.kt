package com.wgrou.mangareader

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wgrou.mangareader.data.ChapterLoadResult
import com.wgrou.mangareader.data.ChapterLoader
import com.wgrou.mangareader.data.LibraryManager
import com.wgrou.mangareader.data.ProgressManager
import com.wgrou.mangareader.data.Series
import com.wgrou.mangareader.reader.ChapterOpenDecision
import com.wgrou.mangareader.reader.ChapterPageCountBuilder
import com.wgrou.mangareader.reader.ContinueReadingBuilder
import com.wgrou.mangareader.reader.ReaderCoordinator
import com.wgrou.mangareader.reader.ReaderSessionState
import com.wgrou.mangareader.screens.ChapterListScreen
import com.wgrou.mangareader.screens.ChapterLoadErrorScreen
import com.wgrou.mangareader.screens.LibraryScreen
import com.wgrou.mangareader.screens.ReaderScreen
import com.wgrou.mangareader.screens.RereadSeriesDialog
import com.wgrou.mangareader.screens.ResumeChapterDialog
import com.wgrou.mangareader.ui.theme.MangaReaderTheme
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = Color.rgb(
                    18,
                    18,
                    18
                )
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = Color.rgb(
                    18,
                    18,
                    18
                )
            )
        )

        setContent {

            val libraryManager = remember {
                LibraryManager(this)
            }

            val chapterLoader = remember(libraryManager) {
                ChapterLoader(libraryManager)
            }

            val progressManager = remember {
                ProgressManager(this)
            }

            val readerCoordinator = remember {
                ReaderCoordinator()
            }

            val continueReadingBuilder = remember(libraryManager) {
                ContinueReadingBuilder(libraryManager)
            }

            val chapterPageCountBuilder = remember(libraryManager) {
                ChapterPageCountBuilder(libraryManager)
            }

            val readerSession = remember {
                ReaderSessionState()
            }

            MangaReaderTheme {

                val continuousReading = readerSession.continuousReading
                val selectedSeries = readerSession.selectedSeries
                val selectedChapter = readerSession.selectedChapter
                val selectedPages = readerSession.selectedPages
                val currentPage = readerSession.currentPage

                var allProgress by remember {
                    mutableStateOf(
                        progressManager.getAllProgress()
                    )
                }

                fun refreshProgress() {
                    allProgress = progressManager.getAllProgress()
                }

                var pendingChapterSeries by remember {
                    mutableStateOf<Series?>(null)
                }

                var pendingChapter by remember {
                    mutableStateOf<Float?>(null)
                }

                var showResumeDialog by remember {
                    mutableStateOf(false)
                }

                var pendingResumePage by remember {
                    mutableStateOf(1)
                }

                var showRereadDialog by remember {
                    mutableStateOf(false)
                }

                fun clearPendingChapterRequest() {
                    pendingChapterSeries = null
                    pendingChapter = null
                    showResumeDialog = false
                    showRereadDialog = false
                }

                var chapterLoadError by remember {
                    mutableStateOf<String?>(null)
                }

                // Attempts to load the requested chapter's pages.
                // If loading fails, records a user-friendly error message
                // that we'll display instead of opening the Reader.
                fun loadChapterPages(
                    series: Series,
                    chapter: Float
                ): List<File>? {

                    return when (
                        val result = chapterLoader.load(
                            series,
                            chapter
                        )
                    ) {

                        is ChapterLoadResult.Success -> {
                            chapterLoadError = null
                            result.pages
                        }

                        is ChapterLoadResult.Error -> {

                            chapterLoadError =
                                result.message

                            null
                        }
                    }
                }

                fun openChapter(
                    series: Series,
                    chapter: Float,
                    page: Int = 1
                ) {
                    val pages = loadChapterPages(series, chapter)

                    if (pages != null) {
                        readerSession.openChapter(
                            series = series,
                            chapter = chapter,
                            pages = pages,
                            page = page
                        )
                    } else {
                        readerSession.selectSeries(series)
                    }
                }

                fun openChapterAtEnd(
                    series: Series,
                    chapter: Float
                ) {
                    val pages = loadChapterPages(series, chapter)

                    if (pages != null) {
                        readerSession.openChapter(
                            series = series,
                            chapter = chapter,
                            pages = pages,
                            page = pages.size.coerceAtLeast(1)
                        )
                    } else {
                        readerSession.selectSeries(series)
                    }
                }

                fun requestChapterOpen(
                    series: Series,
                    chapter: Float
                ) {
                    readerSession.updateContinuousReading(false)

                    pendingChapterSeries = series
                    pendingChapter = chapter

                    val seriesProgress =
                        allProgress.find {
                            it.seriesTitle == series.title
                        }

                    when (
                        val decision =
                            readerCoordinator.decideChapterOpen(
                                series = series,
                                chapter = chapter,
                                seriesProgress = seriesProgress
                            )
                    ) {
                        is ChapterOpenDecision.OpenChapter -> {

                            openChapter(
                                series = series,
                                chapter = chapter
                            )
                        }

                        is ChapterOpenDecision.ResumeChapter -> {

                            pendingResumePage = decision.page
                            showResumeDialog = true
                        }

                        is ChapterOpenDecision.AskReread -> {
                            showRereadDialog = true
                        }
                    }
                }

                val continueReadingItems =
                    continueReadingBuilder.build(
                        allProgress
                    )

                BackHandler(
                    enabled = selectedSeries != null
                ) {
                    if (selectedPages != null) {
                        refreshProgress()
                        readerSession.closeChapter()
                    } else {
                        readerSession.selectSeries(null)
                    }
                }

                if (showResumeDialog) {

                    ResumeChapterDialog(
                        resumePage = pendingResumePage,

                        onContinue = {
                            val series = pendingChapterSeries
                            val chapter = pendingChapter

                            if (
                                series != null &&
                                chapter != null
                            ) {
                                openChapter(
                                    series = series,
                                    chapter = chapter,
                                    page = pendingResumePage
                                )
                            }

                            clearPendingChapterRequest()
                        },

                        onRestart = {
                            val series = pendingChapterSeries
                            val chapter = pendingChapter

                            if (
                                series != null &&
                                chapter != null
                            ) {
                                openChapter(
                                    series = series,
                                    chapter = chapter
                                )
                            }

                            clearPendingChapterRequest()
                        },

                        onDismiss = {
                            clearPendingChapterRequest()
                        }
                    )
                }

                if (showRereadDialog) {

                    RereadSeriesDialog(
                        onTrackReread = {
                            val series = pendingChapterSeries
                            val chapter = pendingChapter

                            if (
                                series != null &&
                                chapter != null
                            ) {
                                progressManager.markChapterUnread(
                                    seriesTitle = series.title,
                                    chapter = chapter
                                )

                                progressManager.setRereading(
                                    seriesTitle = series.title,
                                    rereading = true
                                )

                                refreshProgress()

                                readerSession.updateContinuousReading(false)

                                openChapter(
                                    series = series,
                                    chapter = chapter
                                )
                            }

                            clearPendingChapterRequest()
                        },

                        onStartOver = {
                            val series = pendingChapterSeries
                            val chapter = pendingChapter

                            if (
                                series != null &&
                                chapter != null
                            ) {
                                progressManager.markSeriesUnread(
                                    series.title
                                )

                                refreshProgress()

                                readerSession.updateContinuousReading(false)

                                openChapter(
                                    series = series,
                                    chapter = chapter
                                )
                            }

                            clearPendingChapterRequest()
                        },

                        onDismiss = {
                            clearPendingChapterRequest()
                        }
                    )
                }

                if (chapterLoadError != null) {

                    ChapterLoadErrorScreen(
                        message = chapterLoadError!!,

                        onChapterList = {
                            chapterLoadError = null
                            readerSession.closeChapter()
                        },

                        onLibrary = {
                            chapterLoadError = null
                            readerSession.selectSeries(null)
                        }
                    )
                } else if (selectedPages != null) {

                    key(selectedChapter) {

                        val hasNextChapter =
                            readerCoordinator.nextChapter(
                                series = selectedSeries!!,
                                currentChapter = selectedChapter!!
                            ) != null

                        val hasPreviousChapter =
                            readerCoordinator.previousChapter(
                                series = selectedSeries!!,
                                currentChapter = selectedChapter!!
                            ) != null

                        ReaderScreen(
                            pages = selectedPages!!,
                            chapter = selectedChapter!!,
                            startPage = currentPage,
                            hasNextChapter = hasNextChapter,

                            onPageChanged = { page ->

                                readerSession.updateCurrentPage(page)

                                progressManager.saveProgress(
                                    seriesTitle = selectedSeries!!.title,
                                    chapter = selectedChapter!!,
                                    page = readerSession.currentPage
                                )

                            },

                            onChapterCompleted = {

                                progressManager.markChapterCompleted(
                                    seriesTitle = selectedSeries!!.title,
                                    chapter = selectedChapter!!,
                                    isFinalChapter = !hasNextChapter
                                )

                                refreshProgress()

                            },

                            onNextChapter = {

                                val nextChapter =
                                    readerCoordinator.nextChapter(
                                        series = selectedSeries!!,
                                        currentChapter = selectedChapter!!
                                    )

                                if (nextChapter != null) {

                                    val seriesProgress =
                                        allProgress.find {
                                            it.seriesTitle == selectedSeries!!.title
                                        }

                                    if (seriesProgress?.rereading == true) {

                                        progressManager.markChapterUnread(
                                            seriesTitle = selectedSeries!!.title,
                                            chapter = nextChapter
                                        )

                                        refreshProgress()

                                    }

                                    readerSession.updateContinuousReading(true)

                                    openChapter(
                                        series = selectedSeries!!,
                                        chapter = nextChapter
                                    )

                                }

                            },

                            onChapterList = {
                                refreshProgress()
                                readerSession.closeChapter()
                            },

                            continuousReading = continuousReading,

                            hasPreviousChapter = hasPreviousChapter,

                            onPreviousChapter = {

                                val previousChapter =
                                    readerCoordinator.previousChapter(
                                        series = selectedSeries!!,
                                        currentChapter = selectedChapter!!
                                    )

                                if (previousChapter != null) {

                                    openChapterAtEnd(
                                        series = selectedSeries!!,
                                        chapter = previousChapter
                                    )

                                }

                            },

                            onPreviousChapterFromStart = {

                                val previousChapter =
                                    readerCoordinator.previousChapter(
                                        series = selectedSeries!!,
                                        currentChapter = selectedChapter!!
                                    )

                                if (previousChapter != null) {

                                    openChapter(
                                        series = selectedSeries!!,
                                        chapter = previousChapter
                                    )

                                }

                            },

                            onLibrary = {
                                refreshProgress()
                                readerSession.selectSeries(null)
                            }
                        )

                    }

                } else if (selectedSeries == null) {

                    LibraryScreen(
                        continueReadingItems = continueReadingItems,
                        allProgress = allProgress,
                        onSeriesClick = { series ->
                            readerSession.selectSeries(series)
                        },
                        // User tapped the chapter shortcut directly from the Library.
                        // If this chapter has partial progress, ask whether to resume or restart.
                        onChapterClick = { series, chapter ->

                            requestChapterOpen(
                                series = series,
                                chapter = chapter
                            )

                        },
                        onContinueReading = { item ->

                            val series =
                                libraryManager.getSeriesByTitle(
                                    item.seriesTitle
                                )

                            if (series != null) {

                                readerSession.updateContinuousReading(false)

                                openChapter(
                                    series = series,
                                    chapter = item.chapter,
                                    page = item.page
                                )

                            }

                        }
                    )

                } else {

                    val pageCounts = remember(selectedSeries) {
                        chapterPageCountBuilder.build(
                            selectedSeries!!
                        )
                    }

                    ChapterListScreen(
                        series = selectedSeries!!,

                        progress =
                            allProgress.find {
                                it.seriesTitle == selectedSeries!!.title
                            },

                        pageCounts = pageCounts,

                        // User selected a chapter from the Chapter List.
                        // As with the Library shortcut, partial progress should prompt
                        // the user before deciding where to begin.
                        onChapterClick = { chapter ->

                            requestChapterOpen(
                                series = selectedSeries!!,
                                chapter = chapter
                            )

                        },

                        onMarkRead = { chapter ->

                            val pageCount =
                                pageCounts[chapter] ?: 0

                            progressManager.markChapterRead(
                                seriesTitle = selectedSeries!!.title,
                                chapter = chapter,
                                page = pageCount
                            )

                            refreshProgress()

                        },

                        onMarkUnread = { chapter ->

                            progressManager.markChapterUnread(
                                seriesTitle = selectedSeries!!.title,
                                chapter = chapter
                            )

                            refreshProgress()

                        },

                        onMarkSeriesRead = {

                            progressManager.markSeriesRead(
                                seriesTitle = selectedSeries!!.title,
                                chapters = selectedSeries!!.chapters,
                                pageCounts = pageCounts
                            )

                            refreshProgress()

                        },

                        onMarkSeriesUnread = {

                            progressManager.markSeriesUnread(
                                seriesTitle = selectedSeries!!.title
                            )

                            refreshProgress()

                        }
                    )

                }

            }

        }
    }

}