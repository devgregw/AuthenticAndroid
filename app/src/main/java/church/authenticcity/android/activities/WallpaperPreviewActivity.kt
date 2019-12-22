package church.authenticcity.android.activities

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import church.authenticcity.android.R
import church.authenticcity.android.classes.ImageResource
import church.authenticcity.android.helpers.Utils
import kotlinx.android.synthetic.main.activity_wallpaper_preview.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class WallpaperPreviewActivity : AppCompatActivity() {
    private val mHideHandler = Handler()

    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    private lateinit var downloadReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_wallpaper_preview)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                Utils.makeToast(this@WallpaperPreviewActivity, "Download complete", Toast.LENGTH_SHORT).show()
            }
        }
        registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        mVisible = true

        image.scaleType = ImageView.ScaleType.CENTER_CROP
        ImageResource(intent.getStringExtra("imageName")!!, intent.getIntExtra("width", 1), intent.getIntExtra("height", 1)).load(this, image)

        // Set up the user interaction to manually show or hide the system UI.
        image.setOnClickListener { toggle() }
        cancel_button.typeface = Utils.getTextTypeface(this)
        save_button.typeface = Utils.getTextTypeface(this)
        wallpaper_text_view.typeface = Utils.getTextTypeface(this)
        cancel_button.setOnClickListener { finish() }
        save_button.setOnClickListener {
            ImageResource(intent.getStringExtra("imageName")!!, intent.getIntExtra("width", 1), intent.getIntExtra("height", 1)).saveToGallery(this)
        }

    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(downloadReceiver)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        hide()
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        root.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        mVisible = false
    }

    private fun show() {
        // Show the system bar
        root.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        mVisible = true
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }
}