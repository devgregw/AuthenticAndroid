package church.authenticcity.android.classes

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.util.Log
import church.authenticcity.android.TabActivity
import church.authenticcity.android.helpers.applyColorsAndTypefaces

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
            "OpenURLAction" -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(get<String>("url")))
                when (context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).count()) {
                    0 -> context.startActivity(intent)
                    else -> context.startActivity(Intent.createChooser(intent, "Choose an app"))
                }
            }
            "GetDirectionsAction" -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + get<String>("address"))).apply { `package` = "com.google.android.apps.maps" })
            "EmailAction" -> context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + get<String>("emailAddress"))))
            else -> AlertDialog.Builder(context).setTitle("Unknown Action").setMessage("We were unable to parse this action.").setPositiveButton("OK", null).create().applyColorsAndTypefaces().show()//Toast.makeText(context, String.format("Group: %s, Type: %s, Props: %s", group, type, properties), Toast.LENGTH_LONG).show()
        }
    }
}