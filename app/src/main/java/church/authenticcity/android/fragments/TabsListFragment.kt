package church.authenticcity.android.fragments


import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import church.authenticcity.android.*
import church.authenticcity.android.classes.AuthenticAppearance
import church.authenticcity.android.classes.AuthenticTab
import church.authenticcity.android.helpers.SimpleAnimatorListener
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyColorsAndTypefaces
import church.authenticcity.android.views.LivestreamView
import church.authenticcity.android.views.recyclerView.DualRecyclerView
import church.authenticcity.android.views.recyclerView.Tile
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.perf.FirebasePerformance
import kotlinx.android.synthetic.main.fragment_tabs_list.view.*
import java.util.*
import kotlin.math.roundToInt

class TabsListFragment : Fragment() {
    companion object {
        fun create(activity: Activity) = TabsListFragment().apply { this.activity = activity }
    }

    private lateinit var activity: Activity

    private fun loadTabs(refreshed: Boolean) {
        val trace = FirebasePerformance.startTrace("load tabs")
        if (refreshed)
            trace.incrementMetric("refresh tabs", 1L)
        val appRef = FirebaseDatabase.getInstance().getReference("/appearance/")
        appRef.keepSynced(true)
        appRef.addListenerForSingleValueEvent(object : ValueEventListener {
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
                val appearance = AuthenticAppearance(p0.value as HashMap<String, Any>)
                val tabsRef = FirebaseDatabase.getInstance().getReference("/tabs/")
                tabsRef.keepSynced(true)
                tabsRef.orderByChild("index").addListenerForSingleValueEvent(object : ValueEventListener {
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
                        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, this@TabsListFragment.resources.displayMetrics).roundToInt()
                        view!!.root.findViewWithTag<LinearLayout>("recyclerViewHost").animate().alpha(0f).setDuration(250L).setStartDelay(0L).setListener(object : SimpleAnimatorListener() {
                            override fun onAnimationEnd(animator: Animator?) {
                                view!!.root.apply {
                                    removeAllViews()
                                    addView(RelativeLayout(this@TabsListFragment.requireContext()).apply {
                                        id = R.id.toolbar
                                        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                        val buttonWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, resources.displayMetrics).roundToInt()
                                        setBackgroundColor(Color.BLACK)
                                        addView(RelativeLayout(this@TabsListFragment.requireContext()).apply {
                                            setPadding(px, px / 4, px, 0)
                                            layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                            setPadding(0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, context.resources.displayMetrics).roundToInt(), 0, 0)
                                            id = R.id.title
                                            addView(ImageButton(this@TabsListFragment.requireContext()).apply {
                                                layoutParams = RelativeLayout.LayoutParams(buttonWidth, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                                    addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                                                    addRule(RelativeLayout.CENTER_VERTICAL)
                                                }
                                                setPadding(0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, context.resources.displayMetrics).roundToInt(), 0, 0)
                                                setImageResource(R.drawable.ic_keyboard_arrow_down_white_36dp)
                                                setBackgroundColor(Color.TRANSPARENT)
                                                if (Utils.checkSdk(23))
                                                    foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 255, 255, 255)), null, null).apply { radius = buttonWidth / 2 }
                                                setOnClickListener { (this@TabsListFragment.requireActivity() as HomeActivity).goHome() }
                                            })
                                            addView(TextView(this@TabsListFragment.requireContext()).apply {
                                                text = Utils.makeTypefaceSpan("AUTHENTIC", Utils.getTitleTypeface(this@TabsListFragment.requireContext()))
                                                textSize = 30f
                                                letterSpacing = 0.25f
                                                setTextColor(Color.WHITE)
                                                setPadding(0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, context.resources.displayMetrics).roundToInt(), 0, 0)
                                                layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.CENTER_IN_PARENT) }
                                            })
                                            addView(ImageButton(this@TabsListFragment.requireContext()).apply {
                                                layoutParams = RelativeLayout.LayoutParams(buttonWidth, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                                    addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                                                    addRule(RelativeLayout.CENTER_VERTICAL)
                                                }
                                                setPadding(0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, context.resources.displayMetrics).roundToInt(), 0, 0)
                                                id = R.id.expanded_menu
                                                setImageResource(R.drawable.outline_info_white_24)
                                                setBackgroundColor(Color.TRANSPARENT)
                                                if (Utils.checkSdk(23))
                                                    foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 255, 255, 255)), null, null).apply { radius = buttonWidth / 2 }
                                                setOnClickListener {
                                                    this@TabsListFragment.startActivity(Intent(this@TabsListFragment.requireContext(), AboutActivity::class.java))
                                                }
                                            })
                                        })
                                        addView(RelativeLayout(this@TabsListFragment.requireContext()).apply {
                                            layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, LivestreamView.height.toFloat(), resources.displayMetrics).roundToInt()/*resources.displayMetrics.widthPixels / 4*/).apply {
                                                addRule(RelativeLayout.BELOW, R.id.title)
                                            }
                                            setBackgroundColor(Color.WHITE)
                                            addView(LivestreamView.create(context, this))
                                        })
                                    })
                                }
                                view!!.swipe_refresh_layout.isRefreshing = false
                                val constructed = p0.children.map { Utils.Constructors.constructTab(it.value!!) }
                                initializeAdapter(constructed.filter { it != null }.map { it!! }.filter {
                                    !it.getShouldBeHidden()
                                }, appearance)
                                if (constructed.count { it == null } > 0)
                                    AlertDialog.Builder(requireContext()).setTitle("Error").setMessage("We're having trouble loading content.  Please try again later.  We apologise for the inconvenience.").setPositiveButton("Dismiss", null).create().applyColorsAndTypefaces().show()
                                trace.stop()
                            }
                        })
                    }
                })
            }
        })
    }

    private fun initializeAdapter(tabs: List<AuthenticTab>, appearance: AuthenticAppearance) {
        val toTile: (AuthenticTab) -> Tile<AuthenticTab> = { t ->
            Tile(t.title, false, t.header, t) { tab -> if (tab.action == null) TabActivity.start(requireContext(), tab) else tab.action.invoke(requireContext()) }
        }
        view!!.postDelayed({
            requireActivity().runOnUiThread {
                val ueTile = Tile(appearance.events.title, false, appearance.events.header, appearance.events) { a -> EventListActivity.start(requireActivity(), a) }
                val layout = DualRecyclerView.create(requireActivity(), tabs.filter { t -> t.index % 2 == 0 }.map(toTile), ArrayList<Tile<*>>().apply {
                    add(ueTile)
                    addAll(tabs.filter { t -> t.index % 2 != 0 }.map(toTile))
                }, appearance, requireContext().resources.displayMetrics.heightPixels - view!!.findViewById<View>(R.id.toolbar).height)
                view!!.root.addView(layout)
                layout.animate().setStartDelay(250L).alpha(1f).duration = 250L
            }
        }, 250L)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (!::activity.isInitialized)
            activity = requireActivity()
        view!!.apply {
            Handler().postDelayed({
                activity.runOnUiThread {
                    swipe_refresh_layout.isRefreshing = true
                    loadTabs(false)
                }
            }, 1750L)
            swipe_refresh_layout.apply {
                setOnRefreshListener {
                    loadTabs(true)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tabs_list, container, false)
    }


}
