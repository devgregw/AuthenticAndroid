package church.authenticcity.android

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.viewpager.widget.PagerAdapter
import church.authenticcity.android.classes.AuthenticAppearance
import church.authenticcity.android.classes.AuthenticElement
import church.authenticcity.android.classes.AuthenticTab
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.helpers.*
import church.authenticcity.android.services.FirebaseMessagingService
import church.authenticcity.android.views.VerticalViewPager
import church.authenticcity.android.views.recyclerView.Tile
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.crashlytics.android.Crashlytics
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.new_tabs_list.*
import kotlinx.android.synthetic.main.new_tabs_list.swipe_refresh_layout
import kotlinx.android.synthetic.main.new_tabs_list.view.*
import org.threeten.bp.DayOfWeek
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.thread
import kotlin.math.roundToInt

class NewHomeActivity : AppCompatActivity() {
    private lateinit var viewPager: VerticalViewPager
    private var homeView: View? = null
    private var tabsView: View? = null
    private var appRef: DatabaseReference? = null
    private var tabsRef: DatabaseReference? = null
    private lateinit var appearance: AuthenticAppearance

    private val tabsEventListener = object : ValueEventListener {
        @SuppressLint("SetTextI18n")
        override fun onCancelled(p0: DatabaseError) {
            AlertDialog.Builder(this@NewHomeActivity).setTitle("Error").setMessage("Sorry, an error occurred while loading content.\n${p0.message}").setPositiveButton("OK", null).create().applyColorsAndTypefaces().show()
        }

        override fun onDataChange(p0: DataSnapshot) {
            tiles_main.animate().alpha(0f).setDuration(250L).setStartDelay(0L).setListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animator: Animator?) {
                    thread {
                        if (!swipe_refresh_layout.isRefreshing)
                            return@thread
                        val constructed = p0.children.map { Utils.Constructors.constructTab(it.value!!) }
                        presentTiles(constructed.filterNotNull().filter {
                            it.isVisible
                        }, appearance)
                        if (constructed.count { it == null } > 0)
                            AlertDialog.Builder(this@NewHomeActivity).setTitle("Error").setMessage("We're having trouble loading content.  Please try again later.  We apologise for the inconvenience.").setPositiveButton("Dismiss", null).create().applyColorsAndTypefaces().show()
                    }
                    //trace.stop()
                }
            })
        }
    }

    private val appearanceEventListener = object : ValueEventListener {
        @SuppressLint("SetTextI18n")
        override fun onCancelled(p0: DatabaseError) {
            AlertDialog.Builder(this@NewHomeActivity).setTitle("Error").setMessage("Sorry, an error occurred while loading content.\n${p0.message}").setPositiveButton("OK", null).create().applyColorsAndTypefaces().show()
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
        refreshLivestreamStatus()
        thread {
            appRef?.removeEventListener(appearanceEventListener)
            appRef = FirebaseDatabase.getInstance().getReference(makePath("/appearance/"))
            appRef?.keepSynced(true)
            appRef?.addValueEventListener(appearanceEventListener)
        }
    }

    private fun makePath(path: String) = (if (AuthenticApplication.useDevelopmentDatabase) "/dev" else "") + path

    private fun presentTiles(tabs: List<AuthenticTab>, appearance: AuthenticAppearance) {
        val toTile: (AuthenticTab) -> Tile<AuthenticTab> = { t ->
            Tile(t.title, false, t.header, t) { tab -> if (tab.action == null) TabActivity.start(this@NewHomeActivity, tab) else tab.action.invoke(this@NewHomeActivity) }
        }
        thread {
            if (!swipe_refresh_layout.isRefreshing)
                return@thread
            if (tabsView?.findViewById<View>(R.id.toolbar) == null) {
                Crashlytics.log("WARNING: TabsListFragment toolbar is null!  Skipping adapter initialization.")
            } else {
                Utils.Temp.map = HashMap()
                val ueTile = Tile(appearance.events.title, false, appearance.events.header, appearance.events) { a -> EventListActivity.start(this@NewHomeActivity, a) }
                val leftTiles = tabs.filter { t -> t.index % 2 == 0 }.map(toTile)
                val rightTiles = java.util.ArrayList<Tile<*>>().apply {
                    add(ueTile)
                    addAll(tabs.filter { t -> t.index % 2 != 0 }.map(toTile))
                }
                val willFillLeft = if (leftTiles.count() > 4) false else appearance.tabs.fill
                val willFillRight = if (rightTiles.count() > 4) false else appearance.tabs.fill
                val willFill = willFillLeft and willFillRight
                this@NewHomeActivity.runOnUiThread {
                    scrollView.setScrollingEnabled(!willFill)
                    var sb = 0
                    if (Build.VERSION.SDK_INT >= 28) {
                        val cutout = window.decorView.rootWindowInsets.displayCutout
                        sb += cutout?.safeInsetTop ?: 0
                        sb += (cutout?.safeInsetBottom ?: 0) / 4

                    }
                    val rect = Rect()
                    window.decorView.getWindowVisibleDisplayFrame(rect)
                    sb -= rect.top

                    //
                    val height = this@NewHomeActivity.resources.displayMetrics.heightPixels - toolbar.height - livestream.height + sb
                    tile_list_left.removeAllViews()
                    tile_list_right.removeAllViews()
                    leftTiles.forEach {
                        tile_list_left.addView(AuthenticElement.createTile(this, if (willFill) (height / leftTiles.size) + 1 else 0, it))
                    }
                    rightTiles.forEach {
                        tile_list_right.addView(AuthenticElement.createTile(this, if (willFill) (height / rightTiles.size) + 1 else 0, it))
                    }
                    tiles_main.animate().setStartDelay(200L).alpha(1f).duration = 250L
                    swipe_refresh_layout.isRefreshing = false
                    //

                    /*val layout = DualRecyclerView.create(this@HomeActivity, leftTiles, rightTiles, appearance, this@NewHomeActivity.resources.displayMetrics.heightPixels - tabsView!!.findViewById<View>(R.id.toolbar).height + sb)
                    tabsView!!.root.addView(layout)
                    layout.animate().setStartDelay(250L).alpha(1f).duration = 250L*/
                }
            }
        }
    }

    private fun setupTitleBar() {
        setupLivestreamBar()
        if (Utils.checkSdk(23)) {
            val buttonWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, resources.displayMetrics).roundToInt()
            val ripple = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 255, 255, 255)), null, null).apply { radius = buttonWidth / 2 }
            home_down_arrow.foreground = ripple
            expanded_menu.foreground = ripple
        }
        home_title.typeface = Utils.getTitleTypeface(this)
        home_down_arrow.setOnClickListener { goHome() }
        expanded_menu.apply {
            val popup = PopupMenu(context, expanded_menu)
            popup.menuInflater.inflate(R.menu.menu_info, popup.menu)
            if (!BuildConfig.DEBUG)
                popup.menu.removeItem(R.id.menu_advanced)
            else
                popup.menu.findItem(R.id.menu_db).setTitle(if (AuthenticApplication.useDevelopmentDatabase) R.string.db_prod else R.string.db_dev)
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
                        loadTabs(true)
                        true
                    }
                    else -> false
                }
            }
            this@NewHomeActivity.expanded_menu.setOnClickListener {
                popup.show()
            }
        }
    }

    private fun setupLivestreamBar() {
        livestream_watch.typeface = Utils.getTitleTypeface(this)
        livestream_sundays.typeface = Utils.getTitleTypeface(this)
        livestream_services.typeface = Utils.getTitleTypeface(this)
        refreshLivestreamStatus()
    }

    private fun refreshLivestreamStatus() {
        livestream_progress.animate().setStartDelay(0L).alpha(1f).duration = 250L
        livestream_watch.animate().setStartDelay(0L).alpha(0f).duration = 250L
        livestream_text.animate().setStartDelay(0L).alpha(0f).duration = 250L
        val setText: (Boolean) -> Unit = { isLive ->
            livestream_progress.animate().setStartDelay(0L).alpha(0f).duration = 250L
            livestream_watch.animate().setStartDelay(250L).alpha(if (isLive) 1f else 0f).duration = 250L
            livestream_text.animate().setStartDelay(250L).alpha(if (isLive) 0f else 1f).duration = 250L
        }
        Log.v("Authentic Livestream", "Checking livestream status...")
        if (!swipe_refresh_layout.isRefreshing) {
            Log.w("Authentic Livestream", "Skipping livestream check - The home screen is not refreshing")
            setText(false)
            return
        }
        if (org.threeten.bp.LocalDate.now().dayOfWeek != DayOfWeek.SUNDAY) {
            setText(false)
            Log.w("Authentic Livestream", "Skipping livestream check - it's not Sunday")
            return
        }
        val queue = Volley.newRequestQueue(this)
        queue.add(StringRequest(Request.Method.GET, "https://extras.gregwhatley.dev/authentic/youtube.php", { str ->
            val obj = Parser().parse(StringBuilder(str)) as JsonObject
            if (obj.count() > 0) {
                if (obj.values.map { o -> o?.toString() ?: "" }[0].contains("stream", true)) {
                    setText(true)
                    livestream.setOnClickListener {
                        ButtonAction.openUrl("https://youtube.com/watch?v=${obj.keys.toList()[0]}").invoke(this)
                    }
                    return@StringRequest
                }
            }
            setText(false)
        }, { err ->
            setText(false)
            Log.e("Authentic Livestream", if (String.isNullOrWhiteSpace(err.message)) "<no message>" else err.message!!)
            err.printStackTrace()
        }).setShouldCache(false))
    }

    private fun homeViewAnimation1() {
        tabsButton.apply { setOnClickListener { this@NewHomeActivity.goToTabs() } }
        progress_bar.animate().setStartDelay(100L).alpha(0f).setInterpolator(AccelerateDecelerateInterpolator()).duration = 250L
        logo.animate().setStartDelay(100L).alpha(1f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(250L).setListener(object : SimpleAnimatorListener() {
            override fun onAnimationEnd(animator: Animator?) {
                homeViewAnimation2()
            }
        })
    }

    private fun homeViewAnimation2() {
        tabsButton.animate().setStartDelay(250L).translationY(0f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(500L).setListener(object : SimpleAnimatorListener() {
            override fun onAnimationEnd(animator: Animator?) {
                homeViewAnimation3()
            }
        })
        logo.animate().setStartDelay(250L).translationY(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -37.5f, resources.displayMetrics)).setInterpolator(AccelerateDecelerateInterpolator()).duration = 500L
    }

    private fun homeViewAnimation3() {
        tabsButton.animate().setStartDelay(0L).alpha(1f).setInterpolator(AccelerateDecelerateInterpolator()).duration = 50L
    }

    private fun setupHomeView() {
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
                AlertDialog.Builder(this@NewHomeActivity).setTitle("Unexpected Error").setCancelable(false).setMessage("An unexpected error occurred while checking for updates. You may be able to continue using the app.\n\nCode: ${p0.code}\nMessage: ${p0.message}\nDetails: ${p0.details}").setPositiveButton("Dismiss") { _, _ -> this@NewHomeActivity.homeViewAnimation1() }.create().applyColorsAndTypefaces().show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (Utils.isUpdateAvailable(p0.value!!.toString().toInt())) {
                    AlertDialog.Builder(this@NewHomeActivity).setTitle("Update Available").setCancelable(false).setMessage("An update is available for the Authentic City Church app.  We highly recommend that you update to avoid missing out on new features.").setPositiveButton("Update") { _, _ -> this@NewHomeActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${this@NewHomeActivity.packageName}"))) }.setNegativeButton("Not Now") { _, _ -> this@NewHomeActivity.homeViewAnimation1() }.create().applyColorsAndTypefaces().show()
                } else {
                    this@NewHomeActivity.homeViewAnimation1()
                }
            }
        })
    }

    private fun setupTabsView() {
        loadTabs(false)
        setupTitleBar()
        tabsView!!.swipe_refresh_layout.apply {
            setOnRefreshListener {
                loadTabs(true)
            }
        }
    }

    private fun setupViewPager() {
        viewPager.adapter = object : PagerAdapter() {
            override fun instantiateItem(container: ViewGroup, position: Int): Any = when (position) {
                0 -> {
                    if (homeView == null) homeView = LayoutInflater.from(this@NewHomeActivity).inflate(R.layout.fragment_home, container, false)
                    container.addView(homeView!!)
                    setupHomeView()
                    homeView!!
                }
                1 -> {
                    if (tabsView == null) tabsView = LayoutInflater.from(this@NewHomeActivity).inflate(R.layout.new_tabs_list, container, false)
                    container.addView(tabsView!!)
                    setupTabsView()
                    tabsView!!
                }
                else -> View(this@NewHomeActivity)
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
        setupViewPager()
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
            setupViewPager()
    }
}