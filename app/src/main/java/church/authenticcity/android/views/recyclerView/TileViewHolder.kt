package church.authenticcity.android.views.recyclerView

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import church.authenticcity.android.classes.ImageResource
import church.authenticcity.android.databinding.ViewTileBinding
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.isNullOrWhiteSpace
import java.util.*
import kotlin.math.roundToInt

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 6/8/2018 at 8:27 PM.
 * Licensed under the MIT License.
 */
class TileViewHolder(private val context: Context, private val fullWidth: Boolean, private val height: Int?, viewGroup: ViewGroup, backgroundColor: Boolean = true, private val binding: ViewTileBinding) : RecyclerView.ViewHolder(binding.root) {
    init {
        if (backgroundColor) {
            val rand = Random().nextInt(256)
            itemView.setBackgroundColor(Color.argb(255, rand, rand, rand))
        }
    }

    private fun setHeight(header: ImageResource, override: Int?) {
        if (height != null) {
            binding.tileImage.layoutParams.height = height
            return
        }
        if (override != null) {
            binding.tileImage.layoutParams.height = override
            return
        }
        val adjustedHeight = header.calculateHeight(context, fullWidth)
        binding.tileImage.layoutParams.height = if (binding.tileTitle.height > adjustedHeight) binding.tileTitle.height + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, context.resources.displayMetrics).roundToInt() else adjustedHeight
    }

    fun <T> initialize(tile: Tile<T>) {
        setHeight(tile.header, tile.heightOverride)
        if (tile.hideTitle || String.isNullOrWhiteSpace(tile.title)) {
            val rand = kotlin.random.Random.nextInt(0, 256)
            itemView.setBackgroundColor(Color.argb(255, rand, rand, rand))
            binding.progressBar.indeterminateTintList = ColorStateList.valueOf(Color.argb(255, 255 - rand, 255 - rand, 255 - rand))
        } else
            binding.progressBar.visibility = View.GONE
        //Utils.loadFirebaseImage(context, tile.header.imageName, itemView.tile_image)
        tile.header.load(context, binding.tileImage)
        if (Utils.checkSdk(23))
            itemView.foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
        binding.tileImage.imageTintList = if (tile.hideTitle || tile.title == "") ColorStateList.valueOf(Color.TRANSPARENT) else ColorStateList.valueOf(Color.argb(128, 0, 0, 0))
        binding.tileTitle.text = tile.title
        binding.tileTitle.visibility = if (tile.hideTitle) View.INVISIBLE else View.VISIBLE
        binding.tileTitle.typeface = Utils.getTitleTypeface(context)
        itemView.setOnClickListener {
            tile.handler(tile.argument)
        }
    }
}