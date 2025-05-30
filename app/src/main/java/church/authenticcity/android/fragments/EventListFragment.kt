package church.authenticcity.android.fragments


import android.animation.Animator
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import church.authenticcity.android.R
import church.authenticcity.android.activities.FragmentActivity
import church.authenticcity.android.classes.AuthenticEvent
import church.authenticcity.android.classes.AuthenticCustomEvent
import church.authenticcity.android.databinding.FragmentContentBasicBinding
import church.authenticcity.android.helpers.DatabaseHelper
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.views.recyclerView.Tile
import church.authenticcity.android.views.recyclerView.TileAdapter

/**
 * A simple [Fragment] subclass.
 */
class EventListFragment : AuthenticFragment<FragmentContentBasicBinding>() {
    companion object {
        fun getInstance() = EventListFragment()
    }

    override val root
        get() = binding?.root

    private fun setContent(content: View) {
        binding?.apply {
            contentList.removeAllViews()
            contentList.addView(content)
            nestedScrollView.animate().alpha(1f).setDuration(125L).setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator) {
                }

                override fun onAnimationEnd(p0: Animator) {
                    binding?.swipeRefreshLayout?.isRefreshing = false
                }

                override fun onAnimationCancel(p0: Animator) {
                }

                override fun onAnimationStart(p0: Animator) {
                }
            }).start()
        }
    }

    private fun replaceContent(content: View) {
        binding?.apply {
            nestedScrollView.removeAllViews()
            nestedScrollView.addView(content)
            nestedScrollView.animate().alpha(1f).setDuration(125L).setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator) {
                }

                override fun onAnimationEnd(p0: Animator) {
                    binding?.swipeRefreshLayout?.isRefreshing = false
                }

                override fun onAnimationCancel(p0: Animator) {
                }

                override fun onAnimationStart(p0: Animator) {
                }
            }).start()
        }
    }

    private fun setErrorMessage(view: View, message: String) {
        setContent(TextView(view.context).apply {
            text = Utils.makeTypefaceSpan(message, view.context)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(Color.BLACK)
        })
    }

    private fun populate(view: View, events: Array<AuthenticEvent>) {
        val createHandler: (AuthenticCustomEvent) -> ((e: AuthenticCustomEvent) -> Unit) = {
            when {
                it.action != null -> { e -> e.action!!.invoke(requireActivity()) }
                it.canOpen -> { e -> FragmentActivity.startEvent(requireActivity(), e.id, e.title) }
                else -> { _ -> }
            }
        }
        val tiles = events.filterIsInstance<AuthenticCustomEvent>().filter { it.isVisible }.sortedBy { it.index }.map { Tile(it.title, it.hideTitle, it.header, it, createHandler(it)) }.toList() + events.filter { it !is AuthenticCustomEvent }.filter { it.isVisible }.sortedBy { it.getNextOccurrence().startDate.toEpochSecond() }.map { Tile(it.title, it.hideTitle, it.header, it) { e -> FragmentActivity.startEvent(view.context, e.id, e.title) } }
        Handler(Looper.getMainLooper()).post {
            binding?.nestedScrollView?.removeAllViews()
            val recyclerView = RecyclerView(view.context)
            recyclerView.adapter = TileAdapter(this@EventListFragment.requireActivity(), tiles, fullWidth = true, fillColumn = false, 0)
            recyclerView.layoutManager = LinearLayoutManager(view.context)
            recyclerView.addItemDecoration(DividerItemDecoration(view.context, (recyclerView.layoutManager as LinearLayoutManager).orientation))
            replaceContent(LinearLayout(view.context).apply {
                addView(recyclerView)
                tag = "recyclerViewHost"
                orientation = LinearLayout.VERTICAL
            })
        }
    }

    override fun onRefreshView(view: View) {
        binding?.apply {
            swipeRefreshLayout.isRefreshing = true
            nestedScrollView.animate().alpha(0f).setDuration(125L).setListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(p0: Animator) {
                    DatabaseHelper.loadAllEvents(false) {er, ev ->
                        if (er != null)
                            setErrorMessage(view, "Error 400: Bad request.  FB#${er.code}: ${er.message}")
                        else populate(view, ev?.toTypedArray() ?: ArrayList<AuthenticEvent>().toTypedArray())
                    }
                }

                override fun onAnimationRepeat(p0: Animator) {
                }

                override fun onAnimationCancel(p0: Animator) {
                }

                override fun onAnimationStart(p0: Animator) {
                }
            }).start()
        }
    }

    override fun onCreateView(view: View) {
        binding?.swipeRefreshLayout?.setOnRefreshListener { onRefresh() }
        view.setBackgroundResource(R.color.colorBackground)
    }

    private var stopped = false

    override fun onStop() {
        stopped = true
        super.onStop()
    }

    override fun onResume() {
        if (root == null) {
            Log.w("EventListFragment", "Unable to resume: root view is null")
            return
        }
        if (stopped) {
            stopped = false
            onRefreshView(root!!)
        } else {
            try {
                onCreateView(root!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onResume()
    }

    init {
        setup("UPCOMING EVENTS", { i, c, a -> FragmentContentBasicBinding.inflate(i, c, a)})
    }
}
