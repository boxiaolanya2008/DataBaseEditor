package cn.database.editor.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class DrawerItem(
    val route: String,
    val title: String,
    val icon: @Composable () -> Unit
) {
    object Home : DrawerItem(
        route = Screen.Home.route,
        title = "主页",
        icon = { Icon(Icons.Default.Home, contentDescription = null) }
    )
    object Settings : DrawerItem(
        route = Screen.Settings.route,
        title = "设置",
        icon = { Icon(Icons.Default.Settings, contentDescription = null) }
    )
}

val drawerItems = listOf(
    DrawerItem.Home,
    DrawerItem.Settings
)

@Composable
fun AppDrawerSheet(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                modifier = Modifier.padding(vertical = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "数据库编辑器",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "SQLite 数据库管理工具",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            drawerItems.forEach { item ->
                NavigationDrawerItem(
                    icon = { item.icon() },
                    label = { Text(item.title) },
                    selected = currentRoute == item.route,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        onNavigate(item.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}
