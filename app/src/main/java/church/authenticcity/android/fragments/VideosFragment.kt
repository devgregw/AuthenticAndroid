package church.authenticcity.android.fragments

import android.animation.Animator
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import church.authenticcity.android.classes.AuthenticTab
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.classes.ImageResource
import church.authenticcity.android.databinding.FragmentVideosBinding
import church.authenticcity.android.helpers.DatabaseHelper
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.views.HalfThumbnailButtonView
import church.authenticcity.android.views.LargeThumbnailButtonView

class VideosFragment : AuthenticFragment<FragmentVideosBinding>() {
    companion object {
        fun getInstance(watchTabId: String) = VideosFragment().apply {
            arguments = Bundle().apply {
                putString("watchTabId", watchTabId)
            }
            setup("VIDEOS", {i, c, a -> FragmentVideosBinding.inflate(i, c, a)})
        }
    }

    private val watchTabId: String
        get() = arguments?.getString("watchTabId", "OPQ26R4SRP") ?: "OPQ26R4SRP"

    override val root: View
        get() = binding.root

    private fun replaceContent(view: View, content: View) {
        binding.nestedScrollView.removeAllViews()
        binding.nestedScrollView.addView(content)
        showContent(view)
    }

    private fun setContent(view: View, content: View) {
        binding.contentList.removeAllViews()
        binding.contentList.addView(content)
        showContent(view)
    }

    private fun showContent(view: View) {
        binding.nestedScrollView.animate().alpha(1f).setDuration(125L).setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                binding.swipeRefreshLayout.isRefreshing = false
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }
        }).start()
    }

    private fun setErrorMessage(view: View, message: String) {
        setContent(view, TextView(view.context).apply {
            text = Utils.makeTypefaceSpan(message, view.context)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(Color.BLACK)
        })
    }

    override fun onCreateView(view: View) {
        binding.swipeRefreshLayout.setOnRefreshListener { onRefresh() }
    }

    private fun populate(view: View, playlistTabs: List<AuthenticTab>, latestVideoImage: String, latestVideoProvider: String, latestVideoId: String) {
        binding.swipeRefreshLayout.isRefreshing = false
        if (binding.contentList.childCount == 2)
            binding.contentList.removeViewAt(0)
        binding.videosListLeft.removeAllViews()
        binding.videosListRight.removeAllViews()
        binding.contentList.addView(LargeThumbnailButtonView(view.context, latestVideoProvider, latestVideoId, "", latestVideoImage, true),0)
        playlistTabs.forEachIndexed { index, authenticTab ->
            val btn = HalfThumbnailButtonView(view.context, "", ImageResource(authenticTab.header.imageName, 1920, 1080), ButtonAction(HashMap<String, Any>().apply {
                put("group", 0)
                put("tabId", authenticTab.id)
                put("type", "OpenTabAction")
            }), true)
            if (index % 2 != 0)
                binding.videosListRight.addView(btn)
            else binding.videosListLeft.addView(btn)
        }
        showContent(view)
    }

    override fun onRefreshView(view: View) {
        binding.nestedScrollView.animate().alpha(0f).setDuration(125L).setListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(p0: Animator?) {
                DatabaseHelper.loadAllTabs(false) {er, tabs ->
                    when {
                        er != null -> setErrorMessage(view, "Error 400: Bad request.  FB#${er.code}: ${er.message}")
                        tabs == null -> setErrorMessage(view, "Error 417: Expectation failed.  No data returned.")
                        else -> {
                            val watchTab = tabs.first { it.id == watchTabId }
                            val playlistTabs = watchTab.convertedElements
                                    .map { if (it.type == "tile")
                                        ButtonAction(it.getProperty("action", HashMap()))
                                    else
                                        ButtonAction((it.getProperty("_buttonInfo", HashMap<String, Any>()))["action"] as HashMap<String, Any>) }
                                    .map { action -> tabs.first { it.id == action.get("tabId") }}
                            val latestVideoInfo = playlistTabs.first { !it.title.contains("worship", true) }.convertedElements[0].getProperty("videoInfo", HashMap<String, Any>())
                            val videoImage = latestVideoInfo["thumbnail"] as String
                            val videoProvider = latestVideoInfo["provider"] as String
                            val videoId = latestVideoInfo["id"] as String
                            populate(view, playlistTabs, videoImage, videoProvider, videoId)
                        }
                    }
                }
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }
        }).start()
    }
}