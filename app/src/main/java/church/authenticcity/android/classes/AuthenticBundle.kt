package church.authenticcity.android.classes

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import church.authenticcity.android.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.storage.FirebaseStorage

class AuthenticBundle(val id: String = "", val parentId: String = "", val index: Int = -1, val image: String = "", val title: String = "", val text: String = "", val _buttonInfo: HashMap<String, Any>? = null) {
    val buttonLabel: String
        get() = _buttonInfo?.get("label")?.toString() ?: ""
    val buttonAction: ButtonAction?
        get() = if (_buttonInfo != null) ButtonAction(_buttonInfo["action"] as HashMap<String, Any>) else null

    val hasButton: Boolean
            get() = buttonLabel != "" && buttonAction != null

    fun toView(context: Context): LinearLayout {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT).apply {
                val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).toInt()
                setMargins(0, px, 0, px)
            }
        }
        if (image != "")
            layout.addView(ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                Glide.with(context).load(FirebaseStorage.getInstance().reference.child(image)).transition(DrawableTransitionOptions.withCrossFade()).into(this)
            })
        if (title != "")
            layout.addView(TextView(context).apply {
                text = title
                setTextColor(Color.BLACK)
                layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT).apply {
                    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).toInt()
                    setMargins(px, 0, px, 0)
                }
                textSize = context.resources.getDimension(R.dimen.titleSize)
            })
        if (text != "")
            layout.addView(TextView(context).apply {
                text = text
                setTextColor(Color.DKGRAY)
                layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT).apply {
                    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).toInt()
                    setMargins(px, 0, px, 0)
                }
                textSize = context.resources.getDimension(R.dimen.textSize)
            })
        if (hasButton)
            layout.addView(Button(context).apply {
                text = buttonLabel
                setOnClickListener { buttonAction?.invoke(context) }
            })
        return layout
    }
}