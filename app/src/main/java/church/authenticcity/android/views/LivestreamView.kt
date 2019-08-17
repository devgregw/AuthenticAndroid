package church.authenticcity.android.views

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import church.authenticcity.android.R
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.isNullOrWhiteSpace
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import kotlinx.android.synthetic.main.view_livestream.view.*

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 6/9/2018 at 8:20 PM.
 * Licensed under the MIT License.
 */
class LivestreamView {
    companion object {
        const val height = 80

        fun create(context: Context, viewGroup: ViewGroup): RelativeLayout {
            val view = LayoutInflater.from(context).inflate(R.layout.view_livestream, viewGroup, false) as RelativeLayout
            view.apply {
                layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT/*context.resources.displayMetrics.widthPixels / 4*/).apply { addRule(RelativeLayout.BELOW, R.id.title) }
            }
            view.livestream_progress.animate().setStartDelay(0L).alpha(1f).duration = 250L
            view.livestream_watch.typeface = Utils.getTitleTypeface(context)
            view.livestream_sundays.typeface = Utils.getTitleTypeface(context)
            view.livestream_services.typeface = Utils.getTitleTypeface(context)
            val setText: (Boolean) -> Unit = { isLive ->
                view.livestream_progress.animate().setStartDelay(0L).alpha(0f).duration = 250L
                view.livestream_watch.animate().setStartDelay(250L).alpha(if (isLive) 1f else 0f).duration = 250L
                view.livestream_text.animate().setStartDelay(250L).alpha(if (isLive) 0f else 1f).duration = 250L
            }
            val queue = Volley.newRequestQueue(context)
            //authentic: UCxrYck_z50n5It7ifj1LCjA
            //demo:      UCSJ4gkVC6NrvII8umztf0Ow
            val channel = "UCxrYck_z50n5It7ifj1LCjA"
            val url = "https://www.googleapis.com/youtube/v3/search?part=snippet&channelId=$channel&type=video&eventType=live&key=AIzaSyB4w3GIY9AUi6cApXAkB76vlG6K6J4d8XE"
            queue.add(StringRequest(Request.Method.GET, url, { str ->
                val obj = Parser().parse(StringBuilder(str)) as JsonObject
                val items = obj.array<JsonObject>("items")!!
                if (items.size == 0) {
                    setText(false)
                } else {
                    val result = items[0]
                    val videoId = result.obj("id")!!.string("videoId")!!
                    view.setOnClickListener {
                        //VideoActivity.start(context, "YouTube", videoId, "LIVESTREAM")
                        ButtonAction.openUrl("https://youtube.com/watch?v=$videoId").invoke(context)
                    }
                    setText(true)
                }
            }, { err ->
                setText(false)
                Log.e("Livestream", if (String.isNullOrWhiteSpace(err.message)) "<no message>" else err.message)
                err.printStackTrace()
            }).setShouldCache(false))
            return view
        }
    }
}