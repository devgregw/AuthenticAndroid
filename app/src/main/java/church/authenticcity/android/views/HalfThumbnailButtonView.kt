package church.authenticcity.android.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import church.authenticcity.android.R
import church.authenticcity.android.activities.VideoActivity
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.classes.ImageResource
import church.authenticcity.android.databinding.ThumbnailButtonHalfViewBinding
import church.authenticcity.android.helpers.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlin.math.roundToInt
import kotlin.random.Random

class HalfThumbnailButtonView private constructor(context: Context) : RelativeLayout(context) {
    private var binding = ThumbnailButtonHalfViewBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context, title: String, resource: ImageResource, action: ButtonAction, hideTitle: Boolean) : this(context) {
        initialize(resource)
        binding.title.text = title
        if (hideTitle)
            binding.labelBackground.visibility = View.GONE
        resource.load(context, binding.thumbnail)
        setOnClickListener {
            action.invoke(context)
        }
    }

    constructor(context: Context, provider: String, videoId: String, title: String, thumbnail: String, hideTitle: Boolean) : this(context) {
        val resource = ImageResource("", 1920, 1080)
        initialize(resource)
        binding.title.text = title
        if (hideTitle)
            binding.labelBackground.visibility = View.GONE
        Glide.with(context).load(Uri.parse(thumbnail)).error(R.drawable.unknown).transition(DrawableTransitionOptions.withCrossFade()).into(binding.thumbnail)
        setOnClickListener {
            VideoActivity.start(context, provider, videoId, title)
        }
    }

    private fun initialize(resource: ImageResource) {
        val rand = Random.nextInt(0, 256)
        binding.thumbnailContainer.setBackgroundColor(Color.argb(255, rand, rand, rand))
        binding.progressBar.indeterminateTintList = ColorStateList.valueOf(Color.argb(255, 255 - rand, 255 - rand, 255 - rand))
        if (Utils.checkSdk(23))
            binding.card.foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
        isClickable = true
        val widthMargin = 2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).roundToInt()
        binding.thumbnail.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, resource.calculateHeight((resources.displayMetrics.widthPixels / 2) - widthMargin))
    }
}