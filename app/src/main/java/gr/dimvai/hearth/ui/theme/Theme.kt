package gr.dimvai.hearth.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Hearth Dark Color Scheme
 * We use only Dark Mode as per GEMINI.md instructions.
 */
private val HearthColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = SoftWhite,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = SoftWhite,
    secondary = Accent,
    onSecondary = SoftWhite,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Surface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    error = Danger,
    onError = OnDanger
)

@Composable
fun HearthTheme(
    content: @Composable () -> Unit
) {
    // We strictly use the defined HearthColorScheme for a consistent dark theme
    MaterialTheme(
        colorScheme = HearthColorScheme,
        typography = Typography,
        content = content
    )
}
