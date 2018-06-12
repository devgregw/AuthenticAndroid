package church.authenticcity.android.views

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.ViewSwitcher
import church.authenticcity.android.R
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.helpers.Utils
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
        fun create(context: Context, viewGroup: ViewGroup): ViewSwitcher {
            val view = LayoutInflater.from(context).inflate(R.layout.view_livestream, viewGroup, false) as ViewSwitcher
            view.apply {
                layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.resources.displayMetrics.widthPixels / 2).apply { addRule(RelativeLayout.BELOW, R.id.title) }
            }
            view.livestream_textView.typeface = Utils.getTitleTypeface(context)
            val setText: (Boolean) -> Unit = { isLive ->
                view.livestream_textView.text = if (isLive) "WATCH LIVE ON YOUTUBE" else "SUNDAYS AT 6:30 PM"
                view.showNext()
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
                    val videoId = result.obj("id")!!.string("videoId")
                    view.setOnClickListener {
                        ButtonAction.openUrl("https://youtube.com/watch?v=$videoId").invoke(context)
                    }
                    setText(true)
                }
            }, { err ->
                setText(false)
                Log.e("Livestream", err.localizedMessage)
                err.printStackTrace()
            }).setShouldCache(false))
            return view
        }
    }
}