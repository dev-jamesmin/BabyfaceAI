package co.koko.babyfaceai.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.koko.babyfaceai.data.UserProfile
import co.koko.babyfaceai.ui.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    // ViewModel로부터 사용자 프로필 데이터를 관찰합니다.
    val userProfile by viewModel.userProfile.collectAsState(initial = UserProfile(null, null))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "메인 화면",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            userProfile?.let {
                if (it.nickname != null && it.age != null) {
                    Text("안녕하세요, ${it.nickname}님 (${it.age}세)!")
                    Text("이제 사진을 찍어 동안을 측정해보세요.")
                }
            }
        }
    }
}
