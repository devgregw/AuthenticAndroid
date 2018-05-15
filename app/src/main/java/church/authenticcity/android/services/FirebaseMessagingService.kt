package church.authenticcity.android.services

import android.content.Context
import android.os.Bundle
import church.authenticcity.android.classes.ButtonAction
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class FirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private var action: ButtonAction? = null

        fun invokeNotificationAction(context: Context) {
            action?.invoke(context)
            action = null
        }

        fun setAction(bundle: Bundle?) {
            if (bundle != null && !bundle.isEmpty) {
                if (!bundle.containsKey("type") || !bundle.containsKey("group"))
                    return
                val map = HashMap<String, Any>()
                bundle.keySet().forEach { map.put(it, bundle.get(it)) }
                action = ButtonAction(map)
            }
        }
    }

    override fun onMessageReceived(p0: RemoteMessage?) {
    }
}
