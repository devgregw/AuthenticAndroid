package church.authenticcity.android.classes

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 4/8/2018 at 3:24 PM.
 * Licensed under the MIT License.
 */
class AuthenticAppearance(data: HashMap<String, Any>) {

    class Events(data: HashMap<String, Any>) {
        val title: String = data["title"] as String
        val header: ImageResource = ImageResource(data["header"] as HashMap<String, Any>)

    }

    class Tabs(data: HashMap<String, Any>) {
        val fill = if (data.containsKey("fill")) data["fill"].toString().toBoolean() else true
    }

    val events = Events(data["events"] as HashMap<String, Any>)
    val tabs = Tabs(data["tabs"] as HashMap<String, Any>)
}