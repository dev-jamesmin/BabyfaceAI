package co.koko.babyfaceai.ui.main

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import co.koko.babyfaceai.data.ClassificationResult
import co.koko.babyfaceai.data.UserProfile
import co.koko.babyfaceai.ui.MainViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: MainViewModel) {
    val userProfile by viewModel.userProfile.collectAsState(initial = UserProfile(null, null))
    val classificationResult by viewModel.classificationResult.collectAsState()
    val context = LocalContext.current

    // --- 카메라 및 갤러리 실행을 위한 Launcher 설정 ---
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            viewModel.classifyImage(it)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
            viewModel.classifyImage(bitmap)
        }
    }

    // --- UI 구성 ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "안녕하세요, ${userProfile?.nickname ?: ""}!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // [수정] 설정 버튼 클릭 시 "settings" 화면으로 이동하도록 연결되었습니다.
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF7A69E4), Color(0xFF6A5AE0))
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { cameraLauncher.launch() },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9C87A))
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera Icon", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("카메라로 측정하기", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Image Icon", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("사진 불러오기", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(48.dp))

                FeatureDescription(text = "머신러닝 모델 기반 AI 얼굴 분석해 드려요")
                FeatureDescription(text = "세부 나이 매칭률을 제공해 드려요")
                FeatureDescription(text = "개인정보 보호 걱정마세요. 서버연동 없이 학습된 모델을 디바이스에서 구동하는 방식이에요.")
            }

            Text(
                text = "AI가 알려주는 내 나이",
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
            )
        }
    }

    if (classificationResult.isNotEmpty()) {
        ResultBottomSheet(
            results = classificationResult,
            onDismiss = { viewModel.clearResult() }
        )
    }
}

// [수정] 결과 표시 BottomSheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultBottomSheet(results: List<ClassificationResult>, onDismiss: () -> Unit) {
    val modalBottomSheetState = rememberModalBottomSheetState()

    // 라벨에서 앞의 숫자를 제거하는 헬퍼 함수
    fun formatLabel(label: String): String {
        return label.substringAfter(" ", label) // 공백 뒤의 문자열을 가져오고, 공백이 없으면 원래 문자열 반환
    }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = modalBottomSheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("AI 분석 결과", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            results.firstOrNull()?.let {
                Text(
                    "당신의 얼굴 나이는...",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatLabel(it.label), // [수정] 라벨 포맷팅 적용
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A5AE0)
                )
                Text(
                    "(${(it.confidence * 100).roundToInt()}% 확률)",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("나이대별 매칭률", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // [수정] 확률이 높은 2개의 결과만 표시
            results.take(2).forEach { result ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatLabel(result.label), fontSize = 16.sp) // [수정] 라벨 포맷팅 적용
                    Text("${(result.confidence * 100).roundToInt()}%", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onDismiss() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("확인")
            }
        }
    }
}

@Composable
private fun FeatureDescription(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF9C87A))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp, lineHeight = 20.sp)
    }
}
