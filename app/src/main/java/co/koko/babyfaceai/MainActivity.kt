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
import co.koko.babyfaceai.data.AgeClassifier
import co.koko.babyfaceai.data.UserProfileRepository
import co.koko.babyfaceai.data.dataStore
import co.koko.babyfaceai.ui.MainViewModel
import co.koko.babyfaceai.ui.MainViewModelFactory
import co.koko.babyfaceai.ui.main.MainScreen
import co.koko.babyfaceai.ui.profile.EditProfileScreen
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

    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            repository = UserProfileRepository(context.dataStore),
            classifier = AgeClassifier(context)
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
            MainScreen(navController = navController, viewModel = viewModel)
        }
        composable("settings") {
            SettingsScreen(navController = navController, viewModel = viewModel)
        }
        // [추가] 프로필 수정 화면 경로 추가
        composable("edit_profile") {
            EditProfileScreen(navController = navController, viewModel = viewModel)
        }
    }
}
