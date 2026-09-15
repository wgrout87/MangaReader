package com.wgrou.mangareader.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import com.wgrou.mangareader.data.ReadingProgress
import com.wgrou.mangareader.data.Series
import androidx.compose.material3.Surface
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun ChapterListScreen(
    series: Series,
    progress: ReadingProgress?,
    pageCounts: Map<Float, Int>,
    onChapterClick: (Float) -> Unit,
    onMarkRead: (Float) -> Unit,
    onMarkUnread: (Float) -> Unit,
    onMarkSeriesRead: () -> Unit,
    onMarkSeriesUnread: () -> Unit
) {

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

                var seriesMenuExpanded by remember {
                    mutableStateOf(false)
                }

                val allChaptersRead =
                    series.chapters.isNotEmpty() &&
                            series.chapters.all { chapter ->

                                progress
                                    ?.chapters
                                    ?.find {
                                        it.chapter == chapter
                                    }
                                    ?.completed == true

                            }

                val hasAnyProgress =
                    progress?.chapters?.isNotEmpty() == true

                Text(
                    text = "MANGA READER",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = 16.dp
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 20.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = series.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 4.dp)
                    )

                    Box {

                        IconButton(
                            onClick = {
                                seriesMenuExpanded = true
                            }
                        ) {

                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Series options"
                            )

                        }

                        DropdownMenu(
                            expanded = seriesMenuExpanded,
                            onDismissRequest = {
                                seriesMenuExpanded = false
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        ) {

                            if (!allChaptersRead) {

                                DropdownMenuItem(
                                    text = {
                                        Text("Mark Series as Read")
                                    },
                                    onClick = {

                                        seriesMenuExpanded = false
                                        onMarkSeriesRead()

                                    }
                                )

                            }

                            if (hasAnyProgress) {

                                DropdownMenuItem(
                                    text = {
                                        Text("Mark Series as Unread")
                                    },
                                    onClick = {

                                        seriesMenuExpanded = false
                                        onMarkSeriesUnread()

                                    }
                                )

                            }

                        }

                    }

                }

            }

            items(series.chapters.sorted()) { chapter ->

                var menuExpanded by remember {
                    mutableStateOf(false)
                }

                val chapterProgress =
                    progress
                        ?.chapters
                        ?.find {
                            it.chapter == chapter
                        }

                val totalPages =
                    pageCounts[chapter] ?: 0

                val fillFraction =
                    when {

                        chapterProgress == null ->
                            0f

                        chapterProgress.completed ->
                            1f

                        totalPages <= 0 ->
                            0f

                        else ->
                            (
                                    chapterProgress.page.toFloat() /
                                            totalPages.toFloat()
                                    ).coerceIn(
                                    0f,
                                    1f
                                )

                    }

                val progressColor =
                    if (chapterProgress?.completed == true) {

                        MaterialTheme.colorScheme.outline.copy(
                            alpha = 0.50f
                        )

                    } else {

                        MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.50f
                        )

                    }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {

                            drawRect(
                                color = progressColor,
                                size = size.copy(
                                    width = size.width * fillFraction
                                )
                            )

                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Chapter ${formatChapterNumber(chapter)}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onChapterClick(chapter)
                            }
                            .padding(
                                start = 16.dp,
                                top = 12.dp,
                                bottom = 12.dp
                            )
                    )

                    Box {

                        IconButton(
                            onClick = {
                                menuExpanded = true
                            }
                        ) {

                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Chapter options"
                            )

                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = {
                                menuExpanded = false
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        ) {

                            if (chapterProgress?.completed != true) {

                                DropdownMenuItem(
                                    text = {
                                        Text("Mark as Read")
                                    },
                                    onClick = {

                                        menuExpanded = false

                                        onMarkRead(chapter)

                                    }
                                )

                            }

                            if (chapterProgress != null) {

                                DropdownMenuItem(
                                    text = {
                                        Text("Mark as Unread")
                                    },
                                    onClick = {

                                        menuExpanded = false

                                        onMarkUnread(chapter)

                                    }
                                )

                            }

                        }

                    }

                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.65f
                    )
                )

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