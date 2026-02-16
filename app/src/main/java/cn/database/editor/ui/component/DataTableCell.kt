package cn.database.editor.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun DataTableCell(
    text: String?,
    modifier: Modifier = Modifier,
    isHeader: Boolean = false,
    maxWidth: Int = 150
) {
    Text(
        text = text ?: "NULL",
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .fillMaxWidth(),
        style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
        color = if (isHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        fontFamily = if (!isHeader) FontFamily.Monospace else null,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        softWrap = true
    )
}
