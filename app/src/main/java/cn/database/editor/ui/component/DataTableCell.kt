package cn.database.editor.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun DataTableCell(
    text: String?,
    modifier: Modifier = Modifier,
    isHeader: Boolean = false,
    maxWidth: Int = 150,
    isPrimaryKey: Boolean = false
) {
    Box(
        modifier = modifier
            .requiredWidth(maxWidth.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .clip(MaterialTheme.shapes.small)
            .background(
                if (isHeader) MaterialTheme.colorScheme.primaryContainer 
                else if (isPrimaryKey) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 1.dp,
                color = if (isHeader) MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text ?: "NULL",
            style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
            color = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer 
                else if (isPrimaryKey) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurface,
            fontFamily = if (!isHeader) FontFamily.Monospace else FontFamily.Default,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            softWrap = true,
            textAlign = if (isHeader) TextAlign.Center else TextAlign.Start
        )
    }
}
