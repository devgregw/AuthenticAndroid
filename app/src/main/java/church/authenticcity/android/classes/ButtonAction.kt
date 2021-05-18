package church.authenticcity.android.classes

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.appcompat.app.AlertDialog
import church.authenticcity.android.activities.FragmentActivity
import church.authenticcity.android.helpers.RecurrenceRule
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyColorsAndTypefaces
import church.authenticcity.android.helpers.isNullOrWhiteSpace
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

class ButtonAction(private val map: HashMap<String, Any>) {
    companion object {
        fun openUrl(url: String): ButtonAction = ButtonAction(HashMap<String, Any>().apply {
            put("type", "OpenURLAction")
            put("group", -1)
            put("url", url)
        })

        val empty = ButtonAction(HashMap<String, Any>().apply {
            put("group", -1)
            put("type", "none")
        })
    }

    private val group = map["group"].toString().toInt()
    val type = map["type"] as String
    private var properties = HashMap<String, Any>(map.filter { it.key != "group" && it.key != "type" })

    @Suppress("UNCHECKED_CAST")
    fun <T> get(name: String): T = properties[name] as T

    private fun <T> tryGet(name: String): T? = if (properties.containsKey(name)) get(name) else null

    private fun showAlert(context: Context, title: String, message: String) {
        AlertDialog.Builder(context).setTitle(Utils.makeTypefaceSpan(context, title, Utils.getTitleTypeface(context))).setCancelable(true).setMessage(Utils.makeTypefaceSpan(context, message, Utils.getTextTypeface(context))).setPositiveButton(Utils.makeTypefaceSpan(context,"Dismiss", Utils.getTextTypeface(context)), null).create().applyColorsAndTypefaces().show()
    }

    fun invoke(context: Context) {
        try {
            Log.v("ButtonAction", String.format("Invoking %s", map))
            when (type) {
                "OpenEventsPageAction" -> FragmentActivity.startEventList(context)
                "OpenTabAction" -> FragmentActivity.startTab(context, get("tabId"))
                "OpenEventAction" -> FragmentActivity.startEvent(context, get("eventId"))
                "OpenURLAction" -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(get<String>("url")))
                    context.startActivity(Intent.createChooser(intent, "Choose an app to open this"))
                }
                "OpenYouTubeAction" -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(get<String>("youtubeUri")))
                    when (context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).count()) {
                        0 -> context.startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(get<String>("watchUrl"))), "Choose an app to open this"))
                        else -> context.startActivity(intent)
                    }
                }
                "OpenSpotifyAction" -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(get<String>("spotifyUri")))
                    when (context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).count()) {
                        0 -> context.startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(get<String>("spotifyUrl"))), "Choose an app to open this"))
                        else -> context.startActivity(intent)
                    }
                }
                "ShowMapAction" -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(get<String>("address")))).apply { `package` = "com.google.android.apps.maps" })
                "GetDirectionsAction" -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + Uri.encode(get<String>("address")))).apply { `package` = "com.google.android.apps.maps" })
                "EmailAction" -> context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + get<String>("emailAddress"))))
                "AddToCalendarAction" -> {
                    val eventTitle: String
                    val startDate: ZonedDateTime
                    val endDate: ZonedDateTime
                    val location: String
                    val recurrenceRule: RecurrenceRule?
                    if (group > 2) {
                        showAlert(context, "Error", "We were unable to add this event to your calendar because an invalid parameter group was specified.")
                        return
                    }
                    when (group) {
                        0 -> {
                            val event = Utils.Temp.getEvent(get("eventId"))!!
                            eventTitle = event.title
                            startDate = event.startDate
                            endDate = event.endDate
                            location = if (!String.isNullOrWhiteSpace(event.address)) event.address else event.location
                            recurrenceRule = event.recurrenceRule
                        }
                        1 -> {
                            eventTitle = get("title")
                            val dates = get<HashMap<String, Any>>("dates")
                            startDate = OffsetDateTime.parse(dates["start"] as String, DateTimeFormatter.ISO_DATE_TIME).atZoneSameInstant(ZoneId.systemDefault())
                            endDate = OffsetDateTime.parse(dates["end"] as String, DateTimeFormatter.ISO_DATE_TIME).atZoneSameInstant(ZoneId.systemDefault())
                            location = get("location")
                            val rrule = tryGet<HashMap<String, Any>>("recurrence")
                            recurrenceRule = if (rrule == null) null else RecurrenceRule(rrule)
                        }
                        else -> throw IllegalStateException("Group $group is not legal for AddToCalendarAction")
                    }
                    val intent = Intent(Intent.ACTION_INSERT)
                    intent.data = CalendarContract.Events.CONTENT_URI
                    intent.putExtra(CalendarContract.Events.TITLE, eventTitle)
                    intent.putExtra(CalendarContract.Events.ALL_DAY, false)
                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDate.toEpochSecond() * 1000)
                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDate.toEpochSecond() * 1000)
                    intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                    intent.putExtra(CalendarContract.Events.RRULE, recurrenceRule?.getRRule() ?: "")
                    context.startActivity(intent)
                }
                "none" -> {}
                else -> showAlert(context, "Error", "We were unable to run this action because the type \"$type\" is undefined.")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            Utils.reportAndAlertException(context, ex, this.javaClass.getMethod("invoke", Context::class.java).toString())
        }
    }
}