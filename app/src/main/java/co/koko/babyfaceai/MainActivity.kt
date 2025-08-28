package co.koko.babyfaceai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.koko.babyfaceai.data.AgeClassifier // AgeClassifier 임포트
import co.koko.babyfaceai.data.UserProfileRepository
import co.koko.babyfaceai.data.dataStore
import co.koko.babyfaceai.ui.MainViewModel
import co.koko.babyfaceai.ui.MainViewModelFactory
import co.koko.babyfaceai.ui.main.MainScreen
import co.koko.babyfaceai.ui.profile.ProfileSetupScreen
import co.koko.babyfaceai.ui.settings.SettingsScreen
import co.koko.babyfaceai.ui.splash.SplashScreen
import co.koko.babyfaceai.ui.theme.BabyfaceAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BabyfaceAITheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // [수정] ViewModel을 생성할 때 AgeClassifier를 함께 전달합니다.
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            repository = UserProfileRepository(context.dataStore),
            classifier = AgeClassifier(context) // <-- 이 부분을 추가하세요.
        )
    )

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController = navController, viewModel = viewModel)
        }
        composable("profile_setup") {
            ProfileSetupScreen(navController = navController, viewModel = viewModel)
        }
        composable("main") {
            // [수정] MainScreen이 설정 화면으로 이동할 수 있도록 navController를 전달합니다.
            MainScreen(navController = navController, viewModel = viewModel)
        }
        // [추가] "settings"라는 경로와 SettingsScreen을 연결합니다.
        composable("settings") {
            SettingsScreen(navController = navController, viewModel = viewModel)
        }
    }
}
