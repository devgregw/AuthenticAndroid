package church.authenticcity.android

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import church.authenticcity.android.classes.AuthenticElement
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyTypeface
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

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
        findViewById<LinearLayout>(R.id.content_list).apply {
            val image = ImageView(this@AboutActivity)
            Glide.with(context).load(R.drawable.banner).transition(DrawableTransitionOptions.withCrossFade()).into(image)
            addView(image)
            addView(AuthenticElement.createTitle(this@AboutActivity, "AUTHENTIC CITY CHURCH", "center"))
            addView(AuthenticElement.createText(this@AboutActivity, "Version ${BuildConfig.VERSION_NAME}-u${BuildConfig.VERSION_CODE} for Android devices", "center"))
            addView(AuthenticElement.createSeparator(this@AboutActivity, true))
            addView(AuthenticElement.createCustomText(this@AboutActivity, "FOR ALL TO LOVE GOD, LOVE PEOPLE, AND IMPACT THE KINGDOM.", 33f, Utils.getTextTypeface(this@AboutActivity), "center", Color.BLACK))
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
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://devgregw.com"), "Learn More"))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://github.com/devgregw/AuthenticAndroid"), "GitHub Repository"))
            addView(AuthenticElement.createButton(this@AboutActivity, ButtonAction.openUrl("https://trello.com/b/QUgekVh6"), "Trello Roadmap"))
            addView(AuthenticElement.createSeparator(this@AboutActivity, false))
        }
    }
}
