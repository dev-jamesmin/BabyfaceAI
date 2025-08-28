plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "co.koko.babyfaceai"
    compileSdk = 36

    defaultConfig {
        applicationId = "co.koko.babyfaceai"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // 1. 화면 전환(Navigation)을 위해 필요합니다.
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // 2. ViewModel을 Compose에서 사용하기 위해 필요합니다.
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

    // 3. DataStore(데이터 저장)를 위해 필요합니다.
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("androidx.compose.material:material-icons-extended-android:1.6.8")

    // TensorFlow Lite 핵심 라이브러리
    implementation("org.tensorflow:tensorflow-lite:2.15.0")

    // 이미지 전처리, 카메라 연동 등을 도와주는 Support Library
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // (선택사항) GPU 가속을 사용하고 싶을 때 추가
    implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:0.4.4")
}