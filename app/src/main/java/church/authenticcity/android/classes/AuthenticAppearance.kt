package church.authenticcity.android.classes

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 4/8/2018 at 3:24 PM.
 * Licensed under the MIT License.
 */
class AuthenticAppearance(data: HashMap<String, Any>) {
    companion object {
        val default = AuthenticAppearance(HashMap<String, Any>().apply {
            put("tabs", HashMap<String, Any>())
            put("events", HashMap<String, Any>())
            put("livestream", HashMap<String, Any>())
        })
    }

    class Events(data: HashMap<String, Any>) {
        val title: String = data["title"] as? String ?: "UPCOMING EVENTS"
        val header: ImageResource = if (data.containsKey("header")) ImageResource(data["header"] as HashMap<String, Any>) else ImageResource("unknown.png", 1920, 1080)
        val index: Int = (data["index"] ?: "-999").toString().toInt()
    }

    class Livestream(data: HashMap<String, Any>) {
        val enable = (data["enable"] ?: "true").toString().toBoolean()
    }

    val events = Events(data["events"] as HashMap<String, Any>)
    val livestream = Livestream(data["livestream"] as HashMap<String, Any>)
}