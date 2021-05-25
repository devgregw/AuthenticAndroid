package church.authenticcity.android.views.recyclerView

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import church.authenticcity.android.classes.ImageResource
import church.authenticcity.android.databinding.ViewTileBinding
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.isNullOrWhiteSpace
import com.bumptech.glide.Glide
import java.util.*
import kotlin.math.roundToInt

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 6/8/2018 at 8:27 PM.
 * Licensed under the MIT License.
 */
class TileViewHolder private constructor(
    private val context: Context,
    private val fullWidth: Boolean,
    private val height: Int?,
    setBackgroundColor: Boolean,
    private val binding: ViewTileBinding
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun getInstance(context: Context, fullWidth: Boolean, height: Int?, viewGroup: ViewGroup, setBackgroundColor: Boolean = true) = TileViewHolder(context, fullWidth, height, setBackgroundColor, ViewTileBinding.inflate(
            LayoutInflater.from(context), viewGroup, false))
    }

    private val tileImage
        get() = binding.tileImage
    private val tileTitle
        get() = binding.tileTitle
    private val progressBar
        get() = binding.progressBar

    private var currentTile: Tile<*>? = null

    private fun setHeight(header: ImageResource, override: Int?) {
        if (height != null) {
            tileImage.layoutParams.height = height
            return
        }
        if (override != null) {
            tileImage.layoutParams.height = override
            return
        }
        val adjustedHeight = header.calculateHeight(context, fullWidth)
        tileImage.layoutParams.height = if (tileTitle.height > adjustedHeight) tileTitle.height + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, context.resources.displayMetrics).roundToInt() else adjustedHeight
    }

    fun <T> bind(tile: Tile<T>) {
        currentTile = tile
        Glide.with(context).clear(tileImage)
        setHeight(tile.header, tile.heightOverride)
        if (tile.hideTitle || String.isNullOrWhiteSpace(tile.title)) {
            val rand = kotlin.random.Random.nextInt(0, 256)
            itemView.setBackgroundColor(Color.argb(255, rand, rand, rand))
            progressBar.indeterminateTintList = ColorStateList.valueOf(Color.argb(255, 255 - rand, 255 - rand, 255 - rand))
        } else
            progressBar.visibility = View.GONE

        tile.header.load(context, tileImage)

        tileImage.imageTintList = if (tile.hideTitle || tile.title == "") ColorStateList.valueOf(Color.TRANSPARENT) else ColorStateList.valueOf(Color.argb(128, 0, 0, 0))
        tileTitle.text = tile.title
        tileTitle.visibility = if (tile.hideTitle) View.INVISIBLE else View.VISIBLE
    }

    init {
        if (setBackgroundColor) {
            val rand = Random().nextInt(256)
            itemView.setBackgroundColor(Color.argb(255, rand, rand, rand))
        }
        tileTitle.typeface = Utils.getTitleTypeface(context)
        if (Utils.checkSdk(23))
            itemView.foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
        itemView.setOnClickListener {
            currentTile?.handle()
        }
    }
}