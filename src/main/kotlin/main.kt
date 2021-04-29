import androidx.compose.desktop.AppManager
import androidx.compose.desktop.Window
import data.SRCRepository
import data.local.SettingsRepository
import data.remote.SRCService
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ui.screens.home.HomeScreen
import ui.screens.home.SettingsComponent
import ui.theme.SRCClientTheme
import java.awt.Dimension

fun main() = Window {
    AppManager.windows[0].window.minimumSize = Dimension(500, 500)

    startKoin {
        modules(module {
            single { SettingsRepository() }
            single { SRCService.create() }
            single { SRCRepository(get()) }
        })
    }

    SRCClientTheme {
        HomeScreen()
    }
}