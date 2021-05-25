package church.authenticcity.android.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import church.authenticcity.android.activities.VideoActivity
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.classes.ImageResource
import church.authenticcity.android.databinding.ThumbnailButtonViewBinding
import church.authenticcity.android.helpers.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 7/22/2018 at 3:01 PM.
 * Licensed under the MIT License.
 */
class ThumbnailButtonView private constructor(context: Context) : RelativeLayout(context) {
    private val binding = ThumbnailButtonViewBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context, title: String, resource: ImageResource, action: ButtonAction) : this(context) {
        initialize()
        binding.title.text = title
        resource.load(context, binding.thumbnail)
        setOnClickListener {
            action.invoke(context)
        }
    }

    constructor(context: Context, provider: String, videoId: String, title: String, thumbnail: String) : this(context) {
        initialize()
        binding.title.text = title
        Glide.with(context).load(Uri.parse(thumbnail)).transition(DrawableTransitionOptions.withCrossFade()).into(binding.thumbnail)
        setOnClickListener {
            VideoActivity.start(context, provider, videoId, title)
        }
    }

    private fun initialize() {
        val rand = Random.nextInt(0, 256)
        binding.title.typeface = Utils.getTextTypeface(context)
        binding.thumbnailContainer.setBackgroundColor(Color.argb(255, rand, rand, rand))
        binding.progressBar.indeterminateTintList = ColorStateList.valueOf(Color.argb(255, 255 - rand, 255 - rand, 255 - rand))
        if (Utils.checkSdk(23))
            binding.root.foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
        isClickable = true
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, context.resources.displayMetrics).roundToInt())
    }
}