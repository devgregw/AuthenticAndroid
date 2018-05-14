package church.authenticcity.android

import android.Manifest
import android.animation.Animator
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyColorsAndTypefaces
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    fun finishInitialization() {
        val image = findViewById<ImageView>(R.id.logo)
        val button = findViewById<ImageButton>(R.id.tabsButton).apply { setOnClickListener { TabListActivity.start(this@MainActivity) } }
        findViewById<ProgressBar>(R.id.progress_bar).animate().setStartDelay(100L).alpha(0f).setInterpolator(AccelerateDecelerateInterpolator()).duration = 250L
        image.animate().setStartDelay(100L).alpha(1f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(250L).setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
                //not implemented
            }

            override fun onAnimationEnd(p0: Animator?) {
                button.animate().setStartDelay(250L).translationY(0f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(500L).setListener(object : Animator.AnimatorListener {
                    override fun onAnimationEnd(p0: Animator?) {
                        button.animate().setStartDelay(0L).alpha(1f).setInterpolator(AccelerateDecelerateInterpolator()).duration = 50L
                    }

                    override fun onAnimationRepeat(p0: Animator?) {
                        //not implemented
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                        //not implemented
                    }

                    override fun onAnimationStart(p0: Animator?) {
                        //not implemented
                    }
                })
                image.animate().setStartDelay(250L).translationY(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -37.5f, resources.displayMetrics)).setInterpolator(AccelerateDecelerateInterpolator()).duration = 500L
            }

            override fun onAnimationCancel(p0: Animator?) {
                //not implemented
            }

            override fun onAnimationStart(p0: Animator?) {
                //not implemented
            }
        })
    }

    private fun initialize() {
        findViewById<ImageButton>(R.id.tabsButton).apply {
            translationY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75f, resources.displayMetrics)
            if (Utils.checkSdk(23))
                background = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 255, 255, 255)), null, null).apply { radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics).roundToInt() }
        }
        FirebaseDatabase.getInstance().reference.child("versions").child("android").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                AlertDialog.Builder(this@MainActivity).setTitle("Unexpected Error").setCancelable(false).setMessage("An unexpected error occurred while checking for updates. You may be able to continue using the app.\n\nCode: ${p0?.code
                        ?: "unknown"}\nMessage: ${p0?.message ?: "unknown"}\nDetails: ${p0?.details
                        ?: "unknown"}").setPositiveButton("Dismiss", { _, _ -> this@MainActivity.finishInitialization() }).create().applyColorsAndTypefaces().show()
            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (Utils.isUpdateAvailable(p0!!.value!!.toString().toInt())) {
                    AlertDialog.Builder(this@MainActivity).setTitle("Update Available").setCancelable(false).setMessage("An update is available for the Authentic City Church app.  We highly recommend that you update to avoid missing out on new features.").setPositiveButton("Update", { _, _ -> this@MainActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))) }).setNegativeButton("Not Now", { _, _ -> this@MainActivity.finishInitialization() }).create().applyColorsAndTypefaces().show()
                } else {
                    this@MainActivity.finishInitialization()
                }
            }
        })
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        getSharedPreferences("private", 0).edit().putBoolean("permissionsRequested", true).apply()
        initialize()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("EXTRAS", intent.extras?.toString() ?: "NULL")
        findViewById<ImageView>(R.id.logo).alpha = 0f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !getSharedPreferences("private", 0).getBoolean("permissionsRequested", false) && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(Array(1, { _ -> Manifest.permission.WRITE_CALENDAR }), 100)
        else
            initialize()
    }

}
