package org.orma.project_90

import android.app.Application
import com.onesignal.OneSignal

private const val OrmaOneSignalAppId = "60d2a2a5-e140-46ba-abca-aa2216043e03"

class OrmaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        OneSignal.initWithContext(this, OrmaOneSignalAppId)
    }
}
