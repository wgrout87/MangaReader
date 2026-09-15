package com.wgrou.mangareader.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val InkAndPaperColorScheme = darkColorScheme(
    primary = Vermilion,
    secondary = PaperMuted,

    background = InkBackground,
    surface = InkSurface,

    onPrimary = PaperWhite,
    onSecondary = PaperWhite,
    onBackground = PaperWhite,
    onSurface = PaperWhite,

    outline = InkDivider
)

@Composable
fun MangaReaderTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = InkAndPaperColorScheme,
        typography = Typography,
        content = content
    )
}