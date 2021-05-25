package church.authenticcity.android.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import church.authenticcity.android.activities.TabbedHomeActivity
import church.authenticcity.android.databinding.FragmentWatchBinding
import church.authenticcity.android.helpers.Utils
import com.google.android.material.tabs.TabLayout

class WatchFragment : AuthenticFragment<FragmentWatchBinding>() {
    companion object {
        fun getInstance(watchTabId: String, title: String, listener: OnFragmentTitleChangeListener?) = WatchFragment().apply {
            arguments = Bundle().apply {
                putString("watchTabId", watchTabId)
            }
            setup(title, {i, c, a -> FragmentWatchBinding.inflate(i, c, a)}, listener)
        }
    }

    private val watchTabId: String
        get() = arguments?.getString("watchTabId", "OPQ26R4SRP") ?: "OPQ26R4SRP"

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
                else -> VideosFragment.getInstance(watchTabId)
            }
        }

        override fun getCount(): Int = if (enable) 2 else 1
    }

    override fun onRefreshView(view: View) {
        try {
            adapter = FragmentAdapter(watchTabId, childFragmentManager)
            binding.watchTabLayout.getTabAt(0)?.select()
            binding.watchViewPager.adapter = adapter
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun initialize(view: View) {
        binding.watchTabLayout.removeAllTabs()
        binding.watchTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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
        } else {
            try {
                initialize(binding.root)
                onRefresh()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onResume()
    }

    override fun onCreateView(view: View) {
        initialize(view)
    }
}