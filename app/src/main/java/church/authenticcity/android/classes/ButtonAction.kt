package church.authenticcity.android.classes

import android.content.Context
import android.util.Log
import android.widget.Toast
import church.authenticcity.android.TabActivity

/**
 * Created by devgr on 12/26/2017.
 */
class ButtonAction(private val map: HashMap<String, Any>) {
    var group: Int = -1
        get() = map["group"].toString().toInt()
    var type: String = ""
        get() = map["type"].toString()
    var properties: HashMap<String, Any> = HashMap()
        get() = HashMap(map.filter { it.key != "group" && it.key != "type" })

    fun <T> get(name: String): T = properties[name] as T

    fun invoke(context: Context) {
        Log.v("ButtonAction", String.format("Invoking %s", map))
        when (type) {
            "OpenTabAction" -> TabActivity.start(context, get<String>("tabId"))
            else -> Toast.makeText(context, String.format("Group: %s, Type: %s, Props: %s", group, type, properties), Toast.LENGTH_LONG).show()
        }
    }
}