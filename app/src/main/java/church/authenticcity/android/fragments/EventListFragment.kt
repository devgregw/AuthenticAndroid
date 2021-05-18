package church.authenticcity.android.fragments


import android.animation.Animator
import android.graphics.Color
import android.os.Handler
import android.os.Looper
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
import church.authenticcity.android.classes.AuthenticEventPlaceholder
import church.authenticcity.android.databinding.FragmentContentBasicBinding
import church.authenticcity.android.helpers.DatabaseHelper
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.views.recyclerView.Tile
import church.authenticcity.android.views.recyclerView.TileAdapter
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
class EventListFragment() : AuthenticFragment<FragmentContentBasicBinding>() {
    companion object {
        fun getInstance() = EventListFragment()
    }

    override val root: View
        get() = binding.root

    private fun setContent(view: View, content: View) {
        binding.contentList.removeAllViews()
        binding.contentList.addView(content)
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

    private fun replaceContent(view: View, content: View) {
        binding.nestedScrollView.removeAllViews()
        binding.nestedScrollView.addView(content)
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

    private fun populate(view: View, events: Array<AuthenticEvent>) {
        val createHandler: (AuthenticEventPlaceholder) -> ((e: AuthenticEventPlaceholder) -> Unit) = {
            when {
                it.action != null -> { e -> e.action!!.invoke(requireActivity()) }
                it.canOpen -> { e -> FragmentActivity.startEvent(requireActivity(), e.id, e.title) }
                else -> { _ -> }
            }
        }
        val tiles = events.filterIsInstance<AuthenticEventPlaceholder>().filter { it.isVisible }.sortedBy { it.index }.map { Tile(it.title, it.hideTitle, it.header, it, createHandler(it)) }.toList() + events.filter { it !is AuthenticEventPlaceholder }.filter { it.isVisible }.sortedBy { it.getNextOccurrence().startDate.toEpochSecond() }.map { Tile(it.title, it.hideTitle, it.header, it) { e -> FragmentActivity.startEvent(view.context, e.id, e.title) } }
        Handler(Looper.getMainLooper()).post {
            binding.nestedScrollView.removeAllViews()
            val recyclerView = RecyclerView(view.context)
            recyclerView.adapter = TileAdapter(this@EventListFragment.requireActivity(), tiles, true, false, 0)
            recyclerView.layoutManager = LinearLayoutManager(view.context)
            recyclerView.addItemDecoration(DividerItemDecoration(view.context, (recyclerView.layoutManager as LinearLayoutManager).orientation))
            replaceContent(view, LinearLayout(view.context).apply {
                addView(recyclerView)
                tag = "recyclerViewHost"
                orientation = LinearLayout.VERTICAL
            })
        }
    }

    override fun onRefreshView(view: View) {
        binding.swipeRefreshLayout.isRefreshing = true
        binding.nestedScrollView.animate().alpha(0f).setDuration(125L).setListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(p0: Animator?) {
                DatabaseHelper.loadAllEvents(false) {er, ev ->
                    if (er != null)
                        setErrorMessage(view, "Error 400: Bad request.  FB#${er.code}: ${er.message}")
                    else populate(view, ev?.toTypedArray() ?: ArrayList<AuthenticEvent>().toTypedArray())
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

    override fun onCreateView(view: View) {
        binding.swipeRefreshLayout.setOnRefreshListener { onRefresh() }
        view.setBackgroundResource(R.color.colorBackground)
        //onRefreshView(view)
    }

    private var stopped = false

    override fun onStop() {
        stopped = true
        super.onStop()
    }

    override fun onResume() {
        if (stopped) {
            stopped = false
            onRefreshView(binding.root)
        } else {
            try {
                onCreateView(binding.root)
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
