package church.authenticcity.android.services

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.google.firebase.messaging.FirebaseMessaging

class FirebaseInstanceIDService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        val iid = FirebaseInstanceId.getInstance()
        Log.i("ACC Firebase IID", "Firebase token refresh: ${iid.token}")
        FirebaseMessaging.getInstance().subscribeToTopic("main")
    }
}
