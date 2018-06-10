package church.authenticcity.android.views.recyclerView

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import church.authenticcity.android.R
import church.authenticcity.android.helpers.Utils
import kotlinx.android.synthetic.main.view_tile.view.*
import kotlin.math.roundToInt

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 6/8/2018 at 8:27 PM.
 * Licensed under the MIT License.
 */
class TileViewHolder(private val activity: Activity, viewGroup: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(activity).inflate(R.layout.view_tile, viewGroup, false)) {
    init {
        itemView.setBackgroundColor(Color.BLACK)
    }

    private val LOGGING = false

    private fun applyDrawable(drawable: Drawable, name: String) {
        Handler().postDelayed({
            val bitmap = drawable as BitmapDrawable
            val adjustedWidth = activity.resources.displayMetrics.widthPixels / 2
            val ratio = bitmap.bitmap.width.toFloat() / bitmap.bitmap.height.toFloat()
            val adjustedHeight = (adjustedWidth / ratio).roundToInt()
            if (LOGGING) {
                var log = "Applying $name:\n"
                log += " - requested width: $adjustedWidth\n"
                log += " - image aspect ratio: $ratio (${if (ratio > 1f) "landscape" else if (ratio == 1f) "square" else "landscape"})\n"
                log += " - calculated height: $adjustedHeight\n"
                log += " - text view height: ${itemView.tile_title.height}\n"
                if (itemView.tile_title.height > adjustedHeight)
                    log += "    - final height will be adjusted to accommodate the text view"
                Log.v("applyDrawable", log)
            }
            itemView.tile_image.setImageDrawable(drawable)
            itemView.tile_image.layoutParams.height = if (itemView.tile_title.height > adjustedHeight) itemView.tile_title.height + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, activity.resources.displayMetrics).roundToInt() else adjustedHeight
        }, 200L)
    }

    fun <T> initialize(tile: Tile<T>) {
        Utils.loadFirebaseImage(activity, tile.header, itemView.tile_image, { drawable -> applyDrawable(drawable, tile.header) })
        itemView.tile_title.text = tile.title
        itemView.tile_title.typeface = Utils.getTitleTypeface(activity)
        itemView.setOnClickListener {
            tile.handler(tile.argument)
        }
    }
}