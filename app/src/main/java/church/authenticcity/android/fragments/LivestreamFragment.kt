package church.authenticcity.android.fragments

import android.graphics.Color
import android.util.Log
import android.view.View
import church.authenticcity.android.BuildConfig
import church.authenticcity.android.R
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.classes.ImageResource
import church.authenticcity.android.helpers.DatabaseHelper
import church.authenticcity.android.helpers.Utils
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_livestream.view.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import java.io.Console

class LivestreamFragment : AuthenticFragment("LIVE", R.layout.fragment_livestream, null) {
    override fun onCreateView(view: View) {
        view.livestream_image.setOnClickListener {
            this@LivestreamFragment.checkLivestreamStatus()
        }
        DatabaseHelper.loadAppearance { authenticAppearance ->
            if (authenticAppearance.livestream.image != null) {
                view.setBackgroundColor(authenticAppearance.livestream.color)
                ImageResource(authenticAppearance.livestream.image, 1080, 1920).load(view.context, view.livestream_image)
            }
        }
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
        if (date.dayOfWeek == DayOfWeek.SUNDAY || BuildConfig.DEBUG) {
            checkingSnackbar = Snackbar.make(view!!, Utils.makeTypefaceSpan("LOADING...", view!!.context, 10), Snackbar.LENGTH_INDEFINITE)
            checkingSnackbar?.show()
            val queue = Volley.newRequestQueue(view!!.context)
            request = StringRequest(Request.Method.GET, "https://us-central1-authentic-city-church.cloudfunctions.net/videos", {
                checkingSnackbar!!.dismiss()
                val obj = Parser().parse(StringBuilder(it)) as JsonObject
                if (obj.count() > 0) {
                    if (obj.containsKey("livestream")) {
                        actionSnackbar = Snackbar.make(view!!, Utils.makeTypefaceSpan("WE'RE LIVE", view!!.context, 12), Snackbar.LENGTH_INDEFINITE)
                        actionSnackbar?.setAction(Utils.makeTypefaceSpan("TAP TO WATCH NOW", view!!.context, 12)) {
                            ButtonAction.openUrl("https://youtube.com/watch?v=${obj["livestream"].toString()}").invoke(view!!.context)
                        }
                        actionSnackbar?.setActionTextColor(Color.WHITE)
                        actionSnackbar?.show()
                    } else {
                        if (view != null)
                            Snackbar.make(view!!, Utils.makeTypefaceSpan("WE'RE NOT LIVE", view!!.context, 10), Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    if (view != null)
                        Snackbar.make(view!!, Utils.makeTypefaceSpan("WE'RE NOT LIVE", view!!.context, 10), Snackbar.LENGTH_SHORT).show()
                }
            }, {
                checkingSnackbar?.dismiss()
                if (view != null)
                    Snackbar.make(view!!, Utils.makeTypefaceSpan("ERROR WHILE CHECKING FOR LIVESTREAM", view!!.context, 10), Snackbar.LENGTH_SHORT).show()
                if (it is NetworkError) {
                    Log.e("Livestream", "NetworkError: " + (it.localizedMessage ?: "<null>"))
                } else if (it is TimeoutError || it is NoConnectionError) {
                    Log.e("Livestream", "TimeoutError or NoConnectionError: " + (it.localizedMessage ?: "<null>"))
                } else if (it is ServerError) {
                    Log.e("Livestream", "ServerError: " + (it.localizedMessage ?: "<null>"))
                } else {
                    Log.e("Livestream", "Unknown: " + (it.localizedMessage ?: "<null>"))
                }
            })
            request!!.setShouldCache(false)
            request!!.retryPolicy = DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(request!!)
        }
    }

    fun cancel() {
        request?.cancel()
        actionSnackbar?.dismiss()
        checkingSnackbar?.dismiss()
    }
}