package church.authenticcity.android

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 4/14/2018 at 7:38 AM.
 * Licensed under the MIT License.
 */
class AuthenticApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}