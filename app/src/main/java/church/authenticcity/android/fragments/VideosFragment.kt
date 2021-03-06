package church.authenticcity.android.fragments

import android.animation.Animator
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import church.authenticcity.android.R
import church.authenticcity.android.classes.AuthenticTab
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.classes.ImageResource
import church.authenticcity.android.helpers.DatabaseHelper
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.views.HalfThumbnailButtonView
import church.authenticcity.android.views.LargeThumbnailButtonView
import kotlinx.android.synthetic.main.fragment_videos.view.*

class VideosFragment(private val watchTabId: String) : AuthenticFragment("VIDEOS", R.layout.fragment_videos,null) {
    constructor() : this("OPQ26R4SRP")

    private fun replaceContent(view: View, content: View) {
        view.nested_scroll_view.removeAllViews()
        view.nested_scroll_view.addView(content)
        showContent(view)
    }

    private fun setContent(view: View, content: View) {
        view.content_list.removeAllViews()
        view.content_list.addView(content)
        showContent(view)
    }

    private fun showContent(view: View) {
        view.nested_scroll_view.animate().alpha(1f).setDuration(125L).setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                view.swipe_refresh_layout.isRefreshing = false
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
        view.swipe_refresh_layout.setOnRefreshListener { onRefresh() }
    }

    private fun populate(view: View, playlistTabs: List<AuthenticTab>, latestVideoImage: String, latestVideoProvider: String, latestVideoId: String) {
        view.swipe_refresh_layout.isRefreshing = false
        if (view.content_list.childCount == 2)
            view.content_list.removeViewAt(0)
        view.videos_list_left.removeAllViews()
        view.videos_list_right.removeAllViews()
        view.content_list.addView(LargeThumbnailButtonView(view.context, latestVideoProvider, latestVideoId, "", latestVideoImage, true),0)
        playlistTabs.forEachIndexed { index, authenticTab ->
            val btn = HalfThumbnailButtonView(view.context, "", ImageResource(authenticTab.header.imageName, 1920, 1080), ButtonAction(HashMap<String, Any>().apply {
                put("group", 0)
                put("tabId", authenticTab.id)
                put("type", "OpenTabAction")
            }), true)
            if (index % 2 != 0)
                view.videos_list_right.addView(btn)
            else view.videos_list_left.addView(btn)
        }
        showContent(view)
    }

    override fun onRefreshView(view: View) {
        view.nested_scroll_view.animate().alpha(0f).setDuration(125L).setListener(object : Animator.AnimatorListener {
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