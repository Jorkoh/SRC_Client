import androidx.compose.desktop.AppManager
import androidx.compose.desktop.Window
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import data.SRCRepository
import data.local.DatabaseSingleton
import data.local.FiltersDAO
import data.local.GamesDAO
import data.remote.SRCService
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ui.screens.Screen
import ui.screens.home.HomeScreen
import ui.screens.splash.SplashScreen
import ui.theme.SRCClientTheme
import java.awt.Dimension

fun main() = Window(
    size = IntSize(650, 900)
) {
    AppManager.windows[0].window.minimumSize = Dimension(650, 900)

    // TODO remove this once done testing or move it to build configs somehow
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")

    startKoin {
        modules(module {
            single { DatabaseSingleton() }
            single { FiltersDAO(get()) }
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