package co.koko.babyfaceai.ui.settings

import android.R.attr.versionName
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import co.koko.babyfaceai.util.AdManagerCompose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: MainViewModel) {
    val userProfile by viewModel.userProfile.collectAsState(initial = UserProfile(null, null))
    val context = LocalContext.current

    // ---▼ [추가] 실제 앱 버전을 가져오는 로직 ▼---
    val versionName = try {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        packageInfo.versionName
    } catch (e: Exception) {
        "N/A"
    }
    // ---▲ [추가] ▲---

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
        },
        // ---▼ [핵심 수정] ▼---
        // contentWindowInsets를 사용하여 Scaffold가 시스템 UI 영역을 어떻게 처리할지 명시합니다.
        // 우리는 bottomBar와 content에서 직접 인셋을 처리할 것이므로, 기본 인셋을 비웁니다.
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                    // [수정] '프로필 수정' 버튼 클릭 시 "edit_profile" 화면으로 이동하도록 연결
                    SettingsMenuItem(
                        icon = Icons.Default.Edit,
                        text = "프로필 수정",
                        onClick = { navController.navigate("edit_profile") }
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
                    SettingsMenuItem(icon = Icons.Default.Star, text = "리뷰 작성하기", onClick = {
                        val packageName = context.packageName
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                            context.startActivity(intent)
                        }
                    })
                    SettingsMenuItem(icon = Icons.Default.Group, text = "패밀리 앱 보기", onClick = {
                        val url = "https://play.google.com/store/apps/developer?id=KOKO+COMPANY"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    })
                    SettingsMenuItem(icon = Icons.Default.Description, text = "이용약관 보기", onClick = {
                        val url = "https://www.notion.so/AI-24d931b917cf80bca715dfd6f2f6a142?source=copy_link"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }, hasDivider = false)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 하단 정보
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("AI동안측정기 v$versionName", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 배너 광고
            AdManagerCompose.BannerAdView(modifier = Modifier.fillMaxWidth())

            // ---▼ [핵심 수정] ▼---
            // 시스템 내비게이션 바(소프트 키, 제스처 바)를 위한 공간을 확보합니다.
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding())
            // ---▲ [핵심 수정] ▲---
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
