import androidx.compose.desktop.AppManager
import androidx.compose.desktop.Window
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import data.SRCRepository
import data.local.SettingsDAO
import data.remote.SRCService
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ui.screens.Screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import data.local.DatabaseSingleton
import data.local.GamesDAO
import ui.screens.home.HomeScreen
import ui.screens.splash.SplashScreen
import ui.theme.SRCClientTheme
import java.awt.Dimension

fun main() = Window(
    size = IntSize(500, 800)
) {
    AppManager.windows[0].window.minimumSize = Dimension(500, 800)

    startKoin {
        modules(module {
            single { DatabaseSingleton() }
            single { SettingsDAO(get()) }
            single { GamesDAO(get()) }
            single { SRCService.create() }
            single { SRCRepository(get(), get()) }
        })
    }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

    SRCClientTheme {
        when (currentScreen) {
            is Screen.Splash -> {
                SplashScreen { currentScreen = Screen.Home }
            }
            is Screen.Home -> {
                HomeScreen()
            }
        }
    }
}