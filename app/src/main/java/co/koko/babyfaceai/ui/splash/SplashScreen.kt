package co.koko.babyfaceai.ui.splash

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import co.koko.babyfaceai.ui.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, viewModel: MainViewModel) {
    var startAnimation by remember { mutableStateOf(false) }
    // ViewModel로부터 사용자 프로필 데이터를 관찰합니다. 초기값은 null입니다.
    val userProfile by viewModel.userProfile.collectAsState(initial = null)

    // 화면이 처음 나타날 때 애니메이션을 시작하고 2초간 대기합니다.
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
    }

    // userProfile 데이터가 로드되면(null이 아니게 되면) 다음 화면으로 이동할지 결정합니다.
    LaunchedEffect(userProfile) {
        if (userProfile != null) {
            val destination = if (userProfile?.nickname.isNullOrEmpty()) {
                // 저장된 닉네임이 없으면 프로필 설정 화면으로 이동합니다.
                "profile_setup"
            } else {
                // 저장된 닉네임이 있으면 메인 화면으로 이동합니다.
                "main"
            }
            // 현재 스플래시 화면을 백스택에서 제거하고 새 화면으로 이동합니다.
            navController.navigate(destination) {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    // 스플래시 화면의 UI
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
