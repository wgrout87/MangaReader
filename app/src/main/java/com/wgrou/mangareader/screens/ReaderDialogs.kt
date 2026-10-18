package com.wgrou.mangareader.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

@Composable
fun ResumeChapterDialog(
    resumePage: Int,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Resume Chapter?")
        },

        text = {
            Text(
                "Continue from page $resumePage or restart from page 1?"
            )
        },

        confirmButton = {
            TextButton(
                onClick = onContinue
            ) {
                Text(
                    "Continue from page $resumePage"
                )
            }
        },

        dismissButton = {
            TextButton(
                onClick = onRestart
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

@Composable
fun RereadSeriesDialog(
    onTrackReread: () -> Unit,
    onStartOver: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,

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
                onClick = onTrackReread
            ) {
                Text("Track reread")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onStartOver
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