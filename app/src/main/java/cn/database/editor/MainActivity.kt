package cn.database.editor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import cn.database.editor.data.repository.SettingsRepository
import cn.database.editor.data.repository.dataStore
import cn.database.editor.ui.navigation.AppNavigation
import cn.database.editor.ui.theme.数据库编辑器Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsRepository = SettingsRepository(applicationContext)
        setContent {
            数据库编辑器Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val hasShownTutorial by settingsRepository.hasShownTutorial.collectAsState(initial = false)
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    
                    AppNavigation(
                        navController = navController,
                        hasShownTutorial = hasShownTutorial,
                        onTutorialComplete = {
                            lifecycleScope.launch {
                                settingsRepository.setTutorialShown(true)
                            }
                        },
                        drawerState = drawerState
                    )
                }
            }
        }
    }
}
