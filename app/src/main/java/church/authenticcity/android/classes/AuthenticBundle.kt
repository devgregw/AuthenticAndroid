package church.authenticcity.android.classes

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
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

    fun toView(context: Context): RelativeLayout {
        val layout = RelativeLayout(context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT).apply {
                val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).toInt()
                setMargins(0, px, 0, px)
            }
        }
        var id = 10000
        if (image != "")
            layout.addView(ImageView(context).apply {
                this.id = id
                layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    if (id > 10000)
                        addRule(RelativeLayout.BELOW, id - 1)
                }
                Glide.with(context).load(FirebaseStorage.getInstance().reference.child(image)).transition(DrawableTransitionOptions.withCrossFade()).into(this)
                id++
            })
        if (title != "")
            layout.addView(TextView(context).apply {
                this.id = id
                this.text = title
                setTextColor(Color.WHITE)
                layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    if (id > 10000)
                        addRule(RelativeLayout.BELOW, id - 1)
                    addRule(RelativeLayout.CENTER_HORIZONTAL)
                }
                textSize = context.resources.getDimension(R.dimen.titleSize)
                id++
            })
        if (text != "")
            layout.addView(TextView(context).apply {
                this.id = id
                this.text = this@AuthenticBundle.text
                setTextColor(Color.LTGRAY)
                layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    if (id > 10000)
                        addRule(RelativeLayout.BELOW, id - 1)
                    addRule(RelativeLayout.CENTER_HORIZONTAL)
                }
                textSize = context.resources.getDimension(R.dimen.textSize)
                id++
            })
        if (hasButton)
            layout.addView(Button(context).apply {
                background.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
                setTextColor(Color.BLACK)
                this.id = id
                layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    if (id > 10000)
                        addRule(RelativeLayout.BELOW, id - 1)
                    addRule(RelativeLayout.CENTER_HORIZONTAL)
                }
                text = buttonLabel
                setOnClickListener { buttonAction?.invoke(context) }
            })
        return layout
    }
}