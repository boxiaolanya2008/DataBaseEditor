package cn.database.editor.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ScreenSize {
    SMALL, MEDIUM, LARGE, XLARGE
}

@Composable
fun getScreenSize(): ScreenSize {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    return when {
        screenWidth < 600 -> ScreenSize.SMALL
        screenWidth < 840 -> ScreenSize.MEDIUM
        screenWidth < 1200 -> ScreenSize.LARGE
        else -> ScreenSize.XLARGE
    }
}

@Composable
fun getResponsivePadding(): PaddingValues {
    val screenSize = getScreenSize()
    return when (screenSize) {
        ScreenSize.SMALL -> PaddingValues(16.dp)
        ScreenSize.MEDIUM -> PaddingValues(24.dp)
        ScreenSize.LARGE -> PaddingValues(32.dp)
        ScreenSize.XLARGE -> PaddingValues(48.dp)
    }
}

@Composable
fun getGridColumnCount(): Int {
    val screenSize = getScreenSize()
    return when (screenSize) {
        ScreenSize.SMALL -> 1
        ScreenSize.MEDIUM -> 2
        ScreenSize.LARGE -> 3
        ScreenSize.XLARGE -> 4
    }
}

@Composable
fun ResponsiveGrid(
    items: List<Any>,
    modifier: Modifier = Modifier,
    content: @Composable (item: Any) -> Unit
) {
    val columnCount = getGridColumnCount()
    
    if (columnCount > 1) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            modifier = modifier.fillMaxSize(),
            contentPadding = getResponsivePadding(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                content(item)
            }
        }
    } else {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = getResponsivePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                content(item)
            }
        }
    }
}

@Composable
fun getCardWidth(): Dp {
    val screenSize = getScreenSize()
    return when (screenSize) {
        ScreenSize.SMALL -> 300.dp
        ScreenSize.MEDIUM -> 280.dp
        ScreenSize.LARGE -> 260.dp
        ScreenSize.XLARGE -> 240.dp
    }
}