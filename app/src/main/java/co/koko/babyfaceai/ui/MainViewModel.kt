package co.koko.babyfaceai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import co.koko.babyfaceai.data.UserProfileRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: UserProfileRepository) : ViewModel() {
    // Repository로부터 사용자 프로필 데이터의 변경을 실시간으로 관찰합니다.
    val userProfile = repository.userProfileFlow

    // UI로부터 요청을 받아 프로필을 저장합니다.
    fun saveProfile(nickname: String, age: String) {
        viewModelScope.launch {
            repository.saveUserProfile(nickname, age)
        }
    }
}

// ViewModel에 Repository를 주입하기 위한 간단한 팩토리 클래스입니다.
class MainViewModelFactory(private val repository: UserProfileRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
