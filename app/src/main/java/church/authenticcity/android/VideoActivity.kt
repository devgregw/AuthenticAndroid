package church.authenticcity.android

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import church.authenticcity.android.helpers.Utils
import kotlinx.android.synthetic.main.activity_video.*

class VideoActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context, provider: String, id: String, title: String) {
            context.startActivity(Intent(context, VideoActivity::class.java).apply {
                putExtra("provider", provider)
                putExtra("id", id)
                putExtra("title", title)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_video)
        Utils.makeToast(this, "Swipe down from the top of the screen and press the back button to exit.", Toast.LENGTH_LONG).apply {
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
        Handler().postDelayed({
            video_webview.apply {
                val provider = this@VideoActivity.intent.getStringExtra("provider")
                val videoId = this@VideoActivity.intent.getStringExtra("id")
                loadUrl(if (provider == "YouTube") "https://www.youtube.com/embed/$videoId" else "https://player.vimeo.com/video/$videoId")
                settings.javaScriptEnabled = true
                setBackgroundColor(Color.BLACK)
            }
        }, 500L)
    }
}
