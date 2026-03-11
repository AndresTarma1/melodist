package com.example.melodist.ui.components

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun HorizontalScrollableRow(
    modifier: Modifier,
    state: LazyListState,
    contentPadding: PaddingValues,
    horizontalArrangement: Arrangement.Horizontal,
    content: LazyListScope.() -> Unit
) {
    Box(modifier = modifier) {
        Column {
            LazyRow(
                state = state,
                contentPadding = contentPadding,
                horizontalArrangement = horizontalArrangement,
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
            // Add a fixed spacer to prevent layout jumps
            Spacer(modifier = Modifier.height(14.dp))
        }

        HorizontalScrollbar(
            adapter = rememberScrollbarAdapter(state),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.95f)
                .height(6.dp),
            style = androidx.compose.foundation.ScrollbarStyle(
                minimalHeight = 16.dp,
                thickness = 6.dp,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(3.dp),
                hoverDurationMillis = 300,
                unhoverColor = Color.White.copy(alpha = 0.05f),
                hoverColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}