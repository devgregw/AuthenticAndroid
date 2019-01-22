package church.authenticcity.android

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import church.authenticcity.android.classes.AuthenticAppearance
import church.authenticcity.android.classes.AuthenticTab
import church.authenticcity.android.fragments.HomeFragment
import church.authenticcity.android.fragments.TabsListFragment
import church.authenticcity.android.helpers.SimpleAnimatorListener
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyColorsAndTypefaces
import church.authenticcity.android.helpers.setScrollingEnabled
import church.authenticcity.android.services.FirebaseMessagingService
import church.authenticcity.android.views.TitleBarView
import church.authenticcity.android.views.VerticalViewPager
import church.authenticcity.android.views.recyclerView.DualRecyclerView
import church.authenticcity.android.views.recyclerView.Tile
import com.crashlytics.android.Crashlytics
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_tabs_list.*
import kotlinx.android.synthetic.main.fragment_tabs_list.view.*
import java.util.HashMap
import kotlin.concurrent.thread
import kotlin.math.roundToInt

class HomeActivity : AppCompatActivity() {
    private lateinit var viewPager: VerticalViewPager

    private var homeView: View? = null
    private var tabsView: View? = null
    private var appRef: DatabaseReference? = null
    private var tabsRef: DatabaseReference? = null
    private lateinit var appearance: AuthenticAppearance

    private fun makePath(path: String) = (if (AuthenticApplication.useDevelopmentDatabase) "/dev" else "") + path

    private fun getRecyclerViewHost(): LinearLayout {
        val host = tabsView!!.root.findViewWithTag<LinearLayout>("recyclerViewHost")
        return if (host == null) {
            tabsView!!.root.removeAllViews()
            LinearLayout(this).apply {
                layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                tag = "recyclerViewHost"
                tabsView!!.root.addView(this)
            }
        } else
            host
    }

    private val tabsEventListener = object : ValueEventListener {
        @SuppressLint("SetTextI18n")
        override fun onCancelled(p0: DatabaseError) {
            tabsView!!.root.removeAllViews()
            tabsView!!.root.addView(TextView(this@HomeActivity).apply {
                text = "ERROR: ${p0.message}"
                typeface = Utils.getTextTypeface(this@HomeActivity)
                setTextColor(Color.WHITE)
            })
        }

        override fun onDataChange(p0: DataSnapshot) {
            this@HomeActivity.getRecyclerViewHost().animate().alpha(0f).setDuration(250L).setStartDelay(0L).setListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animator: Animator?) {
                    tabsView!!.root.apply {
                        removeAllViews()
                        addView(TitleBarView.create(this@HomeActivity, this, ::goHome, ::loadTabs))
                    }
                    tabsView!!.swipe_refresh_layout.isRefreshing = false
                    thread {
                        val constructed = p0.children.map { Utils.Constructors.constructTab(it.value!!) }
                        initializeAdapter(constructed.filter { it != null }.map { it!! }.filter {
                            it.isVisible
                        }, appearance)
                        if (constructed.count { it == null } > 0)
                            AlertDialog.Builder(this@HomeActivity).setTitle("Error").setMessage("We're having trouble loading content.  Please try again later.  We apologise for the inconvenience.").setPositiveButton("Dismiss", null).create().applyColorsAndTypefaces().show()
                    }
                    //trace.stop()
                }
            })
        }
    }

    private val appearanceEventListener = object : ValueEventListener {
        @SuppressLint("SetTextI18n")
        override fun onCancelled(p0: DatabaseError) {
            tabsView!!.root.removeAllViews()
            tabsView!!.root.addView(TextView(this@HomeActivity).apply {
                text = "ERROR: ${p0.message}"
                typeface = Utils.getTextTypeface(this@HomeActivity)
                setTextColor(Color.WHITE)
            })
        }

        override fun onDataChange(p0: DataSnapshot) {
            appearance = AuthenticAppearance(p0.value as HashMap<String, Any>)
            tabsRef?.removeEventListener(tabsEventListener)
            tabsRef = FirebaseDatabase.getInstance().getReference(makePath("/tabs/"))
            tabsRef?.keepSynced(true)
            tabsRef?.orderByChild("index")?.addValueEventListener(tabsEventListener)
        }
    }

    private fun loadTabs(refreshed: Boolean) {
        //val trace = FirebasePerformance.startTrace("load tabs")
        //if (refreshed)
        //    trace.incrementMetric("refresh tabs", 1L)
        swipe_refresh_layout.isRefreshing = true
        thread {
            appRef?.removeEventListener(appearanceEventListener)
            appRef = FirebaseDatabase.getInstance().getReference(makePath("/appearance/"))
            appRef?.keepSynced(true)
            appRef?.addValueEventListener(appearanceEventListener)
        }
    }

    private fun initializeAdapter(tabs: List<AuthenticTab>, appearance: AuthenticAppearance) {
        if (tabsView?.findViewById<View>(R.id.toolbar) == null) {
            Crashlytics.log("WARNING: TabsListFragment toolbar is null!  Skipping adapter initialization.")
            return
        }
        val toTile: (AuthenticTab) -> Tile<AuthenticTab> = { t ->
            Tile(t.title, false, t.header, t) { tab -> if (tab.action == null) TabActivity.start(this@HomeActivity, tab) else tab.action.invoke(this@HomeActivity) }
        }
        thread {
            val ueTile = Tile(appearance.events.title, false, appearance.events.header, appearance.events) { a -> EventListActivity.start(this@HomeActivity, a) }
            val leftTiles = tabs.filter { t -> t.index % 2 == 0 }.map(toTile)
            val rightTiles = java.util.ArrayList<Tile<*>>().apply {
                add(ueTile)
                addAll(tabs.filter { t -> t.index % 2 != 0 }.map(toTile))
            }
            val willFillLeft = if (leftTiles.count() > 4) false else appearance.tabs.fillLeft
            val willFillRight = if (rightTiles.count() > 4) false else appearance.tabs.fillRight
            this@HomeActivity.runOnUiThread {
                tabsView!!.tabs_scroll_view.setScrollingEnabled((willFillLeft && willFillRight).not())
                val layout = DualRecyclerView.create(this@HomeActivity, leftTiles, rightTiles, appearance, this@HomeActivity.resources.displayMetrics.heightPixels - tabsView!!.findViewById<View>(R.id.toolbar).height)
                tabsView!!.root.addView(layout)
                layout.animate().setStartDelay(250L).alpha(1f).duration = 250L
            }
        }
    }

    private fun finishHomeView() {
        val image = homeView!!.findViewById<ImageView>(R.id.logo)
        val button = homeView!!.findViewById<ImageButton>(R.id.tabsButton).apply { setOnClickListener { this@HomeActivity.goToTabs() } }
        homeView!!.findViewById<ProgressBar>(R.id.progress_bar).animate().setStartDelay(100L).alpha(0f).setInterpolator(AccelerateDecelerateInterpolator()).duration = 250L
        image.animate().setStartDelay(100L).alpha(1f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(250L).setListener(object : SimpleAnimatorListener() {
            override fun onAnimationEnd(animator: Animator?) {
                button.animate().setStartDelay(250L).translationY(0f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(500L).setListener(object : SimpleAnimatorListener() {
                    override fun onAnimationEnd(animator: Animator?) {
                        button.animate().setStartDelay(0L).alpha(1f).setInterpolator(AccelerateDecelerateInterpolator()).duration = 50L
                    }
                })
                //if (context != null) // ¯\_(ツ)_/¯
                    image.animate().setStartDelay(250L).translationY(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -37.5f, resources.displayMetrics)).setInterpolator(AccelerateDecelerateInterpolator()).duration = 500L
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun initializeHomeView() {
        if (BuildConfig.DEBUG) {
            homeView!!.debug_label.text = "DEBUG BUILD NOT FOR PRODUCTION\nVERSION ${BuildConfig.VERSION_NAME} BUILD ${BuildConfig.VERSION_CODE}"
        }
        else {
            homeView!!.debug_label.visibility = View.GONE
        }
        homeView!!.findViewById<ImageButton>(R.id.tabsButton).apply {
            translationY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75f, resources.displayMetrics)
            if (Utils.checkSdk(23))
                background = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 255, 255, 255)), null, null).apply { radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics).roundToInt() }
        }
        FirebaseDatabase.getInstance().reference.child("versions").child("android").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                AlertDialog.Builder(this@HomeActivity).setTitle("Unexpected Error").setCancelable(false).setMessage("An unexpected error occurred while checking for updates. You may be able to continue using the app.\n\nCode: ${p0.code}\nMessage: ${p0.message}\nDetails: ${p0.details}").setPositiveButton("Dismiss") { _, _ -> this@HomeActivity.finishHomeView() }.create().applyColorsAndTypefaces().show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (Utils.isUpdateAvailable(p0.value!!.toString().toInt())) {
                    AlertDialog.Builder(this@HomeActivity).setTitle("Update Available").setCancelable(false).setMessage("An update is available for the Authentic City Church app.  We highly recommend that you update to avoid missing out on new features.").setPositiveButton("Update") { _, _ -> this@HomeActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${this@HomeActivity.packageName}"))) }.setNegativeButton("Not Now") { _, _ -> this@HomeActivity.finishHomeView() }.create().applyColorsAndTypefaces().show()
                } else {
                    this@HomeActivity.finishHomeView()
                }
            }
        })
    }

    private fun initialize() {
        viewPager.adapter = object : PagerAdapter() {
            override fun instantiateItem(container: ViewGroup, position: Int): Any = when (position) {
                0 -> {
                    if (homeView == null) homeView = LayoutInflater.from(this@HomeActivity).inflate(R.layout.fragment_home, container, false)
                    container.addView(homeView!!)
                    initializeHomeView()
                    homeView!!
                }
                1 -> {
                    if (tabsView == null) tabsView = LayoutInflater.from(this@HomeActivity).inflate(R.layout.fragment_tabs_list, container, false)
                    container.addView(tabsView!!)
                    loadTabs(false)
                    tabsView!!.swipe_refresh_layout.apply {
                        setOnRefreshListener {
                            loadTabs(true)
                        }
                    }
                    tabsView!!
                }
                else -> View(this@HomeActivity)
            }

            override fun isViewFromObject(p0: View, p1: Any) = p0 == p1

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun getCount() = 2

            override fun getPageTitle(position: Int): CharSequence? = ""
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

    override fun onPause() {
        super.onPause()
        tabsRef?.removeEventListener(tabsEventListener)
        appRef?.removeEventListener(appearanceEventListener)
        tabsView?.swipe_refresh_layout?.isRefreshing = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        viewPager = findViewById(R.id.home_view_pager)
        FirebaseMessagingService.setAction(intent.extras)
        FirebaseMessagingService.invokeNotificationAction(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !getSharedPreferences("private", 0).getBoolean("permissionsRequested", false))
            requestPermissions(ArrayList<String>().apply {
                add(Manifest.permission.READ_CALENDAR)
                add(Manifest.permission.WRITE_CALENDAR)
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }.toTypedArray(), 100)
        else
            initialize()
    }
}

