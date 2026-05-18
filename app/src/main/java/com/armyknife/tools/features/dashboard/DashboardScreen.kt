package com.armyknife.tools.features.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.armyknife.tools.data.registry.ToolCategory
import com.armyknife.tools.data.registry.ToolRegistry

private val Categories = listOf(
    ToolCategory.SENSOR,
    ToolCategory.HARDWARE,
    ToolCategory.MEDIA,
    ToolCategory.UTILITY
)

@Composable
fun DashboardScreen(
    onToolClick: (String) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Categories.forEach { category ->
            stickyHeader(key = "header-${category.id}") {
                Surface(tonalElevation = 0.dp) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                }
            }
            val tools = ToolRegistry.findByCategory(category)
            if (tools.isNotEmpty()) {
                items(tools) { tool ->
                    ToolCard(
                        title = tool.title,
                        description = tool.description,
                        onClick = { onToolClick(tool.route) }
                    )
                }
            } else {
                item {
                    Text(
                        text = "No tools yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
