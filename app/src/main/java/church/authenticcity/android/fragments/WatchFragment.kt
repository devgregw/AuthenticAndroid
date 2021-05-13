package church.authenticcity.android.fragments

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import church.authenticcity.android.activities.TabbedHomeActivity
import church.authenticcity.android.databinding.FragmentWatchBinding
import church.authenticcity.android.helpers.Utils
import com.google.android.material.tabs.TabLayout

class WatchFragment(private val id: String, title: String, listener: OnFragmentTitleChangeListener?) : AuthenticFragment<FragmentWatchBinding>(title, {i, c, a -> FragmentWatchBinding.inflate(i, c, a)}, listener) {
    override val root: View
        get() = binding.root
    
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
        binding.watchTabLayout.getTabAt(0)?.select()
        binding.watchViewPager.adapter = adapter
    }

    private fun initialize(view: View) {
        binding.watchTabLayout.removeAllTabs()
        binding.watchTabLayout.addOnTabSelectedListener(object : TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
            override fun onTabSelected(p0: TabLayout.Tab?) {
                binding.watchViewPager.setCurrentItem(p0?.position ?: 0, true)
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }
        })
        binding.watchTabLayout.addTab(binding.watchTabLayout.newTab().setText(Utils.makeTypefaceSpan("VIDEOS", view.context)), true)
        binding.watchTabLayout.addTab(binding.watchTabLayout.newTab().setText(Utils.makeTypefaceSpan("LIVE", view.context)))
        view.isNestedScrollingEnabled = true
        binding.watchViewPager.isNestedScrollingEnabled = true
        binding.watchViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                binding.watchTabLayout.getTabAt(position)?.select()
                if (position == 1)
                    adapter?.livestreamFragment?.checkLivestreamStatus()
                else adapter?.livestreamFragment?.cancel()
            }
        })
        if (!TabbedHomeActivity.appearance.livestream.enable)
            binding.watchTabLayout.visibility = View.GONE
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