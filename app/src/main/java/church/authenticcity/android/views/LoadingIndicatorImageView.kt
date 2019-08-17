package church.authenticcity.android.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Size
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.annotation.IntDef
import church.authenticcity.android.classes.ImageResource
import kotlin.math.roundToInt
import kotlin.random.Random

class LoadingIndicatorImageView {
    companion object{
        @IntDef(ANCHOR_TOP_LEFT, ANCHOR_CENTER)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Anchor
        const val ANCHOR_TOP_LEFT = 0
        const val ANCHOR_CENTER = 1

        @IntDef(MATCH_PARENT, WRAP_CONTENT)
        @Retention(AnnotationRetention.SOURCE)
        annotation class AutoSize
        const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
        const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT

        fun create(context: Context, resource: ImageResource, @Anchor anchor: Int, scaleType: ImageView.ScaleType, randomColor: Boolean = true, @AutoSize width: Int = -1, @AutoSize height: Int = -1): RelativeLayout {
            return RelativeLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(if (width == -1) resource.width else width, if (height == -1) resource.height else height)
                val rand = if (randomColor) Random.nextInt(0, 256) else 0
                setBackgroundColor(Color.argb(if (randomColor) 255 else 0, rand, rand, rand))
                addView(ProgressBar(context).apply {
                    isIndeterminate = true
                    indeterminateTintList = if (randomColor)
                        ColorStateList.valueOf(Color.argb(if (randomColor) 255 else 0, 255 - rand, 255 - rand, 255 - rand))
                    else
                        ColorStateList.valueOf(Color.WHITE)
                    val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics).roundToInt()
                    layoutParams = RelativeLayout.LayoutParams(size, size).apply {
                        if (anchor == ANCHOR_TOP_LEFT) {
                            val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).roundToInt()
                            setMargins(margin, margin, 0, 0)
                            addRule(RelativeLayout.ALIGN_PARENT_START)
                            addRule(RelativeLayout.ALIGN_PARENT_TOP)
                        } else
                            addRule(RelativeLayout.CENTER_IN_PARENT)
                    }
                })
                addView(ImageView(context).apply {
                    layoutParams = RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    setScaleType(scaleType)
                    resource.load(context, this)
                })
            }
        }

        fun create(context: Context, url: String, size: Size, @Anchor anchor: Int, scaleType: ImageView.ScaleType, randomColor: Boolean = true, @AutoSize width: Int = -1, @AutoSize height: Int = -1) = create(context, ImageResource(url, size.width, size.height), anchor, scaleType, randomColor, width, height)
    }
}