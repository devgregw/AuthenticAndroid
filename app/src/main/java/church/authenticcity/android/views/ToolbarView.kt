package church.authenticcity.android.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.widget.RelativeLayout
import church.authenticcity.android.R
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.classes.ImageResource
import church.authenticcity.android.helpers.Utils
import kotlinx.android.synthetic.main.view_toolbar.view.*
import kotlin.math.roundToInt

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 10/6/2018 at 3:42 PM.
 * Licensed under the MIT License.
 */
class ToolbarView(context: Context, private val image: ImageResource, private val leftAction: ButtonAction, private val rightAction: ButtonAction) : RelativeLayout(context) {
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_toolbar, this, true)
        val leftView = view.toolbar_left
        val rightView = view.toolbar_right
        val imageView = view.toolbar_image
        if (Utils.checkSdk(23)) {
            leftView.foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
            rightView.foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
        }
        leftView.isClickable = true
        leftView.setOnClickListener { leftAction.invoke(context) }
        rightView.isClickable = true
        rightView.setOnClickListener { rightAction.invoke(context) }
        val adjustedWidth = context.resources.displayMetrics.widthPixels
        val ratio = image.width.toFloat() / (if (image.height == 0) 1 else image.height).toFloat()
        val adjustedHeight = (adjustedWidth / ratio).roundToInt()
        imageView.layoutParams.height = adjustedHeight
        view.toolbar_buttons.layoutParams.height = adjustedHeight
        Utils.loadFirebaseImage(context, image.imageName, imageView)
    }
}