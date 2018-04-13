package church.authenticcity.android.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Build
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.text.Spannable
import android.text.SpannableString
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.*
import church.authenticcity.android.BuildConfig
import church.authenticcity.android.R
import church.authenticcity.android.classes.AuthenticEvent
import church.authenticcity.android.classes.AuthenticTab
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.storage.FirebaseStorage
import org.threeten.bp.format.DateTimeFormatter

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
    setOnShowListener { p0 ->
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
    return this
}

fun ActionBar.applyTypeface(context: Context, text: String) {
    this.title = Utils.makeTypefaceSpan(text, Utils.getTitleTypeface(context))
}

inline fun <reified T, V, K> HashMap<V, K>.getAs(key: V) = (if (T::class.simpleName == Int::class.simpleName) this[key].toString().toInt() else if (T::class.simpleName == Float::class.simpleName) this[key].toString().toFloat() else this[key]) as T

class Utils {
    class Temp {
        companion object {
            private var objects: HashMap<String, Any> = HashMap()

            fun getTab(id: String): AuthenticTab? = if (objects.containsKey(id)) objects[id] as AuthenticTab else null

            fun getEvent(id: String): AuthenticEvent? = if (objects.containsKey(id)) objects[id] as AuthenticEvent else null

            fun putTab(tab: AuthenticTab) {
                objects[tab.id] = tab
            }

            fun putEvent(event: AuthenticEvent) {
                objects[event.id] = event
            }
        }
    }

    class Constructors {
        companion object {
            fun constructEvent(value: Any): AuthenticEvent {
                val map = value as HashMap<String, Any>
                return AuthenticEvent(map.getAs("id"), map.getAs("title"), map.getAs("hideTitle"), map.getAs("description"), map.getAs("header"), map.getAs("dateTime"), map.getAs("recurrence"), map.getAs("location"), map.getAs("address"), map.getAs("registration"))
            }

            fun constructTab(value: Any): AuthenticTab {
                val map = value as HashMap<String, Any>
                return AuthenticTab(map.getAs("header"), map.getAs("id"), map.getAs("index"), map.getAs("hideTitle"), map.getAs("hideHeader"), map.getAs("title"), map.getAs("elements"), map.getAs("visibility"))
            }
        }
    }

    companion object {
        val datePattern
            get() = DateTimeFormatter.ofPattern("EEEE, MMMM d, y")!!
        val timePattern
            get() = DateTimeFormatter.ofPattern("h:mm a")!!

        private var title: Typeface? = null
        private var text: Typeface? = null

        fun reportAndAlertException(context: Context, ex: Exception, location: String) {
            FirebaseCrash.report(ex)
            var msg = "Unfortunately, an unexpected error occurred and has been reported.  We apologize for the inconvenience.  If you need them, here are the details:\n"
            msg += "\nLocation: $location"
            msg += "\nType: ${ex.javaClass.canonicalName ?: "<null>"}"
            msg+= "\nMessage: ${ex.message ?: "<null>"}"
            AlertDialog.Builder(context).setTitle("Unexpected Error").setMessage(msg).setPositiveButton("Dismiss", null).create().applyColorsAndTypefaces().show()
        }

        @SuppressLint("ShowToast")
        fun makeToast(context: Context, text: String, length: Int) = Toast.makeText(context, makeTypefaceSpan(text, Utils.getTextTypeface(context)), length)!!

        fun makeTypefaceSpan(text: String, typeface: Typeface): SpannableString {
            val span = SpannableString(text)
            span.setSpan(TypefaceSpan(typeface), 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return span
        }

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

        fun checkSdk(apiLevel: Int): Boolean = Build.VERSION.SDK_INT >= apiLevel

        fun createIndeterminateDialog(context: Context, message: String): AlertDialog {
            return AlertDialog.Builder(context).setView(RelativeLayout(context).apply {
                setBackgroundColor(Color.parseColor("#212121"))
                val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
                setPadding(padding, padding, padding, padding)
                addView(ProgressBar(context).apply {
                    isIndeterminate = true
                    val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics).toInt()
                    id = R.id.content_list
                    layoutParams = RelativeLayout.LayoutParams(size, size).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                        addRule(RelativeLayout.CENTER_VERTICAL)
                    }
                    indeterminateDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                })
                addView(TextView(context).apply {
                    text = message
                    typeface = getTextTypeface(context)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        setMargins(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt(), 0, 0, 0)
                        addRule(RelativeLayout.RIGHT_OF, R.id.content_list)
                        addRule(RelativeLayout.CENTER_VERTICAL)
                    }
                    setTextColor(Color.WHITE)
                })
            }).setCancelable(false).create().applyColorsAndTypefaces()
        }
    }
}