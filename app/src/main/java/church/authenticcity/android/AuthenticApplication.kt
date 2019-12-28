package church.authenticcity.android

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.FirebaseDatabase

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 4/14/2018 at 7:38 AM.
 * Licensed under the MIT License.
 */
class AuthenticApplication: Application() {
    companion object {
        var useDevelopmentDatabase = false
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(false)
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
    }
}