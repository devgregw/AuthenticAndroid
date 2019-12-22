package church.authenticcity.android.fragments

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import church.authenticcity.android.R
import church.authenticcity.android.activities.TabbedHomeActivity
import church.authenticcity.android.helpers.Utils
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_watch.view.*

class WatchFragment(private val id: String, title: String, listener: OnFragmentTitleChangeListener?) : AuthenticFragment(title, R.layout.fragment_watch, listener) {
    private var adapter: FragmentAdapter? = null

    class FragmentAdapter(private val watchTabId: String, manager: FragmentManager) : FragmentStatePagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private var enable: Boolean = TabbedHomeActivity.appearance.livestream.enable

        var livestreamFragment: LivestreamFragment? = null

        override fun getItem(position: Int): Fragment {
            return when (position) {
                1 -> {
                    livestreamFragment = LivestreamFragment()
                    livestreamFragment!!
                }
                else -> VideosFragment(watchTabId)
            }
        }

        override fun getCount(): Int = if (enable) 2 else 1
    }

    override fun onRefreshView(view: View) {
        adapter = FragmentAdapter(id, childFragmentManager)
        view.watch_tab_layout.getTabAt(0)?.select()
        view.watch_view_pager.adapter = adapter
    }

    private fun initialize(view: View) {
        view.watch_tab_layout.removeAllTabs()
        view.watch_tab_layout.addOnTabSelectedListener(object : TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
            override fun onTabSelected(p0: TabLayout.Tab?) {
                view.watch_view_pager.setCurrentItem(p0?.position ?: 0, true)
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }
        })
        view.watch_tab_layout.addTab(view.watch_tab_layout.newTab().setText(Utils.makeTypefaceSpan("VIDEOS", view.context)), true)
        view.watch_tab_layout.addTab(view.watch_tab_layout.newTab().setText(Utils.makeTypefaceSpan("LIVE", view.context)))
        view.isNestedScrollingEnabled = true
        view.watch_view_pager.isNestedScrollingEnabled = true
        view.watch_view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                view.watch_tab_layout.getTabAt(position)?.select()
                if (position == 1)
                    adapter?.livestreamFragment?.checkLivestreamStatus()
                else adapter?.livestreamFragment?.cancel()
            }
        })
        if (!TabbedHomeActivity.appearance.livestream.enable)
            view.watch_tab_layout.visibility = View.GONE
    }

    private var stopped = false

    override fun onStop() {
        stopped = true
        adapter = null
        super.onStop()
    }

    override fun onResume() {
        if (stopped) {
            stopped = false
            onRefresh()
        }
        super.onResume()
    }

    override fun onCreateView(view: View) {
        initialize(view)
    }
}