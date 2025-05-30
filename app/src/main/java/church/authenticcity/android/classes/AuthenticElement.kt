package church.authenticcity.android.classes

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.core.content.ContextCompat
import church.authenticcity.android.R
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.views.LargeThumbnailButtonView
import church.authenticcity.android.views.LoadingIndicatorImageView
import church.authenticcity.android.views.ThumbnailButtonView
import church.authenticcity.android.views.ToolbarView
import church.authenticcity.android.views.recyclerView.Tile
import church.authenticcity.android.views.recyclerView.TileViewHolder
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt


/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 2/1/2018 at 8:18 PM.
 * Licensed under the MIT License.
 */

class AuthenticElement(private val map: HashMap<String, Any>) {
    companion object {
        fun createImage(context: Context, image: ImageResource, enlargable: Boolean): View {
            return RelativeLayout(context).apply {
                val rand = Random().nextInt(256)
                val adjustedWidth = context.resources.displayMetrics.widthPixels
                val adjustedHeight = image.calculateHeight(adjustedWidth)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setBackgroundColor(Color.argb(255, rand, rand, rand))
                addView(ProgressBar(context).apply {
                    val psize = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        20f,
                        context.resources.displayMetrics
                    ).roundToInt()
                    isIndeterminate = true
                    indeterminateTintList =
                        ColorStateList.valueOf(Color.argb(255, 255 - rand, 255 - rand, 255 - rand))
                    layoutParams = RelativeLayout.LayoutParams(psize, psize).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_TOP)
                        addRule(RelativeLayout.ALIGN_PARENT_START)
                        val margin = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            4f,
                            context.resources.displayMetrics
                        ).roundToInt()
                        setMargins(margin, margin, 0, 0)
                    }
                })
                val imageView = ImageView(context).apply {
                    id = R.id.image
                    //Utils.loadFirebaseImage(context, image.imageName, this)
                    image.load(context, this)
                }
                if (enlargable) {
                    addView(LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            addRule(RelativeLayout.ALIGN_PARENT_TOP)
                            addRule(RelativeLayout.ALIGN_PARENT_START)
                        }
                        imageView.apply {
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                adjustedHeight
                            )
                            setOnClickListener {
                                image.saveToGallery(context)
                            }
                        }
                        addView(
                            createText(
                                context,
                                "Tap to download.",
                                "left",
                                color = Color.DKGRAY,
                                size = 14f
                            )
                        )
                        addView(imageView)
                    })
                } else
                    addView(imageView.apply {
                        layoutParams = RelativeLayout.LayoutParams(adjustedWidth, adjustedHeight)
                    })
            }
        }

        fun createVideo(context: Context, provider: String, id: String, thumbnail: String, title: String, large: Boolean, hideTitle: Boolean) =
                if (large) LargeThumbnailButtonView(context, provider, id, title, thumbnail, hideTitle) else ThumbnailButtonView(context, provider, id, title, thumbnail)
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

        fun createThumbnailButton(context: Context, action: ButtonAction, text: String, resource: ImageResource, large: Boolean, hideTitle: Boolean) = if (large) LargeThumbnailButtonView(context, text, resource, action, hideTitle) else ThumbnailButtonView(context, text, resource, action)

        fun createThumbnailButton(context: Context, info: HashMap<String, Any>, resource: HashMap<String, Any>, large: Boolean, hideTitle: Boolean) = createThumbnailButton(context, ButtonAction(info["action"] as HashMap<String, Any>), info["label"] as String, ImageResource(resource), large, hideTitle)

        private fun createTile(context: Context, title: String, height: Int, action: ButtonAction, resource: ImageResource): View {
            val usedHeight = if (height == 0) resource.calculateHeight(context, true) else height
            val h = if (height == 0) usedHeight else TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, usedHeight.toFloat(), context.resources.displayMetrics).roundToInt()
            val viewGroup = LinearLayout(context).apply { layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, usedHeight) }
            val viewHolder = TileViewHolder.getInstance(context, false, h, viewGroup, false)
            viewHolder.bind(Tile(title, false, resource, action) { a -> a.invoke(context) })
            return LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(viewHolder.itemView)
                addView(View(context).apply {
                    setBackgroundColor(Color.WHITE)
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, context.resources.displayMetrics).roundToInt())
                })
            }
        }

        fun createTile(context: Context, height: Int, tile: Tile<*>): View {
            val finalHeight = if (height == 0) tile.header.calculateHeight(context.resources.displayMetrics.widthPixels / 2) else height
            val viewGroup = LinearLayout(context).apply { layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, finalHeight) }
            val viewHolder = TileViewHolder.getInstance(context, false, finalHeight, viewGroup, false)
            viewHolder.bind(tile)
            return viewHolder.itemView
        }

        fun createTile(context: Context, title: String, height: Int, action: HashMap<String, Any>, resource: HashMap<String, Any>) = createTile(context, title, height, ButtonAction(action), ImageResource(resource))

        fun createFullExperienceController(context: Context, image: ImageResource, action: ButtonAction) =
                LoadingIndicatorImageView.create(context, image, LoadingIndicatorImageView.ANCHOR_CENTER, ImageView.ScaleType.CENTER_CROP).apply {
                    isClickable = true
                    setOnClickListener { action.invoke(context) }
                    if (Utils.checkSdk(23))
                        foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
                }
                /*ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    isClickable = true
                    setBackgroundColor(Color.BLACK)
                    //Utils.loadFirebaseImage(context, image.imageName, this)
                    image.load(context, true, this)
                    setOnClickListener { action.invoke(context) }
                    if (Utils.checkSdk(23))
                        foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
                }*/

        fun createSeparator(context: Context, visible: Boolean) =
                LinearLayout(context).apply {
                    visibility = if (visible) View.VISIBLE else View.INVISIBLE
                    setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackground))
                    layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.resources.displayMetrics).toInt()).apply {
                        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                        setMargins(px, px, px, px)
                    }
                }

        private fun createToolbar(context: Context, image: ImageResource, leftAction: ButtonAction, rightAction: ButtonAction) = ToolbarView(context, image, leftAction, rightAction)

        fun createToolbar(context: Context, image: HashMap<String, Any>, leftAction: HashMap<String, Any>, rightAction: HashMap<String, Any>) = createToolbar(context, ImageResource(image), ButtonAction(leftAction), ButtonAction(rightAction))

        fun createHtmlReader(context: Context, htmlString: String): WebView {
            val webView = WebView(context)
            webView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val html = "<!DOCTYPE html><html><head><style type=\"text/css\">@font-face {\nfont-family: Alpenglow;\nsrc: url(\"file:///android_asset/fonts/alpenglow_expanded.ttf\");\n}\nbody {\nfont-family: Alpenglow; \n}</style></head><body>$htmlString</body></html>"
            webView.loadDataWithBaseURL("", html, "text/html", "utf-8", null)
            webView.settings.defaultFontSize = 20
            return webView
        }
    }

    val id: String = map["id"] as String
    private val parent: String = map["parent"] as String
    val type: String = map["type"] as String

    @Suppress("UNCHECKED_CAST")
    fun <T> getProperty(key: String, default: T) = if (map.containsKey(key)) map[key] as T else default

    private fun getBoolean(key: String, default: Boolean) = try {
            getProperty(key, default.toString()).toBoolean()
        } catch (a: ClassCastException) {
            getProperty(key, default)
        }

    fun toView(context: Context) =
            when (type) {
                "image" -> createImage(context, ImageResource(getProperty("image", HashMap<String, Any>().apply {
                    put("name", "unknown.png")
                    put("width", 720)
                    put("height", 1080)
                })), getBoolean("enlargeButton", false))
                "video" -> {
                    val info = getProperty("videoInfo", HashMap<String, Any>())
                    createVideo(context, info["provider"] as String, info["id"] as String, info["thumbnail"] as String, info["title"] as String, getBoolean("large", false), getBoolean("hideTitle", false))
                }
                "title" -> createTitle(context, getProperty("title", ""), getProperty("alignment", "center"))
                "text" -> createText(context, getProperty("text", ""), getProperty("alignment", "left"))
                "button" -> createButton(context, getProperty("_buttonInfo", HashMap()))
                "thumbnailButton" -> createThumbnailButton(context, getProperty("_buttonInfo", HashMap()), getProperty("thumbnail", HashMap<String, Any>().apply {
                    put("name", "unknown.png")
                    put("width", 720)
                    put("height", 1080)
                }), getBoolean("large", false), getBoolean("hideTitle", false))
                "toolbar" -> createToolbar(context, getProperty("image", HashMap()), getProperty("leftAction", HashMap()), getProperty("rightAction", HashMap()))
                "tile" -> createTile(context, getProperty("title", ""), getProperty("height", 0), getProperty("action", HashMap()), getProperty("header", HashMap()))
                "fullExpController" -> createFullExperienceController(context, ImageResource(getProperty("image", HashMap())), ButtonAction(getProperty("action", HashMap())))
                "separator" -> createSeparator(context, getBoolean("visible", true))
                "html" -> createHtmlReader(context, getProperty("html", "<p></p>"))
                else -> createText(context, String.format("Invalid element type: %s; ID: %s; parent: %s", type, this@AuthenticElement.id, this@AuthenticElement.parent), "left", Color.RED)
            }
}