package church.authenticcity.android.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.DrawableCompat
import church.authenticcity.android.R
import church.authenticcity.android.VideoActivity
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.classes.ImageResource
import church.authenticcity.android.helpers.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.thumbnail_button_large_view.view.*
import kotlin.math.roundToInt
import kotlin.random.Random

class LargeThumbnailButtonView private constructor(context: Context) : RelativeLayout(context) {
    private val view: View = LayoutInflater.from(context).inflate(R.layout.thumbnail_button_large_view, this, true)

    constructor(context: Context, title: String, resource: ImageResource, action: ButtonAction, hideTitle: Boolean) : this(context) {
        initialize(resource)
        view.title.text = title
        if (hideTitle)
            view.label_background.visibility = View.GONE
        resource.load(context, view.thumbnail)
        setOnClickListener {
            action.invoke(context)
        }
    }

    constructor(context: Context, provider: String, videoId: String, title: String, thumbnail: String, hideTitle: Boolean) : this(context) {
        initialize(ImageResource("", 1920, 1080))
        view.title.text = title
        if (hideTitle)
            view.label_background.visibility = View.GONE
        Glide.with(context).load(Uri.parse(thumbnail)).transition(DrawableTransitionOptions.withCrossFade()).into(view.thumbnail)
        setOnClickListener {
            VideoActivity.start(context, provider, videoId, title)
        }
    }

    private fun initialize(resource: ImageResource) {
        val rand = Random.nextInt(0, 256)
        view.thumbnail_container.setBackgroundColor(Color.argb(255, rand, rand, rand))
        view.label_background.setBackgroundColor(Color.argb(208, 0, 0, 0))
        view.progress_bar.indeterminateTintList = ColorStateList.valueOf(Color.argb(255, 255 - rand, 255 - rand, 255 - rand))
        if (Utils.checkSdk(23))
            view.card.foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
        isClickable = true
        val widthMargin = 2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).roundToInt()
        view.thumbnail.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, resource.calculateHeight(resources.displayMetrics.widthPixels - widthMargin))
    }
}