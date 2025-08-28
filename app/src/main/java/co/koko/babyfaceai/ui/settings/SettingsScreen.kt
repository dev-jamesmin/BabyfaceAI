package co.koko.babyfaceai.ui.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import co.koko.babyfaceai.data.UserProfile
import co.koko.babyfaceai.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: MainViewModel) {
    val userProfile by viewModel.userProfile.collectAsState(initial = UserProfile(null, null))
    var notificationEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF7A69E4),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F2F5))
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // 프로필 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8EAF6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color(0xFF7986CB), modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(userProfile?.nickname ?: "사용자", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${userProfile?.age ?: "0"}세", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 설정 메뉴 리스트
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    SettingsMenuItem(icon = Icons.Default.Edit, text = "닉네임 수정", onClick = { /* TODO */ })
                    SettingsMenuItem(icon = Icons.Default.CalendarToday, text = "실제 나이 수정", onClick = { /* TODO */ })
                    SettingsMenuSwitchItem(
                        icon = Icons.Default.Notifications,
                        text = "알림 설정",
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it }
                    )
                    SettingsMenuItem(icon = Icons.Default.Share, text = "앱 공유하기", onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "AI가 내 얼굴 나이를 분석해줘요! AI 동안 측정기 앱을 사용해보세요: [스토어 링크]")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    })
                    SettingsMenuItem(icon = Icons.Default.Star, text = "리뷰 작성하기", onClick = { /* TODO: 스토어로 이동 */ })
                    SettingsMenuItem(icon = Icons.Default.Group, text = "패밀리 앱 보기", onClick = { /* TODO */ })
                    SettingsMenuItem(icon = Icons.Default.Description, text = "이용약관 보기", onClick = { /* TODO */ }, hasDivider = false)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 하단 정보
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("AI동안측정기 v2.0.0", color = Color.Gray, fontSize = 12.sp)
                Text("AI가 알려주는 내 나이", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SettingsMenuItem(icon: ImageVector, text: String, onClick: () -> Unit, hasDivider: Boolean = true) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = text, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
    if (hasDivider) {
        HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = Color(0xFFF0F2F5))
    }
}

@Composable
private fun SettingsMenuSwitchItem(icon: ImageVector, text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = text, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF7A69E4)
                )
            )
        }
    }
}
