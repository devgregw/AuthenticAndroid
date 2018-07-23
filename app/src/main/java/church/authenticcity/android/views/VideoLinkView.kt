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
import church.authenticcity.android.R
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.helpers.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.video_link_view.view.*
import kotlin.math.roundToInt

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 7/22/2018 at 3:01 PM.
 * Licensed under the MIT License.
 */
class VideoLinkView(context: Context, provider: String, id: String, thumbnail: String, title: String) : RelativeLayout(context) {
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.video_link_view, this, true)
        view.video_title.text = title
        if (Utils.checkSdk(23))
        view.foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
        Glide.with(context).load(Uri.parse(thumbnail)).transition(DrawableTransitionOptions.withCrossFade()).into(view.video_thumbnail)
        isClickable = true
        setOnClickListener {
            ButtonAction.openUrl(if (provider == "YouTube") "https://youtube.com/watch?v=$id" else "https://vimeo.com/$id").invoke(context)
        }
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, context.resources.displayMetrics).roundToInt())
    }
}