package church.authenticcity.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import church.authenticcity.android.fragments.HomeFragment
import church.authenticcity.android.fragments.TabsListFragment
import church.authenticcity.android.services.FirebaseMessagingService
import church.authenticcity.android.views.VerticalViewPager

class HomeActivity : AppCompatActivity() {
    private lateinit var viewPager: VerticalViewPager

    fun initialize() {
        viewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return when (position) {
                    0 -> {
                        HomeFragment()
                    }
                    1 -> {
                        TabsListFragment.create(this@HomeActivity)
                    }
                    else -> throw IllegalArgumentException("Invalid position")
                }
            }

            override fun getCount(): Int = 2

        }
    }

    fun goHome() {
        viewPager.setCurrentItem(0, true)
    }

    fun goToTabs() {
        viewPager.setCurrentItem(1, true)
    }

    override fun onBackPressed() {
        when (viewPager.currentItem) {
            0 -> super.onBackPressed()
            else -> goHome()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        getSharedPreferences("private", 0).edit().putBoolean("permissionsRequested", true).apply()
        initialize()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        viewPager = findViewById(R.id.home_view_pager)
        FirebaseMessagingService.setAction(intent.extras)
        FirebaseMessagingService.invokeNotificationAction(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !getSharedPreferences("private", 0).getBoolean("permissionsRequested", false) && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(Array(1) { _ -> Manifest.permission.WRITE_CALENDAR }, 100)
        else
            initialize()
    }
}
