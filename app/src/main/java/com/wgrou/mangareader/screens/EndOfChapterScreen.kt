package com.wgrou.mangareader.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.style.TextAlign

@Composable
fun EndOfChapterScreen(
    onNextChapter: (() -> Unit)?,
    onChapterList: () -> Unit,
    onLibrary: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp)
            .offset(y = (-40).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "End of Chapter",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(
                bottom = 24.dp
            )
        )

        if (onNextChapter != null) {

            Text(
                text = "Next Chapter",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNextChapter()
                    }
                    .padding(vertical = 12.dp)
            )

            HorizontalDivider()

        }

        Text(
            text = "Chapter List",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onChapterList()
                }
                .padding(vertical = 12.dp)
        )

        HorizontalDivider()

        Text(
            text = "Library",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onLibrary()
                }
                .padding(vertical = 12.dp)
        )

        HorizontalDivider()

    }

}