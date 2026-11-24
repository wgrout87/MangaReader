package com.wgrou.mangareader.reader

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntSize
import com.wgrou.mangareader.R
import kotlin.random.Random

fun paperWarmthFilter(warmth: Float): ColorFilter {
    return ColorFilter.tint(
        color = Color(
            red = 1f,
            green = 1f - (11f / 255f) * warmth,
            blue = 1f - (31f / 255f) * warmth
        ),
        blendMode = BlendMode.Multiply
    )
}

@Composable
fun PaperTextureOverlay(
    textureStrength: Float,
    grainSeed: Int,
    modifier: Modifier = Modifier
) {
    if (textureStrength <= 0f) return

    val paperTexture = ImageBitmap.imageResource(
        R.drawable.paper_texture
    )

    val paperGrain = remember(grainSeed) {
        val random = Random(grainSeed)

        List(5000) {
            Triple(
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat()
            )
        }
    }

    Canvas(
        modifier = modifier.graphicsLayer {
            compositingStrategy =
                CompositingStrategy.ModulateAlpha
        }
    ) {
        clipRect {
            drawImage(
                image = paperTexture,
                dstSize = IntSize(
                    size.width.toInt(),
                    size.height.toInt()
                ),
                alpha = textureStrength,
                blendMode = BlendMode.Multiply
            )

            paperGrain.forEach { grain ->
                val x = grain.first * size.width
                val y = grain.second * size.height

                val opacity =
                    (0.02f + grain.third * 0.03f) *
                            textureStrength

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