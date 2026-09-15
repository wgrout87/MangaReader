package com.wgrou.mangareader.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import java.io.File
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Slider
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.SliderDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.clipRect
import kotlin.random.Random
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    pages: List<File>,
    chapter: Float,
    startPage: Int,
    onPageChanged: (Int) -> Unit,
    onChapterCompleted: () -> Unit,
    hasNextChapter: Boolean,
    hasPreviousChapter: Boolean,
    continuousReading: Boolean,
    onNextChapter: () -> Unit,
    onPreviousChapter: () -> Unit,
    onPreviousChapterFromStart: () -> Unit,
    onChapterList: () -> Unit,
    onLibrary: () -> Unit
) {

    val previousPageOffset =
        if (
            continuousReading &&
            hasPreviousChapter
        ) {
            1
        } else {
            0
        }

    val pagerState = rememberPagerState(
        initialPage =
            startPage - 1 + previousPageOffset,
        pageCount = {
            pages.size +
                    previousPageOffset +
                    if (hasNextChapter) {
                        2
                    } else {
                        1
                    }
        }
    )

    var showOverlay by remember {
        mutableStateOf(false)
    }

    val coroutineScope =
        rememberCoroutineScope()

    val sliderInteractionSource = remember {
        MutableInteractionSource()
    }

    LaunchedEffect(
        pagerState.currentPage
    ) {

        val pageIndex =
            pagerState.currentPage -
                    previousPageOffset

        when {

            pageIndex < 0 -> {

                onPreviousChapter()

            }

            pageIndex < pages.size -> {

                onPageChanged(
                    pageIndex + 1
                )

            }

            pageIndex == pages.size -> {

                onChapterCompleted()

            }

            pageIndex > pages.size -> {

                onNextChapter()

            }

        }

    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true
            ) { pageIndex ->

                val actualPageIndex =
                    pageIndex - previousPageOffset

                when {

                    actualPageIndex < 0 -> {

                        Box(
                            modifier = Modifier.fillMaxSize()
                        )

                    }

                    actualPageIndex < pages.size -> {
                        val page = pages[actualPageIndex]

                        var scale by remember(page) {
                            mutableStateOf(1f)
                        }

                        var offsetX by remember(page) {
                            mutableStateOf(0f)
                        }

                        var offsetY by remember(page) {
                            mutableStateOf(0f)
                        }

                        // Stable pseudo-random paper grain for this page.
                        // Remembering it prevents the texture from changing during recomposition.
                        val paperGrain = remember(page) {
                            val random = Random(page.name.hashCode())

                            List(5000) {
                                Triple(
                                    random.nextFloat(),
                                    random.nextFloat(),
                                    random.nextFloat()
                                )
                            }
                        }

                        // Large, very subtle variations in the paper tone.
                        // These prevent the warm paper from looking perfectly uniform.
                        val paperVariation = remember(page) {
                            val random = Random(page.name.hashCode() + 1)

                            List(12) {
                                Triple(
                                    random.nextFloat(),
                                    random.nextFloat(),
                                    random.nextFloat()
                                )
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                        AsyncImage(
                            model = page,
                            contentDescription = "Manga page ${actualPageIndex + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(page) {
                                    awaitEachGesture {

                                        var gestureStartDistance: Float? = null
                                        var gestureStartScale = scale
                                        var smoothedDistance: Float? = null

                                        var panStartCenter: Offset? = null
                                        var panStartOffsetX = offsetX
                                        var panStartOffsetY = offsetY

                                        var transformGestureStarted = false

                                        do {
                                            val event = awaitPointerEvent()

                                            val pressedPointers =
                                                event.changes.filter {
                                                    it.pressed
                                                }

                                            if (pressedPointers.size >= 2) {

                                                transformGestureStarted = true

                                                val first =
                                                    pressedPointers[0].position

                                                val second =
                                                    pressedPointers[1].position

                                                val distance =
                                                    (first - second).getDistance()

                                                val center =
                                                    Offset(
                                                        x = (first.x + second.x) / 2f,
                                                        y = (first.y + second.y) / 2f
                                                    )

                                                val startDistance =
                                                    gestureStartDistance

                                                if (startDistance == null) {

                                                    // Capture stable references when the
                                                    // two-finger gesture begins.
                                                    gestureStartDistance = distance
                                                    gestureStartScale = scale
                                                    smoothedDistance = distance

                                                } else if (startDistance > 0f) {

                                                    val previousSmoothedDistance =
                                                        smoothedDistance ?: distance

                                                    // Low-pass filter the raw touchscreen measurement.
                                                    // Smaller values are steadier but respond more slowly.
                                                    val smoothingFactor = 0.25f

                                                    val newSmoothedDistance =
                                                        previousSmoothedDistance +
                                                                (distance - previousSmoothedDistance) *
                                                                smoothingFactor

                                                    smoothedDistance =
                                                        newSmoothedDistance

                                                    val zoomRatio =
                                                        newSmoothedDistance / startDistance

                                                    val newScale =
                                                        (gestureStartScale * zoomRatio)
                                                            .coerceIn(
                                                                1f,
                                                                4f
                                                            )

                                                    if (
                                                        kotlin.math.abs(
                                                            newScale - scale
                                                        ) >= 0.01f
                                                    ) {
                                                        scale = newScale

                                                        if (scale == 1f) {
                                                            offsetX = 0f
                                                            offsetY = 0f
                                                        }
                                                    }
                                                }

                                                val startCenter =
                                                    panStartCenter

                                                if (startCenter == null) {

                                                    // Anchor this two-finger pan to where the
                                                    // fingers and image were when it began.
                                                    panStartCenter = center
                                                    panStartOffsetX = offsetX
                                                    panStartOffsetY = offsetY

                                                } else if (scale > 1f) {

                                                    val totalMovement =
                                                        center - startCenter

                                                    val maxOffsetX =
                                                        (size.width * (scale - 1f)) / 2f

                                                    val maxOffsetY =
                                                        (size.height * (scale - 1f)) / 2f

                                                    /*
                                                     * The complete pan range at a given scale is
                                                     * (scale - 1) viewport lengths.
                                                     *
                                                     * Therefore one viewport-length of finger travel
                                                     * should be capable of traversing that entire range.
                                                     */
                                                    val panMultiplier =
                                                        scale - 1f

                                                    offsetX =
                                                        (
                                                                panStartOffsetX +
                                                                        totalMovement.x * panMultiplier
                                                                ).coerceIn(
                                                                -maxOffsetX,
                                                                maxOffsetX
                                                            )

                                                    offsetY =
                                                        (
                                                                panStartOffsetY +
                                                                        totalMovement.y * panMultiplier
                                                                ).coerceIn(
                                                                -maxOffsetY,
                                                                maxOffsetY
                                                            )
                                                }

                                                // Consume only once this becomes
                                                // a two-finger gesture.
                                                pressedPointers.forEach {
                                                    it.consume()
                                                }

                                            } else {

                                                gestureStartDistance = null
                                                smoothedDistance = null

                                                if (transformGestureStarted) {
                                                    event.changes.forEach {
                                                        it.consume()
                                                    }
                                                }
                                            }

                                        } while (
                                            event.changes.any {
                                                it.pressed
                                            }
                                        )
                                    }
                                }
                                .pointerInput(page) {
                                    detectTapGestures(
                                        onTap = {
                                            showOverlay = !showOverlay
                                        },
                                        onDoubleTap = {
                                            scale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                        }
                                    )
                                }
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale

                                    translationX = offsetX
                                    translationY = offsetY
                                },
                                colorFilter = ColorFilter.tint(
                                color = Color(0xFFFFF4E0),
                                blendMode = BlendMode.Multiply
                            ),
                            contentScale = ContentScale.Fit
                        )
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offsetX
                                        translationY = offsetY
                                    }
                            ) {
                                clipRect {
                                    // Very broad tonal variation.
// The circles deliberately overlap and extend far beyond their centers,
// so they should read as uneven paper rather than visible spots.
                                    paperVariation.forEach { variation ->

                                        val center = Offset(
                                            x = variation.first * size.width,
                                            y = variation.second * size.height
                                        )

                                        val radius =
                                            size.minDimension *
                                                    (0.20f + variation.third * 0.25f)

                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.15f),
                                                    Color.Black.copy(alpha = 0.07f),
                                                    Color.Transparent
                                                ),
                                                center = center,
                                                radius = radius
                                            ),
                                            radius = radius,
                                            center = center
                                        )
                                    }

                                    paperGrain.forEach { grain ->

                                        val x = grain.first * size.width
                                        val y = grain.second * size.height

                                        val opacity =
                                            0.02f + grain.third * 0.03f

                                        drawCircle(
                                            color = Color.Black.copy(
                                                alpha = opacity
                                            ),
                                            radius = 1.5f,
                                            center = Offset(x, y)
                                        )
                                    }
                                }
                            }
                            }
                    }

                    actualPageIndex == pages.size -> {

                        EndOfChapterScreen(
                            onNextChapter =
                                if (hasNextChapter) {
                                    onNextChapter
                                } else {
                                    null
                                },
                            onChapterList = onChapterList,
                            onLibrary = onLibrary
                        )

                    }

                    else -> {

                        Box(
                            modifier = Modifier.fillMaxSize()
                        )

                    }

                }

            }

            val currentActualPageIndex =
                pagerState.currentPage - previousPageOffset

            if (
                showOverlay &&
                currentActualPageIndex in pages.indices
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Return to the chapter list.
                    IconButton(
                        onClick = onChapterList
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                            contentDescription = "Chapter list"
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // In our right-to-left reader, left means next chapter.
                        if (hasNextChapter) {
                            IconButton(
                                onClick = onNextChapter
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Next chapter"
                                )
                            }
                        }

                        // Right means previous chapter.
                        if (hasPreviousChapter) {
                            IconButton(
                                onClick = onPreviousChapterFromStart
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Previous chapter"
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        )
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Chapter $chapter",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "${currentActualPageIndex + 1} / ${pages.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )

                    }

                    Slider(
                        interactionSource = sliderInteractionSource,
                        thumb = {
                            Surface(
                                modifier = Modifier.size(23.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                border = BorderStroke(
                                    3.dp,
                                    MaterialTheme.colorScheme.onBackground
                                )
                            ) {}
                        },
                        value = currentActualPageIndex.toFloat(),
                        onValueChange = { value ->

                            coroutineScope.launch {

                                pagerState.scrollToPage(
                                    value.toInt() + previousPageOffset
                                )

                            }

                        },
                        valueRange =
                            0f..(pages.size - 1).toFloat(),
                        steps =
                            (pages.size - 2).coerceAtLeast(0),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outline,
                            activeTickColor = MaterialTheme.colorScheme.onBackground
                        )
                    )

                }

            }

        }

    }

}

