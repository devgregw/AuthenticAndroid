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
                settings.mediaPlaybackRequiresUserGesture = false
                settings.javaScriptEnabled = true
                setBackgroundColor(Color.BLACK)
                val provider = this@VideoActivity.intent.getStringExtra("provider")
                val videoId = this@VideoActivity.intent.getStringExtra("id")
                when (provider) {
                    "YouTube" -> {
                        loadData("<!DOCTYPE html> <html> <head> <meta charset=\"utf-8\"> <meta name=\"viewport\" content=\"width=device-width,initial-scale=1,shrink-to-fit=no\"> <style type=\"text/css\"> *{margin:0;padding:0;}html,body{height:100%; width:100%;}body{display:table;}div{width: 100%; display:table-row;}iframe{width: 100%; height: 100%;}</style> </head> <body style=\"height: 100%;\"> <div/> <div id=\"player\"></div><script>var tag=document.createElement('script'); tag.src=\"https://www.youtube.com/iframe_api\"; var firstScriptTag=document.getElementsByTagName('script')[0]; firstScriptTag.parentNode.insertBefore(tag, firstScriptTag); var player; function onYouTubeIframeAPIReady(){player=new YT.Player('player',{height: '$height', width: '$width', videoId: '$videoId', events:{'onReady': onPlayerReady}});}function onPlayerReady(event){event.target.playVideo();}</script> </body> </html>", "text/html", "UTF-8")

                    }
                    "Vimeo" -> loadUrl("https://player.vimeo.com/video/$videoId?autoplay=1")
                }
                //loadUrl(if (provider == "YouTube") "https://www.youtube.com/embed/$videoId" else "https://player.vimeo.com/video/$videoId")

            }
        }, 500L)
    }
}
