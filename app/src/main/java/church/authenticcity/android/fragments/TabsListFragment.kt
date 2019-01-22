package church.authenticcity.android.fragments


import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import church.authenticcity.android.*
import church.authenticcity.android.R
import church.authenticcity.android.classes.AuthenticAppearance
import church.authenticcity.android.classes.AuthenticTab
import church.authenticcity.android.helpers.SimpleAnimatorListener
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyColorsAndTypefaces
import church.authenticcity.android.helpers.setScrollingEnabled
import church.authenticcity.android.views.TitleBarView
import church.authenticcity.android.views.recyclerView.DualRecyclerView
import church.authenticcity.android.views.recyclerView.Tile
import com.crashlytics.android.Crashlytics
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_tabs_list.*
import kotlinx.android.synthetic.main.fragment_tabs_list.view.*
import java.util.*
import kotlin.concurrent.thread

class TabsListFragment : Fragment() {
    companion object {
        fun create() = TabsListFragment()//.apply { this.activity = activity }
    }

    //private lateinit var activity: Activity
    private var appRef: DatabaseReference? = null
    private var tabsRef: DatabaseReference? = null
    private lateinit var appearance: AuthenticAppearance

    private fun makePath(path: String) = (if (AuthenticApplication.useDevelopmentDatabase) "/dev" else "") + path

    private fun getRecyclerViewHost(): LinearLayout {
        val host = view!!.root.findViewWithTag<LinearLayout>("recyclerViewHost")
        return if (host == null) {
            view!!.root.removeAllViews()
            LinearLayout(context).apply {
                layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                tag = "recyclerViewHost"
                view!!.root.addView(this)
            }
        } else
            host
    }

    private val tabsEventListener = object : ValueEventListener {
        @SuppressLint("SetTextI18n")
        override fun onCancelled(p0: DatabaseError) {
            if (!this@TabsListFragment.isAdded) {
                Crashlytics.log("WARNING: TabsListFragment is not added!  Skipping tabsEventListener->onCancelled")
                return
            }
            view!!.root.removeAllViews()
            view!!.root.addView(TextView(this@TabsListFragment.requireContext()).apply {
                text = "ERROR: ${p0.message}"
                typeface = Utils.getTextTypeface(this@TabsListFragment.requireContext())
                setTextColor(Color.WHITE)
            })
        }

        override fun onDataChange(p0: DataSnapshot) {
            if (!this@TabsListFragment.isAdded) {
                Crashlytics.log("WARNING: TabsListFragment is not added!  Skipping tabsEventListener->onDataChange")
                return
            }
            this@TabsListFragment.getRecyclerViewHost().animate().alpha(0f).setDuration(250L).setStartDelay(0L).setListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animator: Animator?) {
                    view!!.root.apply {
                        removeAllViews()
                        addView(TitleBarView.create(this@TabsListFragment.requireContext(), this, (requireActivity() as HomeActivity)::goHome, ::loadTabs))
                    }
                    view!!.swipe_refresh_layout.isRefreshing = false
                    thread {
                        val constructed = p0.children.map { Utils.Constructors.constructTab(it.value!!) }
                        initializeAdapter(constructed.filter { it != null }.map { it!! }.filter {
                            it.isVisible
                        }, appearance)
                        if (constructed.count { it == null } > 0)
                            AlertDialog.Builder(requireContext()).setTitle("Error").setMessage("We're having trouble loading content.  Please try again later.  We apologise for the inconvenience.").setPositiveButton("Dismiss", null).create().applyColorsAndTypefaces().show()
                    }
                    //trace.stop()
                }
            })
        }
    }

    private val appearanceEventListener = object : ValueEventListener {
        @SuppressLint("SetTextI18n")
        override fun onCancelled(p0: DatabaseError) {
            view!!.root.removeAllViews()
            view!!.root.addView(TextView(this@TabsListFragment.requireContext()).apply {
                text = "ERROR: ${p0.message}"
                typeface = Utils.getTextTypeface(this@TabsListFragment.requireContext())
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
        if (!isAdded || view?.findViewById<View>(R.id.toolbar) == null) {
            Crashlytics.log("WARNING: TabsListFragment is not added or toolbar is null!  Skipping adapter initialization.")
            return
        }
        val toTile: (AuthenticTab) -> Tile<AuthenticTab> = { t ->
            Tile(t.title, false, t.header, t) { tab -> if (tab.action == null) TabActivity.start(requireContext(), tab) else tab.action.invoke(requireContext()) }
        }
        thread {
            val ueTile = Tile(appearance.events.title, false, appearance.events.header, appearance.events) { a -> EventListActivity.start(requireActivity(), a) }
            val leftTiles = tabs.filter { t -> t.index % 2 == 0 }.map(toTile)
            val rightTiles = ArrayList<Tile<*>>().apply {
                add(ueTile)
                addAll(tabs.filter { t -> t.index % 2 != 0 }.map(toTile))
            }
            val willFillLeft = if (leftTiles.count() > 4) false else appearance.tabs.fillLeft
            val willFillRight = if (rightTiles.count() > 4) false else appearance.tabs.fillRight
            this@TabsListFragment.requireActivity().runOnUiThread {
                view!!.tabs_scroll_view.setScrollingEnabled((willFillLeft && willFillRight).not())
                val layout = DualRecyclerView.create(requireActivity(), leftTiles, rightTiles, appearance, requireContext().resources.displayMetrics.heightPixels - view!!.findViewById<View>(R.id.toolbar).height)
                view!!.root.addView(layout)
                layout.animate().setStartDelay(250L).alpha(1f).duration = 250L
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view!!.apply {
            //Handler().postDelayed({
                loadTabs(false)
            //}, 1750L)
            swipe_refresh_layout.apply {
                setOnRefreshListener {
                    loadTabs(true)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        tabsRef?.removeEventListener(tabsEventListener)
        appRef?.removeEventListener(appearanceEventListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tabs_list, container, false)
    }


}
