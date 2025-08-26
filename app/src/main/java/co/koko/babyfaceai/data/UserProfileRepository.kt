package co.koko.babyfaceai.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

// 앱 전체에서 사용할 DataStore 인스턴스를 정의합니다.
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

// 사용자 프로필 데이터를 나타내는 데이터 클래스입니다.
data class UserProfile(val nickname: String?, val age: String?)

class UserProfileRepository(private val dataStore: DataStore<Preferences>) {
    // DataStore에 데이터를 저장할 때 사용할 키(Key)를 정의합니다.
    private object PreferencesKeys {
        val NICKNAME = stringPreferencesKey("nickname")
        val AGE = stringPreferencesKey("age")
    }

    // DataStore에서 프로필 데이터를 Flow 형태로 읽어옵니다. 데이터가 변경되면 자동으로 UI에 반영됩니다.
    val userProfileFlow = dataStore.data.map { preferences ->
        UserProfile(
            nickname = preferences[PreferencesKeys.NICKNAME],
            age = preferences[PreferencesKeys.AGE]
        )
    }

    // 닉네임과 나이를 DataStore에 저장하는 함수입니다.
    suspend fun saveUserProfile(nickname: String, age: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NICKNAME] = nickname
            preferences[PreferencesKeys.AGE] = age
        }
    }
}