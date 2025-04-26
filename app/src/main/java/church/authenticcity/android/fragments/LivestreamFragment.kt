package church.authenticcity.android.fragments

import android.graphics.Color
import android.util.Log
import android.view.View
import church.authenticcity.android.BuildConfig
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.classes.ImageResource
import church.authenticcity.android.databinding.FragmentLivestreamBinding
import church.authenticcity.android.helpers.DatabaseHelper
import church.authenticcity.android.helpers.Utils
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.android.material.snackbar.Snackbar
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class LivestreamFragment : AuthenticFragment<FragmentLivestreamBinding>() {
    companion object {
        fun getInstance() = LivestreamFragment().apply {
            setup("LIVE", {i, c, a -> FragmentLivestreamBinding.inflate(i, c, a)})
        }
    }

    override val root
        get() = binding?.root
    
    override fun onCreateView(view: View) {
        binding?.livestreamImage?.setOnClickListener {
            this@LivestreamFragment.checkLivestreamStatus()
        }
        DatabaseHelper.loadAppearance { authenticAppearance ->
            if (authenticAppearance.livestream.image != null && binding != null) {
                view.setBackgroundColor(authenticAppearance.livestream.color)
                ImageResource(authenticAppearance.livestream.image, 1080, 1920).load(view.context, binding!!.livestreamImage)
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
            checkingSnackbar = Snackbar.make(requireView(), Utils.makeTypefaceSpan("LOADING...", requireView().context, 10), Snackbar.LENGTH_INDEFINITE)
            checkingSnackbar?.show()
            val queue = Volley.newRequestQueue(requireView().context)
            request = StringRequest(Request.Method.GET, "https://us-central1-authentic-city-church.cloudfunctions.net/videos", {
                checkingSnackbar!!.dismiss()
                val obj = Parser.default().parse(StringBuilder(it)) as JsonObject
                if (obj.isNotEmpty()) {
                    if (obj.containsKey("livestream")) {
                        actionSnackbar = Snackbar.make(requireView(), Utils.makeTypefaceSpan("WE'RE LIVE", requireView().context, 12), Snackbar.LENGTH_INDEFINITE)
                        actionSnackbar?.setAction(Utils.makeTypefaceSpan("TAP TO WATCH NOW", requireView().context, 12)) {
                            ButtonAction.openUrl("https://youtube.com/watch?v=${obj["livestream"].toString()}").invoke(requireView().context)
                        }
                        actionSnackbar?.setActionTextColor(Color.WHITE)
                        actionSnackbar?.show()
                    } else {
                        if (view != null)
                            Snackbar.make(requireView(), Utils.makeTypefaceSpan("WE'RE NOT LIVE", requireView().context, 10), Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    if (view != null)
                        Snackbar.make(requireView(), Utils.makeTypefaceSpan("WE'RE NOT LIVE", requireView().context, 10), Snackbar.LENGTH_SHORT).show()
                }
            }, {
                checkingSnackbar?.dismiss()
                if (view != null)
                    Snackbar.make(requireView(), Utils.makeTypefaceSpan("ERROR WHILE CHECKING FOR LIVESTREAM", requireView().context, 10), Snackbar.LENGTH_SHORT).show()
                when (it) {
                    is NetworkError -> {
                        Log.e("Livestream", "NetworkError: " + (it.localizedMessage ?: "<null>"))
                    }

                    is TimeoutError, is NoConnectionError -> {
                        Log.e("Livestream", "TimeoutError or NoConnectionError: " + (it.localizedMessage ?: "<null>"))
                    }

                    is ServerError -> {
                        Log.e("Livestream", "ServerError: " + (it.localizedMessage ?: "<null>"))
                    }

                    else -> {
                        Log.e("Livestream", "Unknown: " + (it.localizedMessage ?: "<null>"))
                    }
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