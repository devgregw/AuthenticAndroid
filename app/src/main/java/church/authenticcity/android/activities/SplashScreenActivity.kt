package church.authenticcity.android.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import church.authenticcity.android.AuthenticApplication
import church.authenticcity.android.databinding.ActivitySplashScreenBinding
import church.authenticcity.android.helpers.DatabaseHelper
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyColorsAndTypefaces
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    private fun loadApp() {
        val data: MutableList<HashMap<String, Any>> = mutableListOf()
        DatabaseHelper.loadAppearance { app ->
            DatabaseHelper.loadAllTabs(false) { _, tabs ->
                data.add(HashMap<String, Any>().apply {
                    put("id", "upcoming_events")
                    put("index", app.events.index)
                    put("title", "EVENTS")
                    put("special_type", "upcoming_events")
                })
                tabs!!.forEach {
                    if (it.isVisible)
                        data.add(HashMap<String, Any>().apply {
                            put("id", it.id)
                            put("index", it.index)
                            put("title", it.title)
                            put("special_type", it.specialType ?: "")
                        })
                }
                data.sortBy { it["index"] as Int }
                TabbedHomeActivity.appearance = app
                this@SplashScreenActivity.startActivity(Intent(this@SplashScreenActivity, TabbedHomeActivity::class.java).apply {
                    if (this@SplashScreenActivity.intent.extras != null)
                        putExtras(this@SplashScreenActivity.intent.extras!!)
                    putExtra("ids", data.map { it["id"] as String }.toTypedArray())
                    putExtra("indices", data.map { it["index"] as Int }.toIntArray())
                    putExtra("titles", data.map { it["title"] as String }.toTypedArray())
                    putExtra("specialTypes", data.map { it["special_type"] as String }.toTypedArray())
                })
                runOnUiThread { this@SplashScreenActivity.finish() }
            }
        }
    }

    private fun checkForUpdates() {
        if (AuthenticApplication.useDevelopmentDatabase) {
            loadApp()
            return
        }
        FirebaseDatabase.getInstance().reference.child("versions").child("android").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                AlertDialog.Builder(this@SplashScreenActivity).setTitle("Unexpected Error").setCancelable(false).setMessage("An unexpected error occurred while checking for updates. You may be able to continue using the app.\n\nCode: ${p0.code}\nMessage: ${p0.message}\nDetails: ${p0.details}").setPositiveButton("Dismiss") { _, _ -> loadApp() }.create().applyColorsAndTypefaces().show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (Utils.isUpdateAvailable(p0.value!!.toString().toInt()))
                    AlertDialog.Builder(this@SplashScreenActivity).setTitle("Update Available").setCancelable(false).setMessage("An update is available for the Authentic City Church app.  We highly recommend that you update to avoid missing out on new features.").setPositiveButton("Update") { _, _ ->
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                        } catch (_: ActivityNotFoundException) {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                        }
                    }.setNegativeButton("Not Now") { _, _ -> loadApp() }.create().applyColorsAndTypefaces().show()
                else loadApp()
            }
        })
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !getSharedPreferences("private", 0).getBoolean("permissionsRequested", false))
            requestPermissions(ArrayList<String>().apply {
                add(Manifest.permission.READ_CALENDAR)
                add(Manifest.permission.WRITE_CALENDAR)
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }.toTypedArray(), 100)
        else
            checkForUpdates()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        getSharedPreferences("private", 0).edit().putBoolean("permissionsRequested", true).apply()
        checkForUpdates()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.splashRoot.postDelayed({
            checkPermissions()
        }, 500)
    }
}