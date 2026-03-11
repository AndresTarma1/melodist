package com.example.melodist.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    )

    val transition = rememberInfiniteTransition()
    val translateAnimation by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation, translateAnimation),
        end = Offset(translateAnimation + 500f, translateAnimation + 500f)
    )
}

@Composable
fun ChipRowSkeleton() {
    val scrollState = rememberLazyListState()
    val brush = shimmerBrush()

    HorizontalScrollableRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        state = scrollState,
    ) {
        items(10) {
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(brush)
            )
        }
    }
}

@Preview
@Composable
fun SectionSkeleton() {
    val brush = shimmerBrush()
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                .width(180.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )

        val scrollState = rememberLazyListState()
        HorizontalScrollableRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            state = scrollState,
        ) {
            items(10) {
                Column(
                    modifier = Modifier.width(160.dp).padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(brush)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.fillMaxWidth(0.8f).height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(brush)
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth(0.5f).height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(brush)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SongSkeleton() {
    val brush = shimmerBrush()
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(52.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier.width(180.dp).height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.width(100.dp).height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }

        Box(
            modifier = Modifier.size(24.dp)
                .clip(CircleShape)
                .background(brush)
        )
    }
}

@Preview
@Composable
fun Previews() {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.horizontalGradient(listOf(Color(0xFF1E2A38), Color.Black))
        )
    )
}

