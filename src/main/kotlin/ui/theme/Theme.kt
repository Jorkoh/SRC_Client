package ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(

)

private val LightColorPalette = lightColors(
    primary = Color.Black
)

@Composable
fun SRCClientTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colors = if (darkTheme) DarkColorPalette else LightColorPalette,
        typography = typography,
        shapes = shapes,
        content = content
    )
}

val Colors.offWhite: Color
    get() = CustomColors.offWhite

val Colors.approveGreen: Color
    get() = CustomColors.approveGreen

val Colors.rejectRed: Color
    get() = CustomColors.rejectRed

val Colors.pendingBlue: Color
    get() = CustomColors.pendingBlue

val Colors.linkBlue: Color
    get() = CustomColors.linkBlue