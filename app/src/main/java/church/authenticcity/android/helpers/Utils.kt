package church.authenticcity.android.helpers

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import church.authenticcity.android.AuthenticApplication
import church.authenticcity.android.BuildConfig
import church.authenticcity.android.R
import church.authenticcity.android.classes.*
import com.crashlytics.android.Crashlytics
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
        a.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)?.typeface = Utils.getTitleTypeface(a.context)
    }
    return this
}

fun ActionBar.applyTypeface(context: Context, text: String) {
    this.title = Utils.makeTypefaceSpan(text, Utils.getTitleTypeface(context))
}

@SuppressLint("ClickableViewAccessibility")
fun ScrollView.setScrollingEnabled(enabled: Boolean) {
    when (enabled) {
        false -> this.setOnTouchListener { v, event -> true }
        true -> this.setOnTouchListener(null)
    }
}

@Suppress("IMPLICIT_CAST_TO_ANY")
inline fun <reified T, V, K> HashMap<V, K>.getAs(key: V) = (if (T::class.simpleName == Int::class.simpleName) this[key].toString().toInt() else if (T::class.simpleName == Float::class.simpleName) this[key].toString().toFloat() else this[key]) as T

@Suppress("IMPLICIT_CAST_TO_ANY")
inline fun <reified T, V, K> HashMap<V, K>.getAs(key: V, default: T) = try {
    this.getAs<T, V, K>(key)
} catch (e: Exception) {
    default
}

open class SimpleAnimatorListener : Animator.AnimatorListener {
    override fun onAnimationRepeat(animator: Animator?) {
        // nothing
    }

    override fun onAnimationEnd(animator: Animator?) {
        // nothing
    }

    override fun onAnimationCancel(animator: Animator?) {
        // nothing
    }

    override fun onAnimationStart(p0: Animator?) {
        // nothing
    }
}

class Utils {
    class Temp {
        companion object {
            private var objects: HashMap<String, Any> = HashMap()
            private var devObjects: HashMap<String, Any> = HashMap()

            var map
                get() = if (AuthenticApplication.useDevelopmentDatabase) devObjects else objects
                set(value) = if (AuthenticApplication.useDevelopmentDatabase) devObjects = value else objects = value

            fun getTab(id: String): AuthenticTab? = if (map.containsKey(id)) map[id] as AuthenticTab else null

            fun getEvent(id: String): AuthenticEvent? = if (map.containsKey(id)) map[id] as AuthenticEvent else null

            fun putTab(tab: AuthenticTab) {
                map[tab.id] = tab
            }

            fun putEvent(event: AuthenticEvent) {
                map[event.id] = event
            }
        }
    }

    class Constructors {
        companion object {
            fun constructEvent(value: Any): AuthenticEvent? = try {
                val map = value as HashMap<String, Any>
                if (map.containsKey("index"))
                    AuthenticEventPlaceholder(map.getAs("id"), map.getAs("index"), map.getAs("title"), map.getAs("hideTitle", false), ImageResource(map.getAs("header")), map.getAs("elements", ArrayList()), if (map.containsKey("action")) ButtonAction(map.getAs("action")) else null, if (map.containsKey("visibility")) map.getAs<String, String, Any>("visibility") else null)
                else
                    AuthenticEvent(map.getAs("id"), map.getAs("title"), map.getAs("hideTitle"), map.getAs("description"), ImageResource(map.getAs("header")), map.getAs("dateTime"), map.getAs("hideEndDate"), map.getAs("recurrence"), map.getAs("location"), map.getAs("address"), map.getAs("registration"))
            } catch (e: Exception) {
                Crashlytics.logException(e)
                e.printStackTrace()
                null
            }

            fun constructTab(value: Any): AuthenticTab? = try {
                val map = value as HashMap<String, Any>
                AuthenticTab(ImageResource(map.getAs("header")), map.getAs("id"), map.getAs("index"), map.getAs("title"), if (map.containsKey("action")) map.getAs<HashMap<String, Any>, String, Any>("action") else null, if (map.containsKey("elements")) map.getAs<List<HashMap<String, Any>>, String, Any>("elements") else null, map.getAs("visibility"), map.getAs<String?, String, Any>("specialType", null))
            } catch (e: Exception) {
                Crashlytics.logException(e)
                e.printStackTrace()
                null
            }
        }
    }

    companion object {
        val datePattern
            get() = DateTimeFormatter.ofPattern("EEEE, MMMM d, y")!!
        val timePattern
            get() = DateTimeFormatter.ofPattern("h:mm a")!!

        private var title: Typeface? = null
        private var titleb: Typeface? = null
        private var text: Typeface? = null

        fun reportAndAlertException(context: Context, ex: Exception, location: String) {
            Crashlytics.logException(ex)
            var msg = "Unfortunately, an unexpected error occurred and has been reported.  We apologize for the inconvenience.  If you need them, here are the details:\n"
            msg += "\nLocation: $location"
            msg += "\nType: ${ex.javaClass.canonicalName ?: "<null>"}"
            msg += "\nMessage: ${ex.message ?: "<null>"}"
            AlertDialog.Builder(context).setTitle("Unexpected Error").setMessage(msg).setPositiveButton("Dismiss", null).create().applyColorsAndTypefaces().show()
        }

        @SuppressLint("ShowToast")
        fun makeToast(context: Context, text: String, length: Int) = Toast.makeText(context, makeTypefaceSpan(text, getTextTypeface(context)), length)!!

        fun makeTypefaceSpan(text: String, typeface: Typeface): SpannableString {
            val span = SpannableString(text)
            span.setSpan(TypefaceSpan(typeface), 0, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return span
        }

        fun getTitleTypeface(context: Context, bold: Boolean = false): Typeface {
            if (bold) {
                if (titleb == null)
                    titleb = ResourcesCompat.getFont(context, R.font.effra_bold)
                return titleb!!
            }
            if (title == null)
                title = ResourcesCompat.getFont(context, R.font.effra)
            return title!!
        }

        fun getTextTypeface(context: Context): Typeface {
            if (text == null)
                text = ResourcesCompat.getFont(context, R.font.proxima_nova)
            return text!!
        }

        fun isUpdateAvailable(latestCode: Int) = BuildConfig.VERSION_CODE < latestCode

        fun checkSdk(apiLevel: Int) = Build.VERSION.SDK_INT >= apiLevel

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