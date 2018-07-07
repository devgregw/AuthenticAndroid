package church.authenticcity.android

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import church.authenticcity.android.classes.AuthenticElement
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyColorsAndTypefaces
import church.authenticcity.android.helpers.applyTypeface
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.math.roundToInt

class AboutActivity : AppCompatActivity() {

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_about, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_launchAms -> {
                ButtonAction.openUrl("https://accams.devgregw.com").invoke(this)
                true
            }
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
            image.setOnLongClickListener {
                AlertDialog.Builder(this@AboutActivity).setTitle("Development Notifications").setMessage("Development notifications are for internal testing use only.  Interacting with them may cause instability.\n\n/topics/dev").setPositiveButton("Subscribe") { _, _ ->
                    FirebaseMessaging.getInstance().subscribeToTopic("dev")
                    Utils.makeToast(this@AboutActivity, "Subscribed to /topics/dev", Toast.LENGTH_SHORT).show()
                }.setNegativeButton("Unsubscribe") { _, _ ->
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("dev")
                    Utils.makeToast(this@AboutActivity, "Unsubscribed from /topics/dev", Toast.LENGTH_SHORT).show()
                }.create().applyColorsAndTypefaces().show()
                true
            }
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
            addView(AuthenticElement.createText(this@AboutActivity, "FOR ALL TO LOVE GOD, LOVE PEOPLE, AND IMPACT THE KINGDOM.", "center", size = 33f))
            addView(AuthenticElement.createSeparator(this@AboutActivity, true))
            addView(AuthenticElement.createTitle(this@AboutActivity, "CONNECT WITH US", "center"))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://www.authenticcity.church"), "Visit Our Website"))
            addView(AuthenticElement.createSeparator(this@AboutActivity, false))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://www.authenticcity.church/next/"), "Take the Next Step"))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://www.authenticcity.church/new-products/"), "Merchandise"))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://www.authenticcity.church/give/"), "Give"))
            addView(AuthenticElement.createSeparator(this@AboutActivity, false))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://www.instagram.com/authentic_city_church/"), "Instagram"))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://www.facebook.com/AuthenticCityChurch/"), "Facebook"))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://twitter.com/AuthenticCity_"), "Twitter"))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://www.youtube.com/channel/UCxrYck_z50n5It7ifj1LCjA"), "YouTube"))
            addView(AuthenticElement.createSeparator(this@AboutActivity, true))
            addView(AuthenticElement.createText(this@AboutActivity, "Designed and developed by Greg Whatley for Authentic City Church", "center"))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://devgregw.com"), "Visit My Website"))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://github.com/devgregw/AuthenticAndroid"), "GitHub Repository"))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://trello.com/b/QUgekVh6"), "Trello Roadmap"))
            addView(AuthenticElement.createSeparator(this@AboutActivity, true))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://docs.accams.devgregw.com"), "Legal Documentation"))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://docs.accams.devgregw.com/privacy-policy"), "Privacy Policy"))
            OssLicensesMenuActivity.setActivityTitle("Licenses")
            addView(Button(context).apply {
                this.text = "Licenses"
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
            addView(AuthenticElement.createText(this@AboutActivity, FirebaseInstanceId.getInstance().token ?: "Unknown", "left", Color.GRAY, 16f, true))
            addView(AuthenticElement.createSeparator(this@AboutActivity, false))
        }
    }
}
