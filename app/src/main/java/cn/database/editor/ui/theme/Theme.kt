package cn.database.editor.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DatabaseBlue80,
    secondary = DatabaseTeal80,
    tertiary = DatabaseGray80,
    surface = Color(0xFF121212),
    surfaceContainer = Color(0xFF1E1E1E),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFB8C2CC)
)

private val LightColorScheme = lightColorScheme(
    primary = DatabaseBlue40,
    secondary = DatabaseTeal40,
    tertiary = DatabaseGray40,
    surface = Color(0xFFFFFFFF),
    surfaceContainer = SurfaceContainer,
    onSurface = Color(0xFF000000),
    onSurfaceVariant = OnSurfaceVariant
)

@Composable
fun 数据库编辑器Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
