package com.wgrou.mangareader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.wgrou.mangareader.ui.theme.MangaReaderTheme
import com.wgrou.mangareader.screens.LibraryScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wgrou.mangareader.data.Series
import com.wgrou.mangareader.screens.ChapterListScreen
import com.wgrou.mangareader.data.LibraryManager
import java.io.File
import com.wgrou.mangareader.screens.ReaderScreen
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.key
import com.wgrou.mangareader.data.ProgressManager
import com.wgrou.mangareader.data.ContinueReadingItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import com.wgrou.mangareader.data.PageLoadResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import android.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface

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

            val libraryManager = LibraryManager(this)

            val progressManager = ProgressManager(this)

            MangaReaderTheme {

                var continuousReading by remember {
                    mutableStateOf(false)
                }

                var rereadingSeries by remember {
                    mutableStateOf(false)
                }

                var selectedSeries by remember {
                    mutableStateOf<Series?>(null)
                }

                var selectedChapter by remember {
                    mutableStateOf<Float?>(null)
                }

                var selectedPages by remember {
                    mutableStateOf<List<File>?>(null)
                }

                var currentPage by remember {
                    mutableStateOf(1)
                }

                var allProgress by remember {
                    mutableStateOf(
                        progressManager.getAllProgress()
                    )
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

                var pendingRereadSeries by remember {
                    mutableStateOf<Series?>(null)
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
                        val result =
                            libraryManager.getPages(
                                series,
                                chapter
                            )
                    ) {

                        is PageLoadResult.Success -> {

                            chapterLoadError = null

                            if (result.pages.isNotEmpty()) {

                                currentPage =
                                    currentPage.coerceIn(
                                        1,
                                        result.pages.size
                                    )

                            }

                            result.pages

                        }

                        PageLoadResult.MissingChapter -> {

                            chapterLoadError =
                                "The files for Chapter ${chapter} could not be found."

                            null

                        }

                        PageLoadResult.NoPages -> {

                            chapterLoadError =
                                "No readable pages were found for Chapter ${chapter}."

                            null

                        }

                    }

                }

                val continueReadingItems =
                    allProgress
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

                BackHandler(
                    enabled = selectedSeries != null
                ) {

                    if (selectedPages != null) {

                        selectedPages = null

                    } else {

                        selectedSeries = null

                    }

                }

                if (showResumeDialog) {

                    AlertDialog(
                        onDismissRequest = {

                            showResumeDialog = false
                            pendingChapterSeries = null
                            pendingChapter = null

                        },

                        title = {

                            Text("Resume Chapter?")

                        },

                        text = {

                            Text(
                                "Continue from page $pendingResumePage or restart from page 1?"
                            )

                        },

                        confirmButton = {

                            TextButton(
                                onClick = {

                                    val series =
                                        pendingChapterSeries

                                    val chapter =
                                        pendingChapter

                                    if (
                                        series != null &&
                                        chapter != null
                                    ) {

                                        selectedSeries = series
                                        selectedChapter = chapter
                                        currentPage = pendingResumePage

                                        selectedPages =
                                            loadChapterPages(
                                                series,
                                                chapter
                                            )

                                    }

                                    showResumeDialog = false
                                    pendingChapterSeries = null
                                    pendingChapter = null

                                }
                            ) {

                                Text(
                                    "Continue from page $pendingResumePage"
                                )

                            }

                        },

                        dismissButton = {

                            TextButton(
                                onClick = {

                                    val series =
                                        pendingChapterSeries

                                    val chapter =
                                        pendingChapter

                                    if (
                                        series != null &&
                                        chapter != null
                                    ) {

                                        selectedSeries = series
                                        selectedChapter = chapter
                                        currentPage = 1

                                        selectedPages =
                                            loadChapterPages(
                                                series,
                                                chapter
                                            )

                                    }

                                    showResumeDialog = false
                                    pendingChapterSeries = null
                                    pendingChapter = null

                                }
                            ) {

                                Text("Start from page 1")

                            }

                        },

                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        textContentColor = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(12.dp)
                    )

                }

                if (showRereadDialog) {

                    AlertDialog(
                        onDismissRequest = {

                            showRereadDialog = false
                            pendingRereadSeries = null
                            pendingChapterSeries = null
                            pendingChapter = null

                        },

                        title = {

                            Text("Reread this series?")

                        },

                        text = {

                            Text(
                                "You have already completed this series. " +
                                        "Would you like to track a reread while keeping your read history, " +
                                        "or start the series over and mark it unread?"
                            )

                        },

                        confirmButton = {

                            TextButton(
                                onClick = {

                                    val series =
                                        pendingChapterSeries

                                    val chapter =
                                        pendingChapter

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

                                        allProgress =
                                            progressManager.getAllProgress()

                                        continuousReading = false

                                        selectedSeries = series
                                        selectedChapter = chapter
                                        currentPage = 1

                                        selectedPages =
                                            loadChapterPages(
                                                series,
                                                chapter
                                            )

                                    }

                                    showRereadDialog = false
                                    pendingRereadSeries = null
                                    pendingChapterSeries = null
                                    pendingChapter = null

                                }
                            ) {

                                Text("Track reread")

                            }

                        },

                        dismissButton = {

                            TextButton(
                                onClick = {

                                    val series =
                                        pendingChapterSeries

                                    val chapter =
                                        pendingChapter

                                    if (
                                        series != null &&
                                        chapter != null
                                    ) {

                                        progressManager.markSeriesUnread(
                                            series.title
                                        )

                                        allProgress =
                                            progressManager.getAllProgress()

                                        continuousReading = false

                                        selectedSeries = series
                                        selectedChapter = chapter
                                        currentPage = 1

                                        selectedPages =
                                            loadChapterPages(
                                                series,
                                                chapter
                                            )

                                    }

                                    showRereadDialog = false
                                    pendingRereadSeries = null
                                    pendingChapterSeries = null
                                    pendingChapter = null

                                }
                            ) {

                                Text("Start series over")

                            }

                        },

                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        textContentColor = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(12.dp)
                    )

                }

                if (chapterLoadError != null) {

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .safeDrawingPadding()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {

                            Text(
                                text = chapterLoadError!!
                            )

                            Button(
                                onClick = {

                                    chapterLoadError = null
                                    selectedPages = null

                                },
                                modifier = Modifier.padding(
                                    top = 24.dp
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {

                                Text("Chapter List")

                            }

                            Button(
                                onClick = {

                                    chapterLoadError = null
                                    selectedPages = null
                                    selectedChapter = null
                                    selectedSeries = null
                                    currentPage = 1
                                    continuousReading = false

                                },
                                modifier = Modifier.padding(
                                    top = 12.dp
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {

                                Text("Library")

                            }

                        }

                    }

                } else if (selectedPages != null) {

                    key(selectedChapter) {

                        val chapters =
                            selectedSeries!!.chapters.sorted()

                        val currentChapterIndex =
                            chapters.indexOf(
                                selectedChapter
                            )

                        val hasNextChapter =
                            currentChapterIndex >= 0 &&
                                    currentChapterIndex < chapters.lastIndex

                        val hasPreviousChapter =
                            currentChapterIndex > 0

                        ReaderScreen(
                            pages = selectedPages!!,
                            chapter = selectedChapter!!,
                            startPage = currentPage,
                            hasNextChapter = hasNextChapter,

                            onPageChanged = { page ->

                                currentPage = page

                                progressManager.saveProgress(
                                    seriesTitle = selectedSeries!!.title,
                                    chapter = selectedChapter!!,
                                    page = currentPage
                                )

                                allProgress =
                                    progressManager.getAllProgress()

                            },

                            onChapterCompleted = {

                                progressManager.markChapterCompleted(
                                    seriesTitle = selectedSeries!!.title,
                                    chapter = selectedChapter!!,
                                    isFinalChapter = !hasNextChapter
                                )

                                allProgress =
                                    progressManager.getAllProgress()

                            },

                            onNextChapter = {

                                val chapters =
                                    selectedSeries!!.chapters.sorted()

                                val currentIndex =
                                    chapters.indexOf(
                                        selectedChapter
                                    )

                                val nextChapter =
                                    chapters.getOrNull(
                                        currentIndex + 1
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

                                        allProgress =
                                            progressManager.getAllProgress()

                                    }

                                    continuousReading = true

                                    selectedChapter =
                                        nextChapter

                                    currentPage = 1

                                    selectedPages =
                                        loadChapterPages(
                                            selectedSeries!!,
                                            nextChapter
                                        )

                                }

                            },

                            onChapterList = {

                                continuousReading = false
                                selectedPages = null
                                currentPage = 1

                            },

                            continuousReading = continuousReading,

                            hasPreviousChapter = hasPreviousChapter,

                            onPreviousChapter = {

                                val chapters =
                                    selectedSeries!!.chapters.sorted()

                                val currentIndex =
                                    chapters.indexOf(
                                        selectedChapter
                                    )

                                val previousChapter =
                                    chapters.getOrNull(
                                        currentIndex - 1
                                    )

                                if (previousChapter != null) {

                                    selectedChapter =
                                        previousChapter

                                    val pages =
                                        loadChapterPages(
                                            selectedSeries!!,
                                            previousChapter
                                        )

                                    if (pages != null) {

                                        currentPage =
                                            pages.size.coerceAtLeast(1)

                                        selectedPages = pages

                                    }

                                }

                            },

                            onPreviousChapterFromStart = {

                                val chapters =
                                    selectedSeries!!.chapters.sorted()

                                val currentIndex =
                                    chapters.indexOf(
                                        selectedChapter
                                    )

                                val previousChapter =
                                    chapters.getOrNull(
                                        currentIndex - 1
                                    )

                                if (previousChapter != null) {

                                    selectedChapter =
                                        previousChapter

                                    currentPage = 1

                                    selectedPages =
                                        loadChapterPages(
                                            selectedSeries!!,
                                            previousChapter
                                        )

                                }

                            },

                            onLibrary = {

                                continuousReading = false
                                selectedPages = null
                                selectedChapter = null
                                selectedSeries = null
                                currentPage = 1

                            }
                        )

                    }

                } else if (selectedSeries == null) {

                    LibraryScreen(
                        continueReadingItems = continueReadingItems,
                        allProgress = allProgress,
                        onSeriesClick = { series ->
                            selectedSeries = series
                        },
                        // User tapped the chapter shortcut directly from the Library.
// If this chapter has partial progress, ask whether to resume or restart.
                        onChapterClick = { series, chapter ->

                            continuousReading = false

                            // Remember what the user is trying to open while we decide
                            // whether a resume dialog is necessary.
                            pendingChapterSeries = series
                            pendingChapter = chapter

                            val seriesProgress =
                                allProgress.find {
                                    it.seriesTitle == series.title
                                }

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

                                pendingRereadSeries = series
                                showRereadDialog = true

                            } else {

                                // Look for saved progress specifically for this chapter.
                                val chapterProgress =
                                    allProgress
                                        .find {
                                            it.seriesTitle == series.title
                                        }
                                        ?.chapters
                                        ?.find {
                                            it.chapter == chapter
                                        }

                                // A partially read chapter gets a Resume / Restart prompt.
                                if (
                                    chapterProgress != null &&
                                    !chapterProgress.completed &&
                                    chapterProgress.page > 1
                                ) {

                                    pendingResumePage =
                                        chapterProgress.page

                                    showResumeDialog = true

                                } else {

                                    // No partial progress, so open normally at page 1.
                                    selectedSeries = series
                                    selectedChapter = chapter
                                    currentPage = 1

                                    selectedPages =
                                        loadChapterPages(
                                            series,
                                            chapter
                                        )

                                }
                            }

                        },
                        onContinueReading = { item ->

                            val series =
                                libraryManager.getSeriesByTitle(
                                    item.seriesTitle
                                )

                            if (series != null) {

                                continuousReading = false

                                selectedSeries = series
                                selectedChapter = item.chapter
                                currentPage = item.page
                                selectedPages =
                                    loadChapterPages(
                                        series,
                                        item.chapter
                                    )

                            }

                        }
                    )

                } else {

                    val pageCounts =
                        selectedSeries!!.chapters
                            .associateWith { chapter ->

                                libraryManager.getPageCount(
                                    selectedSeries!!,
                                    chapter
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

                            continuousReading = false

                            val series =
                                selectedSeries!!

                            // Remember the requested destination while we decide
                            // whether to show a reread or resume dialog.
                            pendingChapterSeries = series
                            pendingChapter = chapter

                            val seriesProgress =
                                allProgress.find {
                                    it.seriesTitle == series.title
                                }

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

                                // The series has reading history but is not currently active.
                                // Ask how the user wants to handle a reread.
                                pendingRereadSeries = series
                                showRereadDialog = true

                            } else {

                                // Otherwise, fall back to the normal partial-progress logic.
                                val chapterProgress =
                                    seriesProgress
                                        ?.chapters
                                        ?.find {
                                            it.chapter == chapter
                                        }

                                if (
                                    chapterProgress != null &&
                                    !chapterProgress.completed &&
                                    chapterProgress.page > 1
                                ) {

                                    pendingResumePage =
                                        chapterProgress.page

                                    showResumeDialog = true

                                } else {

                                    selectedChapter = chapter
                                    currentPage = 1

                                    selectedPages =
                                        loadChapterPages(
                                            series,
                                            chapter
                                        )

                                }

                            }

                        },

                        onMarkRead = { chapter ->

                            val pageCount =
                                pageCounts[chapter] ?: 0

                            progressManager.markChapterRead(
                                seriesTitle = selectedSeries!!.title,
                                chapter = chapter,
                                page = pageCount
                            )

                            allProgress =
                                progressManager.getAllProgress()

                        },

                        onMarkUnread = { chapter ->

                            progressManager.markChapterUnread(
                                seriesTitle = selectedSeries!!.title,
                                chapter = chapter
                            )

                            allProgress =
                                progressManager.getAllProgress()

                        },

                        onMarkSeriesRead = {

                            progressManager.markSeriesRead(
                                seriesTitle = selectedSeries!!.title,
                                chapters = selectedSeries!!.chapters,
                                pageCounts = pageCounts
                            )

                            allProgress =
                                progressManager.getAllProgress()

                        },

                        onMarkSeriesUnread = {

                            progressManager.markSeriesUnread(
                                seriesTitle = selectedSeries!!.title
                            )

                            allProgress =
                                progressManager.getAllProgress()

                        }
                    )

                }

            }

        }
    }

}