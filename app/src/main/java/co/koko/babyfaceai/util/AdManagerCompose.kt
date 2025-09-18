package co.koko.babyfaceai.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.ironsource.mediationsdk.ads.nativead.LevelPlayNativeAd
import com.ironsource.mediationsdk.ads.nativead.LevelPlayNativeAdListener
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo
import com.ironsource.mediationsdk.logger.IronSourceError
import com.unity3d.mediation.*
import com.unity3d.mediation.banner.LevelPlayBannerAdView
import com.unity3d.mediation.banner.LevelPlayBannerAdViewListener
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAd
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAdListener
import com.unity3d.mediation.rewarded.LevelPlayReward
import com.unity3d.mediation.rewarded.LevelPlayRewardedAd
import com.unity3d.mediation.rewarded.LevelPlayRewardedAdListener

@SuppressLint("StaticFieldLeak")
object AdManagerCompose {
    private const val TAG = "AdManagerCompose"
    private const val APP_KEY = "23a37e22d"
    private const val BANNER_AD_UNIT_ID = "trfon44p9x30ilud"
    private const val INTERSTITIAL_AD_UNIT_ID = "1atn2bytibjt9qri"
    private const val REWARDED_AD_UNIT_ID = "jp8iq91kctd4j33o"
    private const val NATIVE_AD_PLACEMENT_NAME = "fzfhq1l145y84ixy"

    // 광고 객체들
    private var rewardedAd: LevelPlayRewardedAd? = null
    private var interstitialAd: LevelPlayInterstitialAd? = null
    private var bannerAd: LevelPlayBannerAdView? = null

    // 상태 관리 변수
    private var interstitialCallback: (() -> Unit)? = null
    private var isInitialized = false
    private val postInitActions = mutableListOf<() -> Unit>()

    private var waitingActivityForInterstitial: Activity? = null

    // --- 초기화 및 공통 로직 (기존과 거의 동일) ---
    fun initialize(application: Application) {
        if (isInitialized) {
            Log.d(TAG, "초기화 이미 완료됨")
            return
        }

        Log.d(TAG, "SDK 초기화 시작")
        val request = LevelPlayInitRequest.Builder(APP_KEY).build()
        LevelPlay.init(application, request, object : LevelPlayInitListener {
            override fun onInitSuccess(config: LevelPlayConfiguration) {
                Log.d(TAG, "SDK 초기화 성공: config=$config")
                isInitialized = true
                synchronized(postInitActions) {
                    postInitActions.forEach { it.invoke() }
                    postInitActions.clear()
                }
            }
            override fun onInitFailed(error: LevelPlayInitError) {
                Log.e(TAG, "LevelPlay 초기화 실패: ${error.errorMessage}")
                Log.e(TAG, "SDK 초기화 실패: ${error.errorMessage}")
            }
        })
    }

    fun runAfterInit(action: () -> Unit) {
        if (isInitialized) action() else synchronized(postInitActions) { postInitActions.add(action) }
    }

    fun loadAdsOnAppStart(activity: Activity) {
        runAfterInit {
            setupInterstitial()
            loadInterstitial()
            setupRewarded()
            loadRewarded()
        }
    }

    // --- Interstitial ---
    private fun setupInterstitial() {
        Log.d(TAG, "전면광고 세팅 시작")
        interstitialAd = LevelPlayInterstitialAd(INTERSTITIAL_AD_UNIT_ID)
        interstitialAd?.setListener(object : LevelPlayInterstitialAdListener {
            override fun onAdLoaded(adInfo: LevelPlayAdInfo) {
                Log.d(TAG, "전면 광고 로드됨")
                waitingActivityForInterstitial?.let { activity ->
                    if (!activity.isFinishing) {
                        Log.d(TAG, "대기 중이던 전면 광고를 표시합니다.")
                        interstitialAd?.showAd(activity)
                    }
                    waitingActivityForInterstitial = null
                }
            }
            override fun onAdLoadFailed(error: LevelPlayAdError) {
                Log.e(TAG, "전면 광고 로드 실패: ${error.getErrorMessage()}")
                interstitialCallback?.invoke()
                interstitialCallback = null
                waitingActivityForInterstitial = null
            }
            override fun onAdClosed(adInfo: LevelPlayAdInfo) {
                Log.d(TAG, "전면 광고 닫힘")
                interstitialCallback?.invoke()
                interstitialCallback = null
                waitingActivityForInterstitial = null
                loadInterstitial()
            }
            override fun onAdDisplayFailed(error: LevelPlayAdError, adInfo: LevelPlayAdInfo) {
                Log.e(TAG, "전면 광고 표시 실패: ${error.getErrorMessage()}")
                interstitialCallback?.invoke()
                interstitialCallback = null
                waitingActivityForInterstitial = null
            }
            override fun onAdClicked(adInfo: LevelPlayAdInfo) {}
            override fun onAdDisplayed(adInfo: LevelPlayAdInfo) {}
        })
    }

    fun loadInterstitial() {
        interstitialAd?.loadAd()
    }

    fun showInterstitial(activity: Activity, onClosed: () -> Unit) {
        Log.d(TAG, "전면 광고 호출 요청")
        runAfterInit {
            interstitialCallback = onClosed
            if (interstitialAd?.isAdReady() == true) {
                Log.d(TAG, "전면 광고가 준비되어 즉시 표시합니다.")
                interstitialAd?.showAd(activity)
            } else {
                Log.d(TAG, "전면 광고가 아직 준비되지 않았습니다. 로드 완료 후 표시됩니다.")
                waitingActivityForInterstitial = activity
            }
        }
    }

    // --- Rewarded ---
    private fun setupRewarded() {
        rewardedAd = LevelPlayRewardedAd(REWARDED_AD_UNIT_ID)
        rewardedAd?.setListener(object : LevelPlayRewardedAdListener {
            override fun onAdLoaded(adInfo: LevelPlayAdInfo) { Log.d(TAG, "리워드 광고 로드됨") }
            override fun onAdRewarded(reward: LevelPlayReward, adInfo: LevelPlayAdInfo) { Log.d(TAG, "리워드 광고 완료: ${reward.amount} ${reward.name}") }
            override fun onAdLoadFailed(error: LevelPlayAdError) { Log.e(TAG, "리워드 광고 로드 실패: ${error.getErrorMessage()}") }
            override fun onAdDisplayed(adInfo: LevelPlayAdInfo) {}
            override fun onAdClicked(adInfo: LevelPlayAdInfo) {}
            override fun onAdClosed(adInfo: LevelPlayAdInfo) { loadRewarded() }
        })
    }

    fun loadRewarded() {
        rewardedAd?.loadAd()
    }

    // --- 배너 광고 (Compose 용으로 수정) ---
    @Composable
    fun BannerAdView(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        AndroidView(
            modifier = modifier,
            factory = {
                // 배너 뷰를 생성하고 로드
                val adSize = LevelPlayAdSize.BANNER
                val config = LevelPlayBannerAdView.Config.Builder().setAdSize(adSize).build()
                LevelPlayBannerAdView(context, BANNER_AD_UNIT_ID, config).apply {
                    setBannerListener(object : LevelPlayBannerAdViewListener {
                        override fun onAdLoaded(adInfo: LevelPlayAdInfo) { Log.d(TAG, "배너 광고 로드 성공") }
                        override fun onAdLoadFailed(error: LevelPlayAdError) { Log.e(TAG, "배너 광고 로드 실패: ${error.getErrorMessage()}") }
                        override fun onAdClicked(adInfo: LevelPlayAdInfo) {}
                        override fun onAdDisplayed(adInfo: LevelPlayAdInfo) {}
                    })
                    loadAd()
                }
            },
            onRelease = { bannerView ->
                bannerView.destroy()
            }
        )
    }


    // --- 네이티브 광고 (Compose 용으로 수정) ---
    @SuppressLint("ContextCastToActivity")
    @Composable
    fun NativeAdView(
        placementName: String,
        modifier: Modifier = Modifier,
        onAdLoaded: @Composable (LevelPlayNativeAd) -> Unit
    ) {
        val activity = LocalContext.current as Activity
        var nativeAd by remember { mutableStateOf<LevelPlayNativeAd?>(null) }

        // Composable이 화면에 나타날 때 광고를 로드하고, 사라질 때 광고를 파괴
        LaunchedEffect(placementName) {
            val nativeAdLoader = LevelPlayNativeAd.Builder()
                .withActivity(activity)
                .withPlacementName(placementName)
                .withListener(object : LevelPlayNativeAdListener {
                    override fun onAdLoaded(ad: LevelPlayNativeAd?, adInfo: AdInfo?) {
                        Log.d(TAG, "네이티브 광고 로드 성공")
                        nativeAd = ad
                    }
                    override fun onAdLoadFailed(ad: LevelPlayNativeAd?, error: IronSourceError?) {
                        Log.e(TAG, "네이티브 광고 로드 실패: ${error?.errorMessage}")
                        ad?.destroyAd()
                        nativeAd = null
                    }
                    override fun onAdClicked(ad: LevelPlayNativeAd?, adInfo: AdInfo?) {}
                    override fun onAdImpression(ad: LevelPlayNativeAd?, adInfo: AdInfo?) {}
                })
                .build()
            nativeAdLoader.loadAd()
        }

        DisposableEffect(Unit) {
            onDispose {
                nativeAd?.destroyAd()
            }
        }

        // 광고가 로드되면 onAdLoaded 람다를 호출하여 UI를 그림
        nativeAd?.let { ad ->
            onAdLoaded(ad)
        }
    }
}
