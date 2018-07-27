package church.authenticcity.android

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import church.authenticcity.android.classes.AuthenticElement
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyTypeface
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.math.roundToInt

class AboutActivity : AppCompatActivity() {

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_basic)
        findViewById<Toolbar>(R.id.toolbar).apply {
            this@AboutActivity.setSupportActionBar(this)
            setBackgroundColor(Color.parseColor("#212121"))
        }
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left_white_36dp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.applyTypeface(this, "ABOUT")
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        findViewById<TextView>(R.id.toolbar_title).apply {
            typeface = Utils.getTitleTypeface(this@AboutActivity)
            text = "ABOUT"
        }
        findViewById<LinearLayout>(R.id.content_list).apply {
            val image = ImageView(this@AboutActivity)
            Glide.with(context).load(R.drawable.banner).transition(DrawableTransitionOptions.withCrossFade()).into(image)
            addView(image)
            addView(AuthenticElement.createTitle(this@AboutActivity, "AUTHENTIC CITY CHURCH", "center"))
            addView(AuthenticElement.createText(this@AboutActivity, "Version ${BuildConfig.VERSION_NAME} build ${BuildConfig.VERSION_CODE} for Android devices", "center"))
            addView(Button(context).apply {
                this.text = "Settings"
                val dimen = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).roundToInt()
                setPadding(dimen, dimen, dimen, dimen)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                typeface = Utils.getTextTypeface(context)
                setOnClickListener {
                    this@AboutActivity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${this@AboutActivity.packageName}")
                    }) }
                layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                    setMargins(px, 0, px, 0)
                }
            })
            addView(AuthenticElement.createSeparator(this@AboutActivity, true))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://github.com/devgregw/AuthenticAndroid"), "GitHub Repository"))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://trello.com/b/QUgekVh6"), "Trello Roadmap"))
            addView(AuthenticElement.createSeparator(this@AboutActivity, true))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://docs.accams.devgregw.com"), "Legal Documentation"))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://docs.accams.devgregw.com/privacy-policy"), "Privacy Policy"))
            OssLicensesMenuActivity.setActivityTitle("Licenses")
            addView(Button(context).apply {
                this.text = "Licenses"
                textSize = 18f
                val dimen = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).roundToInt()
                setPadding(dimen, dimen, dimen, dimen)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                typeface = Utils.getTextTypeface(context)
                setOnClickListener { this@AboutActivity.startActivity(Intent(this@AboutActivity, OssLicensesMenuActivity::class.java)) }
                layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                    setMargins(px, 0, px, 0)
                }
            })
            addView(AuthenticElement.createSeparator(this@AboutActivity, false))
            addView(CheckBox(this@AboutActivity).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                    setMargins(px, 0, px, 0)
                }
                text = Utils.makeTypefaceSpan("Development notifications", Utils.getTextTypeface(this@AboutActivity))
                textSize = 18f
                setTextColor(Color.BLACK)
                isChecked = this@AboutActivity.getSharedPreferences("private", 0).getBoolean("devNotifications", false)
                buttonTintList = ColorStateList.valueOf(Color.BLACK)
                setOnCheckedChangeListener { _, b ->
                    this@AboutActivity.getSharedPreferences("private", 0).edit().putBoolean("devNotifications", b).apply()
                    if (b)
                        FirebaseMessaging.getInstance().subscribeToTopic("dev")
                    else
                        FirebaseMessaging.getInstance().unsubscribeFromTopic("dev")
                }
            })
            addView(AuthenticElement.createText(this@AboutActivity, "Development notifications are for internal testing use only.", "left", Color.DKGRAY, 14f))
            if (this@AboutActivity.getSharedPreferences("private", 0).getBoolean("devNotifications", false))
                addView(AuthenticElement.createText(this@AboutActivity, FirebaseInstanceId.getInstance().token ?: "<unavailable>", "left", Color.DKGRAY, 14f, selectable = true))
            addView(AuthenticElement.createSeparator(this@AboutActivity, false))
        }
    }
}
