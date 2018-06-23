package church.authenticcity.android.fragments


import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.PopupMenu
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
import kotlinx.android.synthetic.main.fragment_tabs_list.view.*
import java.util.*
import kotlin.math.roundToInt

class TabsListFragment : Fragment() {
    companion object {
        fun create(activity: Activity) = TabsListFragment().apply { this.activity = activity }
    }

    private lateinit var activity: Activity

    private fun loadTabs() {
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
                                        addView(RelativeLayout(this@TabsListFragment.requireContext()).apply {
                                            setPadding(px, px / 4, px, 0)
                                            layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                            id = R.id.title
                                            addView(ImageButton(this@TabsListFragment.requireContext()).apply {
                                                layoutParams = RelativeLayout.LayoutParams(buttonWidth, ViewGroup.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.ALIGN_PARENT_LEFT) }
                                                setImageResource(R.drawable.ic_keyboard_arrow_down_black_36dp)
                                                setBackgroundColor(Color.TRANSPARENT)
                                                if (Utils.checkSdk(23))
                                                    foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(128, 0, 0, 0)), null, null).apply { radius = buttonWidth / 2 }
                                                setOnClickListener { (this@TabsListFragment.requireActivity() as HomeActivity).goHome() }
                                            })
                                            addView(TextView(this@TabsListFragment.requireContext()).apply {
                                                text = Utils.makeTypefaceSpan("HOME", Utils.getTitleTypeface(this@TabsListFragment.requireContext()))
                                                textSize = 30f
                                                setTextColor(Color.BLACK)
                                                layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.CENTER_IN_PARENT) }
                                            })
                                            addView(ImageButton(this@TabsListFragment.requireContext()).apply {
                                                layoutParams = RelativeLayout.LayoutParams(buttonWidth, ViewGroup.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.ALIGN_PARENT_RIGHT) }
                                                id = R.id.expanded_menu
                                                setImageResource(R.drawable.ic_more_vert_black_36dp)
                                                setBackgroundColor(Color.TRANSPARENT)
                                                if (Utils.checkSdk(23))
                                                    foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, null).apply { radius = buttonWidth / 2 }
                                                val menu = PopupMenu(this@TabsListFragment.requireContext(), this)
                                                menu.inflate(R.menu.menu_tab_list_popup)
                                                menu.menu.getItem(0).title = Utils.makeTypefaceSpan("About", Utils.getTextTypeface(this@TabsListFragment.requireContext()))
                                                menu.menu.getItem(1).title = Utils.makeTypefaceSpan("Settings", Utils.getTextTypeface(this@TabsListFragment.requireContext()))
                                                menu.setOnMenuItemClickListener { item ->
                                                    var handled = true
                                                    when (item.itemId) {
                                                        R.id.popupMenu_about -> this@TabsListFragment.startActivity(Intent(this@TabsListFragment.requireContext(), AboutActivity::class.java))
                                                        R.id.popupMenu_settings -> this@TabsListFragment.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                            data = Uri.parse("package:${this@TabsListFragment.requireContext().packageName}")
                                                        })
                                                        else -> handled = false
                                                    }
                                                    handled
                                                }
                                                setOnClickListener {
                                                    menu.show()
                                                }
                                            })
                                        })
                                        addView(RelativeLayout(this@TabsListFragment.requireContext()).apply {
                                            layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (resources.displayMetrics.widthPixels * 0.4).roundToInt()).apply {
                                                addRule(RelativeLayout.BELOW, R.id.title)
                                            }
                                            setBackgroundColor(Color.BLACK)
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
                            }
                        })
                    }
                })
            }
        })
    }

    private fun initializeAdapter(tabs: List<AuthenticTab>, appearance: AuthenticAppearance) {
        val toTile: (AuthenticTab) -> Tile<AuthenticTab> = { t ->
            Tile(t.title, t.header, t) { tab -> if (tab.action == null) TabActivity.start(requireContext(), tab) else tab.action.invoke(requireContext()) }
        }
        requireActivity().runOnUiThread {
            val ueTile = Tile(appearance.events.title, appearance.events.header, appearance.events) { a -> EventListActivity.start(requireActivity(), a) }
            val layout = DualRecyclerView.create(requireActivity(), ArrayList<Tile<*>>().apply {
                add(ueTile)
                addAll(tabs.filterIndexed { i, _ -> i % 2 != 0 }.map(toTile))
            }, tabs.filterIndexed { i, _ -> i % 2 == 0 }.map(toTile))
            view!!.root.addView(layout)
            layout.animate().setStartDelay(250L).alpha(1f).duration = 250L
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (!::activity.isInitialized)
            activity = requireActivity()
        view!!.apply {
            Handler().postDelayed({
                activity.runOnUiThread {
                    swipe_refresh_layout.isRefreshing = true
                    loadTabs()
                }
            }, 1750L)
            swipe_refresh_layout.apply {
                setOnRefreshListener {
                    loadTabs()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tabs_list, container, false)
    }


}
