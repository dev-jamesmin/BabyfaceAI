package co.koko.babyfaceai

import android.app.AlertDialog
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
import co.koko.babyfaceai.util.AdManagerCompose
import kotlin.apply

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ---▼ [핵심 수정] ▼---
        // 광고 SDK를 초기화하기 전에 명시적 공개 및 동의 절차를 진행합니다.
        showProminentDisclosureDialog()
        // ---▲ [핵심 수정] ▲---

//        AdManagerCompose.loadAdsOnAppStart(this)
        setContent {
            BabyfaceAITheme {
                AppNavigation()
            }
        }
    }

    private fun showProminentDisclosureDialog() {
        // SharedPreferences 등을 사용하여 사용자가 이미 동의했는지 확인
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val hasAgreed = prefs.getBoolean("has_agreed_to_data_collection", false)

        if (!hasAgreed) {
            AlertDialog.Builder(this)
                .setTitle("앱 데이터 수집 안내")
                .setMessage("더 나은 맞춤형 광고 경험을 제공하기 위해, 기기에 설치된 앱 목록 정보를 수집합니다. 이 정보는 광고 제공 목적으로만 사용됩니다.")
                .setPositiveButton("동의하고 계속하기") { dialog, _ ->
                    // 사용자가 동의하면 SharedPreferences에 기록
                    prefs.edit().putBoolean("has_agreed_to_data_collection", true).apply()
                    // 동의 후에 광고 SDK 초기화
                    AdManagerCompose.loadAdsOnAppStart(this)
                    dialog.dismiss()
                }
                .setNegativeButton("거부") { dialog, _ ->
                    // 거부 시 앱을 종료하거나, 광고 없는 모드로 전환
                    finish() // 앱 종료 예시
                }
                .setCancelable(false) // 뒤로가기 버튼으로 닫기 방지
                .show()
        } else {
            // 이미 동의한 사용자이므로 바로 광고 SDK 초기화
            AdManagerCompose.loadAdsOnAppStart(this)
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
