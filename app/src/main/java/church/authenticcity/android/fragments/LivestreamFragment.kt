package church.authenticcity.android.fragments

import android.graphics.Color
import android.util.Log
import android.view.View
import church.authenticcity.android.R
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.helpers.Utils
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.android.material.snackbar.Snackbar
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class LivestreamFragment : AuthenticFragment("LIVE", R.layout.fragment_livestream, null) {
    override fun onCreateView(view: View) {
    }

    override fun onRefreshView(view: View) {
    }

    private var checkingSnackbar: Snackbar? = null
    private var actionSnackbar: Snackbar? = null
    private var request: StringRequest? = null

    fun checkLivestreamStatus() {
        actionSnackbar?.dismiss()
        checkingSnackbar?.dismiss()
        if (view == null) {
            Log.e("AUTHENTIC", "view is null!")
            return
        }
        val date = LocalDateTime.now(ZoneId.systemDefault())
        if (date.dayOfWeek == DayOfWeek.SUNDAY) {
            checkingSnackbar = Snackbar.make(view!!, Utils.makeTypefaceSpan("LOADING...", view!!.context, 10), Snackbar.LENGTH_INDEFINITE)
            checkingSnackbar?.show()
            val queue = Volley.newRequestQueue(view!!.context)
            request = StringRequest(Request.Method.GET, "https://us-central1-authentic-city-church.cloudfunctions.net/videos", {
                checkingSnackbar!!.dismiss()
                val obj = Parser().parse(StringBuilder(it)) as JsonObject
                if (obj.count() > 0) {
                    if (obj.values.map { o ->
                                o?.toString() ?: ""
                            }[0].contains("stream", true)) {
                        actionSnackbar = Snackbar.make(view!!, Utils.makeTypefaceSpan("WE'RE LIVE", view!!.context, 12), Snackbar.LENGTH_INDEFINITE)
                        actionSnackbar?.setAction(Utils.makeTypefaceSpan("TAP TO WATCH NOW", view!!.context, 12)) {
                            ButtonAction.openUrl("https://youtube.com/watch?v=${obj.keys.elementAt(0)}").invoke(view!!.context)
                        }
                        actionSnackbar?.setActionTextColor(Color.WHITE)
                        actionSnackbar?.show()
                    } else {
                        Snackbar.make(view!!, Utils.makeTypefaceSpan("WE'RE NOT LIVE", view!!.context, 10), Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    Snackbar.make(view!!, Utils.makeTypefaceSpan("WE'RE NOT LIVE", view!!.context, 10), Snackbar.LENGTH_SHORT).show()
                }
            }, {
                checkingSnackbar?.dismiss()
                Snackbar.make(view!!, Utils.makeTypefaceSpan("ERROR WHILE CHECKING FOR LIVESTREAM", view!!.context, 10), Snackbar.LENGTH_SHORT).show()
            })
            request!!.setShouldCache(false)
            queue.add(request!!)
        }
    }

    fun cancel() {
        request?.cancel()
        actionSnackbar?.dismiss()
        checkingSnackbar?.dismiss()
    }
}