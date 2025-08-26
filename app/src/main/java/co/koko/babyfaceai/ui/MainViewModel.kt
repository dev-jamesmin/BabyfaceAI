/*
 * ======================================================================
 * 파일 위치: ui/MainViewModel.kt
 * 역할: AgeClassifier를 사용하여 이미지 분석을 요청하고, 결과를 UI에 전달합니다.
 * ======================================================================
 */
package co.koko.babyfaceai.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import co.koko.babyfaceai.data.AgeClassifier
import co.koko.babyfaceai.data.ClassificationResult
import co.koko.babyfaceai.data.UserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: UserProfileRepository,
    private val classifier: AgeClassifier // AgeClassifier를 주입받습니다.
) : ViewModel() {
    val userProfile = repository.userProfileFlow

    // [추가] 분석 결과를 저장할 StateFlow를 만듭니다.
    private val _classificationResult = MutableStateFlow<List<ClassificationResult>>(emptyList())
    val classificationResult = _classificationResult.asStateFlow()

    fun saveProfile(nickname: String, age: String) {
        viewModelScope.launch {
            repository.saveUserProfile(nickname, age)
        }
    }

    // [추가] UI로부터 Bitmap을 받아 이미지 분석을 요청하는 함수입니다.
    fun classifyImage(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) { // CPU 집약적인 작업은 Default Dispatcher에서 실행
            val results = classifier.classify(bitmap)
            _classificationResult.value = results
        }
    }

    // [추가] 결과 창을 닫을 때 호출하여 이전 결과를 초기화합니다.
    fun clearResult() {
        _classificationResult.value = emptyList()
    }
}

// ViewModelFactory를 수정하여 AgeClassifier도 주입할 수 있도록 합니다.
class MainViewModelFactory(
    private val repository: UserProfileRepository,
    private val classifier: AgeClassifier
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, classifier) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// [수정] MainActivity에서 ViewModel을 생성할 때 Classifier도 함께 전달하도록 수정해야 합니다.
// MainActivity.kt의 AppNavigation() 함수
/*
@Composable
fun AppNavigation() {
    // ...
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            UserProfileRepository(context.dataStore),
            AgeClassifier(context) // Classifier 인스턴스 생성
        )
    )
    // ...
}
*/
