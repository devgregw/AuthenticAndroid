package church.authenticcity.android.classes

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import church.authenticcity.android.R
import church.authenticcity.android.helpers.Utils


/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 2/1/2018 at 8:18 PM.
 * Licensed under the MIT License.
 */

class AuthenticElement(private val map: HashMap<String, Any>) {
    companion object {
        const val VIDEO_PROVIDER_YOUTUBE = "YouTube"
        const val VIDEO_PROVIDER_VIMEO = "Vimeo"
    }

    val id: String = map["id"] as String
    val parent: String = map["parent"] as String
    val type: String = map["type"] as String

    private fun <T> getProperty(key: String) = map[key] as T

    fun toView(context: Context) =
            when (type) {
                "image" -> ImageView(context).apply { Utils.loadFirebaseImage(context, getProperty("image"), this) }
                "video" -> WebView(context).apply {
                    val id = getProperty<String>("videoId")
                    this.loadUrl(if (getProperty<String>("provider") == AuthenticElement.VIDEO_PROVIDER_YOUTUBE) "https://www.youtube.com/embed/$id" else "https://player.vimeo.com/video/$id")
                    layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300f, context.resources.displayMetrics).toInt()).apply { setMargins(0, 0, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, context.resources.displayMetrics).toInt()) }
                    this.settings.javaScriptEnabled = true
                    setBackgroundColor(Color.BLACK)
                }
                "title" -> TextView(context).apply {
                    textSize = 22f
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    if (Utils.checkSdk(17))
                        textAlignment = when (getProperty<String>("alignment")) {
                            "left" -> TextView.TEXT_ALIGNMENT_TEXT_START
                            "center" -> TextView.TEXT_ALIGNMENT_CENTER
                            "right" -> TextView.TEXT_ALIGNMENT_TEXT_END
                            else -> TextView.TEXT_ALIGNMENT_INHERIT
                        }
                    setTextColor(Color.BLACK)
                    typeface = Utils.getTitleTypeface(context)
                    text = getProperty("title")
                    layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                        setMargins(px, 0, px, 0)
                    }
                }
                "text" -> TextView(context).apply {
                    textSize = 18f
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    if (Utils.checkSdk(17))
                        textAlignment = when (getProperty<String>("alignment")) {
                            "left" -> TextView.TEXT_ALIGNMENT_TEXT_START
                            "center" -> TextView.TEXT_ALIGNMENT_CENTER
                            "right" -> TextView.TEXT_ALIGNMENT_TEXT_END
                            else -> TextView.TEXT_ALIGNMENT_INHERIT
                        }
                    setTextColor(Color.BLACK)
                    typeface = Utils.getTextTypeface(context)
                    text = type
                    layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                        setMargins(px, 0, px, 0)
                    }
                }
                "button" -> Button(context).apply {
                    val infoMap = getProperty<HashMap<String, Any>>("_buttonInfo")
                    text = infoMap["label"] as String
                    typeface = Utils.getTextTypeface(context)
                    val action = ButtonAction(infoMap["action"] as HashMap<String, Any>)
                    setOnClickListener { action.invoke(context) }
                    layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                        setMargins(px, 0, px, 0)
                    }
                }
                "separator" -> LinearLayout(context).apply {
                    visibility = if (getProperty("visible")) View.VISIBLE else View.INVISIBLE
                    setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackground))
                    layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.resources.displayMetrics).toInt()).apply {
                        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                        setMargins(px, px, px, px)
                    }
                }
                else -> TextView(context).apply {
                    textSize = 18f
                    setTextColor(Color.RED)
                    typeface = Utils.getTextTypeface(context)
                    text = String.format("Invalid element type: %s; ID: %s; parent: %s", type, this@AuthenticElement.id, this@AuthenticElement.parent)
                }
            }
}