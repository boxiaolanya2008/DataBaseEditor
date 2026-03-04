package cn.database.editor.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class StatusType {
    SUCCESS, WARNING, ERROR, INFO, NEUTRAL
}

@Composable
fun StatusIndicator(
    text: String,
    type: StatusType = StatusType.NEUTRAL,
    modifier: Modifier = Modifier
) {
    val color = when (type) {
        StatusType.SUCCESS -> MaterialTheme.colorScheme.primary
        StatusType.WARNING -> MaterialTheme.colorScheme.tertiary
        StatusType.ERROR -> MaterialTheme.colorScheme.error
        StatusType.INFO -> MaterialTheme.colorScheme.secondary
        StatusType.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = color,
            modifier = Modifier.size(8.dp)
        ) {}
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ConnectionStatusIndicator(
    isConnected: Boolean,
    databaseName: String,
    modifier: Modifier = Modifier
) {
    StatusIndicator(
        text = if (isConnected) "已连接: $databaseName" else "未连接",
        type = if (isConnected) StatusType.SUCCESS else StatusType.NEUTRAL,
        modifier = modifier
    )
}