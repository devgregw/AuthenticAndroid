package church.authenticcity.android.classes

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 4/8/2018 at 3:24 PM.
 * Licensed under the MIT License.
 */
class AuthenticAppearance(data: HashMap<String, Any>) {

    class Events(data: HashMap<String, Any>) {
        val title: String
        val hideTitle: Boolean
        val header: String

        init {
            title = data["title"] as String
            hideTitle = data["hideTitle"] as Boolean
            header = data["header"] as String
        }
    }

    val events: Events

    init {
        events = Events(data["events"] as HashMap<String, Any>)
    }

}