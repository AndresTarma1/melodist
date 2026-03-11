package com.example.melodist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun AlbumScreenSkeletonContent() {
    val brush = shimmerBrush()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 800.dp

        if (isCompact) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InfoPanelSkeleton(brush, coverSize = 200.dp)
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.08f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(8.dp))
                SongListSkeleton(brush, count = 6)
            }
        } else {
            Row(modifier = Modifier.fillMaxSize().padding(start = 48.dp, end = 24.dp)) {
                Column(
                    modifier = Modifier.width(320.dp).padding(top = 24.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { InfoPanelSkeleton(brush, coverSize = 240.dp) }
                Spacer(Modifier.width(32.dp))
                Column(modifier = Modifier.weight(1f).padding(top = 24.dp, end = 12.dp)) {
                    SongListSkeleton(brush, count = 8)
                }
            }
        }
    }
}

@Composable
internal fun PlaylistScreenSkeletonContent() {
    val brush = shimmerBrush()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 800.dp

        if (isCompact) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InfoPanelSkeleton(brush, coverSize = 200.dp)
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.08f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(Modifier.height(8.dp))
                SongListSkeleton(brush, count = 6)
            }
        } else {
            Row(modifier = Modifier.fillMaxSize().padding(start = 48.dp, end = 24.dp)) {
                Column(
                    modifier = Modifier.width(320.dp).padding(top = 24.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { InfoPanelSkeleton(brush, coverSize = 240.dp) }
                Spacer(Modifier.width(32.dp))
                Column(modifier = Modifier.weight(1f).padding(top = 24.dp, end = 12.dp)) {
                    SongListSkeleton(brush, count = 8)
                }
            }
        }
    }
}

@Composable
internal fun ArtistScreenSkeletonContent() {
    val brush = shimmerBrush()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 800.dp

        if (isCompact) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.size(160.dp).clip(CircleShape).background(brush))
                Spacer(Modifier.height(20.dp))
                Box(Modifier.size(180.dp, 28.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                Spacer(Modifier.height(8.dp))
                Box(Modifier.size(120.dp, 14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(44.dp).clip(CircleShape).background(brush))
                    Box(Modifier.size(56.dp).clip(CircleShape).background(brush))
                    Box(Modifier.size(44.dp).clip(CircleShape).background(brush))
                }
                Spacer(Modifier.height(32.dp))
                repeat(2) {
                    ArtistSectionSkeleton(brush)
                    Spacer(Modifier.height(24.dp))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 48.dp, vertical = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(180.dp).clip(CircleShape).background(brush))
                    Spacer(Modifier.width(32.dp))
                    Column {
                        Box(Modifier.size(220.dp, 32.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                        Spacer(Modifier.height(10.dp))
                        Box(Modifier.size(140.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                        Spacer(Modifier.height(6.dp))
                        Box(Modifier.size(100.dp, 14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                        Spacer(Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(44.dp).clip(CircleShape).background(brush))
                            Box(Modifier.size(56.dp).clip(CircleShape).background(brush))
                            Box(Modifier.size(44.dp).clip(CircleShape).background(brush))
                        }
                    }
                }
                Spacer(Modifier.height(40.dp))
                repeat(3) {
                    ArtistSectionSkeleton(brush)
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
internal fun SkeletonSongRow(brush: Brush) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(24.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(brush))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Box(Modifier.fillMaxWidth(0.5f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth(0.25f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(brush))
        }
        Spacer(Modifier.width(12.dp))
        Box(Modifier.size(36.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(brush))
    }
}

@Composable
internal fun InfoPanelSkeleton(brush: Brush, coverSize: Dp) {
    Box(Modifier.size(120.dp, 28.dp).clip(RoundedCornerShape(14.dp)).background(brush))
    Spacer(Modifier.height(20.dp))
    Box(Modifier.size(coverSize).clip(RoundedCornerShape(8.dp)).background(brush))
    Spacer(Modifier.height(24.dp))
    Box(Modifier.size(180.dp, 28.dp).clip(RoundedCornerShape(4.dp)).background(brush))
    Spacer(Modifier.height(8.dp))
    Box(Modifier.size(130.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(brush))
    Spacer(Modifier.height(4.dp))
    Box(Modifier.size(150.dp, 14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
    Spacer(Modifier.height(24.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(brush))
        Box(Modifier.size(56.dp).clip(CircleShape).background(brush))
        Box(Modifier.size(44.dp).clip(CircleShape).background(brush))
    }
}

@Composable
internal fun SongListSkeleton(brush: Brush, count: Int) {
    repeat(count) { i ->
        SkeletonSongRow(brush)
        if (i < count - 1) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.08f),
                modifier = Modifier.padding(start = 48.dp)
            )
        }
    }
}

@Composable
internal fun ArtistSectionSkeleton(brush: Brush) {
    Box(
        Modifier.fillMaxWidth(0.3f).height(22.dp)
            .clip(RoundedCornerShape(4.dp)).background(brush)
    )
    Spacer(Modifier.height(12.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(5) {
            Column(modifier = Modifier.width(150.dp)) {
                Box(Modifier.size(150.dp).clip(RoundedCornerShape(8.dp)).background(brush))
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(0.8f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth(0.5f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            }
        }
    }
}
