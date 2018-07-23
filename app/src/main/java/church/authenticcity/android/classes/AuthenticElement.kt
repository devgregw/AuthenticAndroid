package church.authenticcity.android.classes

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import church.authenticcity.android.R
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.views.VideoLinkView
import java.util.*
import kotlin.math.roundToInt


/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 2/1/2018 at 8:18 PM.
 * Licensed under the MIT License.
 */

class AuthenticElement(private val map: HashMap<String, Any>) {
    companion object {
        fun createImage(context: Context, image: ImageResource, enlargable: Boolean): View {
            val imageView = ImageView(context).apply {
                val adjustedWidth = context.resources.displayMetrics.widthPixels
                val ratio = image.width.toFloat() / (if (image.height == 0) 1 else image.height).toFloat()
                val adjustedHeight = (adjustedWidth / ratio).roundToInt()
                layoutParams = ViewGroup.LayoutParams(adjustedWidth, adjustedHeight)
                Utils.loadFirebaseImage(context, image.imageName, this)
                val rand = Random().nextInt(256)
                setBackgroundColor(Color.argb(255, rand, rand, rand))
            }
            if (enlargable)
                return LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    imageView.setOnClickListener {
                        ButtonAction.openUrl("https://accams.devgregw.com/meta/storage/${image.imageName}").invoke(context)
                    }
                    addView(imageView)
                    addView(createText(context, "Tap to open.", "left", color = Color.DKGRAY, size = 14f))
                }
            return imageView
        }

        fun createVideo(context: Context, provider: String, id: String, thumbnail: String, title: String) =
                VideoLinkView(context, provider, id, thumbnail, title)
                /*WebView(context).apply {
                    this.loadUrl(if (provider == "YouTube") "https://www.youtube.com/embed/$videoId" else "https://player.vimeo.com/video/$videoId")
                    layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300f, context.resources.displayMetrics).toInt()).apply { setMargins(0, 0, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, context.resources.displayMetrics).toInt()) }
                    this.settings.javaScriptEnabled = true
                    setBackgroundColor(Color.BLACK)
                }*/

        fun createTitle(context: Context, text: String, alignment: String, size: Float = 24f, selectable: Boolean = false) = TextView(context).apply {
            textSize = size
            setTextIsSelectable(selectable)
            val dimen = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).roundToInt()
            setPadding(dimen, dimen, dimen, dimen)
            /*textAlignment = when (alignment) {
                "center" -> TextView.TEXT_ALIGNMENT_CENTER
                "right" -> TextView.TEXT_ALIGNMENT_TEXT_END
                else -> TextView.TEXT_ALIGNMENT_TEXT_START
            }*/
            setTextColor(Color.BLACK)
            setBackgroundResource(R.drawable.title_border_black)
            this.typeface = Utils.getTitleTypeface(context)
            this.text = text
            letterSpacing = 0.15f
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = when (alignment) {
                    "center" -> Gravity.CENTER_HORIZONTAL
                    "right" -> Gravity.END
                    else -> Gravity.START
                }
                val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                setMargins(px, px / 8, px, px / 8)
            }
        }

        fun createText(context: Context, text: String, alignment: String, color: Int = Color.BLACK, size: Float = 20f, selectable: Boolean = false) = TextView(context).apply {
            textSize = size
            setTextIsSelectable(selectable)
            val dimen = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, context.resources.displayMetrics).roundToInt()
            setPadding(dimen, dimen, dimen, dimen)
            textAlignment = when (alignment) {
                "left" -> TextView.TEXT_ALIGNMENT_TEXT_START
                "center" -> TextView.TEXT_ALIGNMENT_CENTER
                "right" -> TextView.TEXT_ALIGNMENT_TEXT_END
                else -> TextView.TEXT_ALIGNMENT_INHERIT
            }
            setTextColor(color)
            this.typeface = Utils.getTextTypeface(context)
            this.text = text
            layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                setMargins(px, px / 8, px, px / 8)
            }
        }

        fun createButton(context: Context, action: ButtonAction, text: String) =
                Button(context).apply {
                    this.text = text
                    val dimen = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).roundToInt()
                    setPadding(dimen, dimen, dimen, dimen)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
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

    @Suppress("UNCHECKED_CAST")
    private fun <T> getProperty(key: String, default: T) = if (map.containsKey(key)) map[key] as T else default

    fun toView(context: Context) =
            when (type) {
                "image" -> createImage(context, ImageResource(getProperty("image", HashMap<String, Any>().apply {
                    put("name", "unknown.png")
                    put("width", 720)
                    put("height", 1080)
                })), getProperty("enlargeButton", false))
                "video" -> {
                    val info = getProperty("videoInfo", HashMap<String, Any>())
                    createVideo(context, info["provider"] as String, info["id"] as String, info["thumbnail"] as String, info["title"] as String)
                }
                "title" -> createTitle(context, getProperty("title", ""), getProperty("alignment", "center"))
                "text" -> createText(context, getProperty("text", ""), getProperty("alignment", "left"))
                "button" -> createButton(context, getProperty("_buttonInfo", HashMap()))
                "separator" -> createSeparator(context, getProperty("visible", true))
                else -> createText(context, String.format("Invalid element type: %s; ID: %s; parent: %s", type, this@AuthenticElement.id, this@AuthenticElement.parent), "left", Color.RED)
            }
}