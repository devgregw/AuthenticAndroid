package church.authenticcity.android.views.recyclerView

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import church.authenticcity.android.R
import church.authenticcity.android.classes.ImageResource
import church.authenticcity.android.helpers.Utils
import kotlinx.android.synthetic.main.view_tile.view.*
import java.util.*
import kotlin.math.roundToInt

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 6/8/2018 at 8:27 PM.
 * Licensed under the MIT License.
 */
class TileViewHolder(private val context: Context, private val fullWidth: Boolean, private val height: Int?, viewGroup: ViewGroup, backgroundColor: Boolean = true) : RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_tile, viewGroup, false)) {
    init {
        if (backgroundColor) {
            val rand = Random().nextInt(256)
            itemView.setBackgroundColor(Color.argb(255, rand, rand, rand))
        }
    }

    private val logging = false

    private fun setHeight(header: ImageResource, override: Int?) {
        if (height != null) {
            itemView.tile_image.layoutParams.height = height
            return
        }
        if (override != null) {
            itemView.tile_image.layoutParams.height = override
            return
        }
        val adjustedWidth = context.resources.displayMetrics.widthPixels / (if (fullWidth) 1 else 2)
        val ratio = header.width.toFloat() / (if (header.height == 0) 1 else header.height).toFloat()
        val adjustedHeight = (adjustedWidth / ratio).roundToInt()
        if (logging) {
            var log = "Applying ${header.imageName}:\n"
            log += " - requested width: $adjustedWidth\n"
            log += " - image aspect ratio: $ratio (${if (ratio > 1f) "landscape" else if (ratio == 1f) "square" else "landscape"})\n"
            log += " - calculated height: $adjustedHeight\n"
            log += " - text view height: ${itemView.tile_title.height}\n"
            if (itemView.tile_title.height > adjustedHeight)
                log += "    - final height will be adjusted to accommodate the text view"
            Log.v("applyDrawable", log)
        }
        itemView.tile_image.layoutParams.height = if (itemView.tile_title.height > adjustedHeight) itemView.tile_title.height + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, context.resources.displayMetrics).roundToInt() else adjustedHeight
    }

    fun <T> initialize(tile: Tile<T>) {
        setHeight(tile.header, tile.heightOverride)
        Utils.loadFirebaseImage(context, tile.header.imageName, itemView.tile_image)
        if (Utils.checkSdk(23))
            itemView.foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
        itemView.tile_image.imageTintList = if (tile.hideTitle || tile.title == "") ColorStateList.valueOf(Color.TRANSPARENT) else ColorStateList.valueOf(Color.argb(128, 0, 0, 0))
        itemView.tile_title.text = tile.title
        itemView.tile_title.visibility = if (tile.hideTitle) View.INVISIBLE else View.VISIBLE
        itemView.tile_title.typeface = Utils.getTitleTypeface(context, true)
        itemView.setOnClickListener {
            tile.handler(tile.argument)
        }
    }
}