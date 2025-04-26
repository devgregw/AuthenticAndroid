package church.authenticcity.android.fragments

import android.animation.Animator
import android.content.Context
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

    override val root
        get() = binding?.root

    private fun setContent(content: View) {
        binding?.apply {
            contentList.removeAllViews()
            contentList.addView(content)
        }
        showContent()
    }

    private fun showContent() {
        binding?.nestedScrollView?.animate()?.alpha(1f)?.setDuration(125L)?.setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) {
            }

            override fun onAnimationEnd(p0: Animator) {
                binding?.swipeRefreshLayout?.isRefreshing = false
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationStart(p0: Animator) {
            }
        })?.start()
    }

    private fun setErrorMessage(context: Context, message: String) {
        setContent(TextView(context).apply {
            text = Utils.makeTypefaceSpan(message, context)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(Color.BLACK)
        })
    }

    override fun onCreateView(view: View) {
        binding?.swipeRefreshLayout?.setOnRefreshListener { onRefresh() }
    }

    private fun populate(view: View, playlistTabs: List<AuthenticTab>, latestVideoImage: String, latestVideoProvider: String, latestVideoId: String) {
        binding?.apply {
            swipeRefreshLayout.isRefreshing = false
            if (contentList.childCount == 2)
                contentList.removeViewAt(0)
            videosListLeft.removeAllViews()
            videosListRight.removeAllViews()
            contentList.addView(LargeThumbnailButtonView(view.context, latestVideoProvider, latestVideoId, "", latestVideoImage, true),0)
        }

        playlistTabs.forEachIndexed { index, authenticTab ->
            val btn = HalfThumbnailButtonView(view.context, "", ImageResource(authenticTab.header.imageName, 1920, 1080), ButtonAction(HashMap<String, Any>().apply {
                put("group", 0)
                put("tabId", authenticTab.id)
                put("type", "OpenTabAction")
            }), true)
            if (index % 2 != 0)
                binding?.videosListRight?.addView(btn)
            else binding?.videosListLeft?.addView(btn)
        }
        showContent()
    }

    override fun onRefreshView(view: View) {
        binding?.nestedScrollView?.animate()?.alpha(0f)?.setDuration(125L)?.setListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(p0: Animator) {
                DatabaseHelper.loadAllTabs(false) {er, tabs ->
                    when {
                        er != null -> setErrorMessage(view.context, "Error 400: Bad request.  FB#${er.code}: ${er.message}")
                        tabs == null -> setErrorMessage(view.context, "Error 417: Expectation failed.  No data returned.")
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

            override fun onAnimationRepeat(p0: Animator) {
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationStart(p0: Animator) {
            }
        })?.start()
    }
}