package church.authenticcity.android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, NewHomeActivity::class.java).apply {
            if (intent.extras != null)
                putExtras(intent.extras!!)
        })
        finish()
    }
}