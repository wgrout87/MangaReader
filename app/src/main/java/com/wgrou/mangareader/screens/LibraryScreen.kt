package com.wgrou.mangareader.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.wgrou.mangareader.data.LibraryManager
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import com.wgrou.mangareader.data.Series
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.wgrou.mangareader.data.ReadingProgress
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import com.wgrou.mangareader.data.ContinueReadingItem
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight

@Composable
fun LibraryScreen(
    continueReadingItems: List<ContinueReadingItem>,
    allProgress: List<ReadingProgress>,
    onSeriesClick: (Series) -> Unit,
    onChapterClick: (Series, Float) -> Unit,
    onContinueReading: (ContinueReadingItem) -> Unit
) {

    val context = LocalContext.current
    val library = LibraryManager(context)
    val series = library.getSeries()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentPadding = PaddingValues(
                vertical = 16.dp
            )
        ) {

            item {

                Text(
                    text = "MANGA READER",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = 16.dp
                    )
                )

                Text(
                    text = "Library",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 24.dp
                    )
                )

            }

            if (continueReadingItems.isNotEmpty()) {

                item {

                    Text(
                        text = "CONTINUE READING",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 12.dp
                        )
                    )

                    LazyRow(
                        contentPadding = PaddingValues(
                            start = 16.dp
                        )
                    ) {

                        items(continueReadingItems) { item ->

                            Card(
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(270.dp)
                                    .padding(end = 12.dp)
                                    .clickable {
                                        onContinueReading(item)
                                    }
                            ) {

                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {

                                    // Reserved for actual cover artwork later.
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(190.dp),
                                        color = MaterialTheme.colorScheme.background
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "NO\nCOVER",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {

                                        Text(
                                            text = item.seriesTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2
                                        )

                                        Text(
                                            text = "Chapter ${formatChapterNumber(item.chapter)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )

                                        Text(
                                            text = "Page ${item.page}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                            }

                        }

                    }

                }

            }

            item {

                Text(
                    text = "MY MANGA",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 28.dp,
                        bottom = 12.dp
                    )
                )

            }

            items(series) { manga ->

                val progress =
                    allProgress.find {
                        it.seriesTitle == manga.title
                    }

                val displayChapter =
                    progress?.chapter
                        ?: manga.chapters.sorted().firstOrNull()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 4.dp
                        )
                        .clickable {
                            onSeriesClick(manga)
                        }
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 10.dp
                                )
                        ) {

                            Text(
                                text = manga.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )

                            if (displayChapter != null) {

                                Text(
                                    text = "Chapter ${formatChapterNumber(displayChapter)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clickable {
                                            onChapterClick(
                                                manga,
                                                displayChapter
                                            )
                                        }
                                )

                            }

                        }

                        // Reserved for actual cover artwork in V2.
                        Surface(
                            modifier = Modifier
                                .width(58.dp)
                                .fillMaxHeight(),
                            color = MaterialTheme.colorScheme.background
                        ) {

                            Box(
                                contentAlignment = Alignment.Center
                            ) {

                                Text(
                                    text = "NO\nCOVER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                            }

                        }

                    }

                }

            }

        }

    }

}


private fun formatChapterNumber(
    chapter: Float
): String {

    return if (chapter % 1f == 0f) {

        chapter.toInt().toString()

    } else {

        chapter.toString()

    }

}