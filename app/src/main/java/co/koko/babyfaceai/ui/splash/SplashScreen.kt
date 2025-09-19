package co.koko.babyfaceai.ui.splash

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import co.koko.babyfaceai.ui.MainViewModel
import co.koko.babyfaceai.util.AdManagerCompose
import co.koko.babyfaceai.util.findActivity
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable// In SplashScreen.kt
fun SplashScreen(navController: NavController, viewModel: MainViewModel) {
    var startAnimation by remember { mutableStateOf(false) }
    // [수정] Activity 컨텍스트를 가져옵니다.
    val activity = LocalContext.current.findActivity()

    // 화면이 처음 나타날 때 애니메이션을 시작합니다.
    LaunchedEffect(key1 = true) {
        startAnimation = true
    }

    // [수정] 모든 로직을 하나의 LaunchedEffect로 통합하여 관리합니다.
    LaunchedEffect(Unit) {
        // 1. 사용자 데이터를 미리 비동기적으로 불러옵니다.
        val userProfileDeferred = async { viewModel.userProfile.first { it != null } }

        // 2. 스플래시 화면 최소 노출 시간을 위해 2초간 대기합니다.
        delay(2000)

        // 3. 사용자 데이터 로드가 완료될 때까지 기다립니다.
        val userProfile = userProfileDeferred.await()

        // 4. 사용자 데이터 기반으로 최종 목적지를 결정합니다.
        val destination = if (userProfile?.nickname.isNullOrEmpty()) {
            "profile_setup"
        } else {
            "main"
        }

//        navController.navigate(destination) {
//            popUpTo("splash") { inclusive = true }
//        }

        // 5. 광고 초기화를 기다린 후, 전면 광고를 표시합니다.
        AdManagerCompose.runAfterInit {
            // activity가 null이 아닐 때만 광고를 호출합니다.
            activity?.let {
                AdManagerCompose.showInterstitial(it) {
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            } ?: run {
                // Activity를 찾지 못한 경우 (예: 프리뷰), 광고 없이 바로 화면 전환
                navController.navigate(destination) {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }

    // 스플래시 화면의 UI (수정 없음)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6A5AE0)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = startAnimation,
            enter = fadeIn(animationSpec = tween(1000))
        ) {
            Text("AI 동안 측정기", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
    }
}