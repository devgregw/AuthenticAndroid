package church.authenticcity.android.classes

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
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
        fun createImage(context: Context, image: String) = ImageView(context).apply { Utils.loadFirebaseImage(context, image, this) }

        fun createVideo(context: Context, videoId: String, provider: String) =
                WebView(context).apply {
                    this.loadUrl(if (provider == "YouTube") "https://www.youtube.com/embed/$videoId" else "https://player.vimeo.com/video/$videoId")
                    layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300f, context.resources.displayMetrics).toInt()).apply { setMargins(0, 0, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, context.resources.displayMetrics).toInt()) }
                    this.settings.javaScriptEnabled = true
                    setBackgroundColor(Color.BLACK)
                }

        fun createCustomText(context: Context, text: String, size: Float, typeface: Typeface = Typeface.DEFAULT, alignment: String = "left", color: Int = Color.BLACK, selectable: Boolean = false) =
                TextView(context).apply {
                    textSize = size
                    setTextIsSelectable(selectable)
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    if (Utils.checkSdk(17))
                        textAlignment = when (alignment) {
                            "left" -> TextView.TEXT_ALIGNMENT_TEXT_START
                            "center" -> TextView.TEXT_ALIGNMENT_CENTER
                            "right" -> TextView.TEXT_ALIGNMENT_TEXT_END
                            else -> TextView.TEXT_ALIGNMENT_INHERIT
                        }
                    setTextColor(color)
                    this.typeface = typeface
                    this.text = text
                    layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                        setMargins(px, 0, px, 0)
                    }
                }

        fun createTitle(context: Context, text: String, alignment: String, selectable: Boolean = false) = createCustomText(context, text, 26f, Utils.getTitleTypeface(context), alignment, selectable = selectable)

        fun createText(context: Context, text: String, alignment: String, selectable: Boolean = false) = createCustomText(context, text, 18f, Utils.getTextTypeface(context), alignment, selectable = selectable)

        fun createButton(context: Context, action: ButtonAction, text: String) =
                Button(context).apply {
                    this.text = text
                    typeface = Utils.getTextTypeface(context)
                    //if (action.isExternal)
                        //setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.ic_open_in_new_white_18dp), null)
                    setOnClickListener { action.invoke(context) }
                    layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                        setMargins(px, 0, px, 0)
                    }
                }

        fun createButton(context: Context, info: HashMap<String, Any>) = createButton(context, ButtonAction(info["action"] as HashMap<String, Any>), info["label"] as String)

        fun createSeparator(context: Context, visible: Boolean) =
                LinearLayout(context).apply {
                    visibility = if (visible) View.VISIBLE else View.INVISIBLE
                    setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackground))
                    layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.resources.displayMetrics).toInt()).apply {
                        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                        setMargins(px, px, px, px)
                    }
                }
    }

    val id: String = map["id"] as String
    val parent: String = map["parent"] as String
    val type: String = map["type"] as String

    private fun <T> getProperty(key: String) = map[key] as T

    fun toView(context: Context) =
            when (type) {
                "image" -> createImage(context, getProperty("image"))
                "video" -> createVideo(context, getProperty("videoId"), getProperty("provider"))
                "title" -> createTitle(context, getProperty("title"), getProperty("alignment"))
                "text" -> createText(context, getProperty("text"), getProperty("alignment"))
                "button" -> createButton(context, getProperty("_buttonInfo"))
                "separator" -> createSeparator(context, getProperty("visible"))
                else -> createCustomText(context, String.format("Invalid element type: %s; ID: %s; parent: %s", type, this@AuthenticElement.id, this@AuthenticElement.parent), 18f, Utils.getTextTypeface(context))
            }
}