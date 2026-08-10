package com.yuuparrow.folderopt.ui.storage.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class BreadcrumbSegment(val label: String, val path: String)

@Composable
fun Breadcrumb(
    segments: List<BreadcrumbSegment>,
    onSegmentClick: (BreadcrumbSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState())
    ) {
        segments.forEachIndexed { index, segment ->
            val isLast = index == segments.lastIndex
            Text(
                text = segment.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                color = if (isLast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                modifier = if (!isLast) {
                    Modifier
                        .padding(end = 4.dp)
                        .clickable { onSegmentClick(segment) }
                } else {
                    Modifier.padding(end = 4.dp)
                }
            )
            if (!isLast) {
                Text(
                    text = "›",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}
