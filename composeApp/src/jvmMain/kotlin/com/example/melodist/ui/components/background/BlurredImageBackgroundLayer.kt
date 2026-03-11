package com.example.melodist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * Fondo de pantalla difuminado usando la imagen provista.
 */
@Composable
fun BlurredImageBackground(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    darkOverlayAlpha: Float = 0.50f,
    gradientFraction: Float = 0.55f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        if (!imageUrl.isNullOrBlank()) {
            val bgUrl = upscaleThumbnailUrl(imageUrl, 480)
            AsyncImage(
                model = bgUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        renderEffect = BlurEffect(
                            radiusX = 55f,
                            radiusY = 55f,
                            edgeTreatment = TileMode.Clamp
                        )
                        scaleX = 1.18f
                        scaleY = 1.18f
                    }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = darkOverlayAlpha))
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.00f to Color.Black.copy(alpha = 0.10f),
                            gradientFraction to Color.Black.copy(alpha = 0.35f),
                            1.00f to Color.Black.copy(alpha = 0.82f)
                        )
                    )
            )
        }

        content()
    }
}

