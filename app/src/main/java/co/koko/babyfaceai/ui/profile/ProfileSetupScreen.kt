package co.koko.babyfaceai.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import co.koko.babyfaceai.R // 참고: 이 아이콘 리소스를 res/drawable 폴더에 추가해야 합니다.
import co.koko.babyfaceai.ui.MainViewModel

@Composable
fun ProfileSetupScreen(navController: NavController, viewModel: MainViewModel) {
    var nickname by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    // 닉네임과 나이가 모두 입력되었을 때만 버튼이 활성화되도록 합니다.
    val isButtonEnabled by remember {
        derivedStateOf { nickname.isNotBlank() && age.isNotBlank() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF7A69E4), Color(0xFF6A5AE0))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 앱 아이콘
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                // TODO: res/drawable 폴더에 'ic_app_icon.xml' 같은 아이콘 파일을 추가해주세요.
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 앱 제목
            Text("AI동안측정기", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("AI가 분석하는 당신의 나이", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(48.dp))

            // 프로필 설정 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("프로필 설정", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(24.dp))

                    // 닉네임 입력 필드
                    Text("닉네임 입력", fontSize = 14.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        placeholder = { Text("닉네임을 입력해주세요") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6A5AE0),
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 실제 나이 입력 필드
                    Text("실제 나이 입력", fontSize = 14.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = age,
                        onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
                        placeholder = { Text("나이를 입력해주세요") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6A5AE0),
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    // 시작하기 버튼
                    Button(
                        onClick = {
                            viewModel.saveProfile(nickname, age)
                            navController.navigate("main") {
                                // 이전 화면 스택을 모두 제거하고 메인 화면으로 이동합니다.
                                popUpTo("splash") { inclusive = true }
                            }
                        },
                        enabled = isButtonEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF9C87A),
                            disabledContainerColor = Color(0xFFF9C87A).copy(alpha = 0.5f)
                        )
                    ) {
                        Text("시작하기", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("AI가 알려주는 내 나이", color = Color.White.copy(alpha = 0.7f))
        }
    }
}
