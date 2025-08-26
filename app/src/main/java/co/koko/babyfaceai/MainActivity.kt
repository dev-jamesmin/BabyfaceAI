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
import co.koko.babyfaceai.data.UserProfileRepository
import co.koko.babyfaceai.data.dataStore
import co.koko.babyfaceai.ui.MainViewModel
import co.koko.babyfaceai.ui.MainViewModelFactory
import co.koko.babyfaceai.ui.main.MainScreen
import co.koko.babyfaceai.ui.profile.ProfileSetupScreen
import co.koko.babyfaceai.ui.splash.SplashScreen
import co.koko.babyfaceai.ui.theme.BabyfaceAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 앱의 전체적인 테마를 적용합니다.
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

    // ViewModelFactory를 사용하여 ViewModel 인스턴스를 생성합니다.
    // 이 ViewModel은 앱의 모든 화면에서 공유되어 사용자 데이터를 관리합니다.
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(UserProfileRepository(context.dataStore))
    )

    // NavHost가 화면 전환을 관리합니다. 시작 화면은 "splash" 입니다.
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController = navController, viewModel = viewModel)
        }
        composable("profile_setup") {
            ProfileSetupScreen(navController = navController, viewModel = viewModel)
        }
        composable("main") {
            MainScreen(viewModel = viewModel)
        }
    }
}
