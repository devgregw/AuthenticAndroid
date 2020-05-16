package church.authenticcity.android.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import church.authenticcity.android.AuthenticApplication
import church.authenticcity.android.BuildConfig
import church.authenticcity.android.R
import church.authenticcity.android.classes.AuthenticAppearance
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.fragments.MoreFragment
import church.authenticcity.android.helpers.FragmentHelper
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyTypeface
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.tabs.TabLayout
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_tabbed_home.*
import kotlinx.android.synthetic.main.toolbar_view.*

class TabbedHomeActivity : AppCompatActivity() {
    companion object {
        var appearance: AuthenticAppearance = AuthenticAppearance.default
    }

    private lateinit var adapter: FragmentAdapter
    private lateinit var ids: Array<String>
    private lateinit var titles: Array<String>
    private lateinit var specialTypes: Array<String>

    class FragmentAdapter(private val ids: Array<String>, private val titles: Array<String>, private val specialTypes: Array<String>, manager: FragmentManager) : FragmentStatePagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            if (position in 0 until if (ids.count() >= 5) 4 else ids.count())
                return FragmentHelper.getTabFragment(ids[position], titles[position], specialTypes[position], null)
            return MoreFragment(ids.drop(4).toTypedArray(), titles.drop(4).toTypedArray(), specialTypes.drop(4).toTypedArray())
        }

        override fun getCount(): Int = if (ids.count() >= 5) 5 else ids.count()
    }

    private fun newTab(title: String): TabLayout.Tab {
        val tab = tab_layout.newTab()
        tab.text = Utils.makeTypefaceSpan(this, title, ResourcesCompat.getFont(this, R.font.alpenglow_expanded)!!)
        return tab
    }

    private fun getStringArray(name: String, savedInstanceState: Bundle?): Array<String> {
        return savedInstanceState?.getStringArray(name) ?: intent.getStringArrayExtra(name) ?: ArrayList<String>().toTypedArray()
    }

    private fun initialize(index: Int, savedInstanceState: Bundle?) {
        tab_layout.addOnTabSelectedListener(object : TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
            override fun onTabSelected(p0: TabLayout.Tab?) {
                view_pager.setCurrentItem(p0?.position ?: 0, true)
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }
        })
        ids = getStringArray("ids", savedInstanceState)
        titles = getStringArray("titles", savedInstanceState)
        specialTypes = getStringArray("specialTypes", savedInstanceState)
        adapter = FragmentAdapter(ids, titles, specialTypes, supportFragmentManager)
        view_pager.adapter = adapter
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                tab_layout.getTabAt(position)?.select()
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
        })
        val titles = intent.getStringArrayExtra("titles")!!
        if (titles.count() >= 5) {
            titles.take(4).forEach {
                tab_layout.addTab(newTab(it))
            }
            tab_layout.addTab(newTab("MORE"))
        } else titles.forEach {
            tab_layout.addTab(newTab(it))
        }
        view_pager.setCurrentItem(index, false)
        expanded_menu.apply {
            val popup = PopupMenu(context, expanded_menu)
            popup.menuInflater.inflate(R.menu.menu_info, popup.menu)
            if (!BuildConfig.DEBUG)
                popup.menu.removeItem(R.id.menu_advanced)
            else {
                popup.menu.findItem(R.id.menu_db).setTitle(if (AuthenticApplication.useDevelopmentDatabase) R.string.db_prod else R.string.db_dev)
                popup.menu.findItem(R.id.menu_advanced).applyTypeface(this@TabbedHomeActivity)
                popup.menu.findItem(R.id.menu_db).applyTypeface(this@TabbedHomeActivity)
                popup.menu.findItem(R.id.menu_copy_fcm).applyTypeface(this@TabbedHomeActivity)
            }
            popup.menu.findItem(R.id.menu_licenses).applyTypeface(this@TabbedHomeActivity)
            popup.menu.findItem(R.id.menu_privacy).applyTypeface(this@TabbedHomeActivity)
            popup.menu.findItem(R.id.menu_settings).applyTypeface(this@TabbedHomeActivity)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_settings -> {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        })
                        true
                    }
                    R.id.menu_privacy -> {
                        ButtonAction.openUrl("https://authenticdocs.gregwhatley.dev/privacy-policy").invoke(context)
                        true
                    }
                    R.id.menu_licenses -> {
                        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                        true
                    }
                    R.id.menu_copy_fcm -> {
                        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.w("CopyIID", "Unable to copy IID", task.exception)
                                Utils.makeToast(context, "Unable to copy registration token.", Toast.LENGTH_SHORT).show()
                                return@addOnCompleteListener
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("fcm", task.result?.token ?: "<unavailable>"))
                            Utils.makeToast(context, "Your registration token was copied.", Toast.LENGTH_SHORT).show()
                        }

                        true
                    }
                    R.id.menu_db -> {
                        AuthenticApplication.useDevelopmentDatabase = !AuthenticApplication.useDevelopmentDatabase
                        startActivity(Intent(context, SplashScreenActivity::class.java))
                        finish()
                        true
                    }
                    else -> false
                }
            }
            setOnClickListener {
                popup.show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabbed_home)
        initialize(0, savedInstanceState)
    }

    /*override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArray("ids", ids)
        outState.putStringArray("titles", titles)
        outState.putStringArray("specialTypes", specialTypes)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        index = view_pager.currentItem
        super.onPause()

    }

    override fun onRestart() {
        initialize(index, null)
        super.onRestart()
    }*/
}
