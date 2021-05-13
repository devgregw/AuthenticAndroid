package church.authenticcity.android.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.widget.RelativeLayout
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.classes.ImageResource
import church.authenticcity.android.databinding.ViewToolbarBinding
import church.authenticcity.android.helpers.Utils

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 10/6/2018 at 3:42 PM.
 * Licensed under the MIT License.
 */
@SuppressLint("ViewConstructor")
class ToolbarView(context: Context, image: ImageResource, private val leftAction: ButtonAction, private val rightAction: ButtonAction) : RelativeLayout(context) {
    init {
        val binding = ViewToolbarBinding.inflate(LayoutInflater.from(context), this, true)
        val leftView = binding.toolbarLeft
        val rightView = binding.toolbarRight
        val imageView = binding.toolbarImage
        if (Utils.checkSdk(23)) {
            leftView.foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
            rightView.foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
        }
        leftView.isClickable = true
        leftView.setOnClickListener { leftAction.invoke(context) }
        rightView.isClickable = true
        rightView.setOnClickListener { rightAction.invoke(context) }
        val adjustedHeight = image.calculateHeight(context, true)
        imageView.layoutParams.height = adjustedHeight
        binding.toolbarButtons.layoutParams.height = adjustedHeight
        //Utils.loadFirebaseImage(context, image.imageName, imageView)
        image.load(context, imageView)
    }
}