package co.koko.babyfaceai

import android.app.Application
import android.util.Log
import co.koko.babyfaceai.util.AdManagerCompose

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AdManagerCompose.initialize(this)
        Log.d("AdInit", "AdManagerCompose initialized")

    }
}