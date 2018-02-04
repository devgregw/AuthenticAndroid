package church.authenticcity.android.helpers

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.text.Spannable
import android.text.SpannableString
import android.widget.ImageView
import android.widget.TextView
import church.authenticcity.android.BuildConfig
import church.authenticcity.android.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.storage.FirebaseStorage

fun String.Companion.isNullOrWhiteSpace(string: String?): Boolean {
    if (string != null) {
        var i = 0
        while (i < string.length) {
            if (!string[i].isWhitespace())
                return false
            ++i
        }
    }
    return true
}

fun AlertDialog.applyColorsAndTypefaces(): AlertDialog {
    setOnShowListener(object : DialogInterface.OnShowListener {
        override fun onShow(p0: DialogInterface?) {
            val a = p0 as AlertDialog
            a.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                setTextColor(Color.WHITE)
                typeface = Utils.getTextTypeface(a.context)
            }
            a.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                setTextColor(Color.WHITE)
                typeface = Utils.getTextTypeface(a.context)
            }
            a.getButton(AlertDialog.BUTTON_NEUTRAL).apply {
                setTextColor(Color.WHITE)
                typeface = Utils.getTextTypeface(a.context)
            }
            a.findViewById<TextView>(android.R.id.message)?.typeface = Utils.getTextTypeface(a.context)
            a.findViewById<TextView>(android.support.v7.appcompat.R.id.alertTitle)?.typeface = Utils.getTitleTypeface(a.context)
        }
    })
    return this
}

fun ActionBar.applyTypeface(context: Context, text: String) {
    val span = SpannableString(text)
    span.setSpan(TypefaceSpan(Utils.getTitleTypeface(context)), 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    this.title = span
}

class Utils {
    companion object {
        private var title: Typeface? = null
        private var text: Typeface? = null

        fun getTitleTypeface(context: Context): Typeface {
            if (title == null)
                title = ResourcesCompat.getFont(context, R.font.title)
            return title!!
        }

        fun getTextTypeface(context: Context): Typeface {
            if (text == null)
                text = ResourcesCompat.getFont(context, R.font.text)
            return text!!
        }

        fun loadFirebaseImage(context: Context, name: String, view: ImageView) {
            Glide.with(context).load(FirebaseStorage.getInstance().reference.child(name)).transition(DrawableTransitionOptions.withCrossFade()).into(view)
        }

        fun isUpdateAvailable(latestCode: Int) = BuildConfig.VERSION_CODE < latestCode

        fun showErrorDialog(activity: Activity, code: Int, message: String, details: String) {
            AlertDialog.Builder(activity).setCancelable(false).setTitle("Error").setMessage(String.format("Unfortunately, an error occurred.\nCode: %s\nMessage: %s\nDetails: %s", code, message, details)).setPositiveButton("Close", { _, _ -> activity.finish() }).create().apply { setOnShowListener { dialog -> (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE) } }.show()
        }

        fun checkSdk(apiLevel: Int): Boolean = Build.VERSION.SDK_INT >= apiLevel
    }
}