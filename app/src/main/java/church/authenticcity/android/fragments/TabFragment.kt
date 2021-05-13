package church.authenticcity.android.fragments

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import church.authenticcity.android.R
import church.authenticcity.android.activities.FragmentActivity
import church.authenticcity.android.activities.WallpaperPreviewActivity
import church.authenticcity.android.classes.AuthenticElement
import church.authenticcity.android.classes.AuthenticTab
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.classes.ImageResource
import church.authenticcity.android.databinding.FragmentContentBasicBinding
import church.authenticcity.android.helpers.DatabaseHelper
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyColorsAndTypefaces
import church.authenticcity.android.views.HalfThumbnailButtonView
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class TabFragment(private val tabId: String, title: String, listener: OnFragmentTitleChangeListener?) : AuthenticFragment<FragmentContentBasicBinding>(title, {i, c, a -> FragmentContentBasicBinding.inflate(i, c, a)}, listener) {
    override val root: View
        get() = binding.root
    
    constructor() : this("error500", "Error 500", null)

    private class WallpaperViewHolder(private val context: Context) : RecyclerView.ViewHolder(RelativeLayout(context).apply {
        val rand = Random().nextInt(256)
        val size = context.resources.displayMetrics.widthPixels / 2
        layoutParams = RecyclerView.LayoutParams(size, size)
        setBackgroundColor(Color.argb(255, rand, rand, rand))
        addView(ProgressBar(context).apply {
            val psize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics).roundToInt()
            isIndeterminate = true
            indeterminateTintList = ColorStateList.valueOf(Color.argb(255, 255 - rand, 255 - rand, 255 - rand))
            layoutParams = RelativeLayout.LayoutParams(psize, psize).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT)
            }
        })
        addView(ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            id = R.id.image
        })
    }) {

        fun initialize(resource: ImageResource) {
            resource.load(context, itemView.findViewById(R.id.image) as ImageView)
            itemView.setOnClickListener {
                context.startActivity(Intent(context, WallpaperPreviewActivity::class.java).apply {
                    putExtra("imageName", resource.imageName)
                    putExtra("width", resource.width)
                    putExtra("height", resource.height)
                })
            }
        }
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

    private fun setContent(view: View, views: Array<View>) {
        binding.contentList.removeAllViews()
        views.forEach { binding.contentList.addView(it) }
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

    private fun populate(view: View, tab: AuthenticTab?) {
        Handler(Looper.getMainLooper()).post {
            if (tab != null)
                title = tab.title
            when {
                tab == null -> {
                    setErrorMessage(view, "Error 404: The page $tabId could not be found.")
                }
                tab.action != null -> {
                    tab.action.invoke(view.context)
                    if (this@TabFragment.activity is FragmentActivity) {
                        this@TabFragment.activity?.finish()
                    }
                }
                tab.specialType != null -> populate(view, tab, tab.specialType)
                else -> setContent(view, tab.convertedElements.map { it.toView(view.context) }.toTypedArray())
            }
        }
    }

    private fun populate(view: View, tab: AuthenticTab, specialType: String) {
        when (specialType) {
            "wallpapers" -> {
                val recyclerView = RecyclerView(view.context)
                recyclerView.isNestedScrollingEnabled = false
                recyclerView.layoutManager = GridLayoutManager(view.context, 2)
                recyclerView.adapter = object : RecyclerView.Adapter<WallpaperViewHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder = WallpaperViewHolder(view.context)

                    override fun getItemCount(): Int = tab.elementCount

                    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
                        val element = tab.convertedElements[position]
                        holder.initialize(ImageResource(element.getProperty("image", HashMap<String, Any>().apply {
                            put("name", "unknown.png")
                            put("width", 720)
                            put("height", 1080)
                        })))
                    }
                }
                replaceContent(view, recyclerView)
            }
            "watchPlaylist" -> {
                val elements = ArrayList(tab.convertedElements)
                val rootList = LinearLayout(view.context).apply {
                    orientation = LinearLayout.VERTICAL
                }
                val list = LinearLayout(view.context).apply {
                    weightSum = 1f
                    orientation = LinearLayout.HORIZONTAL
                }
                if (elements.count() > 0) {
                    rootList.addView(AuthenticElement.createThumbnailButton(view.context, ButtonAction.empty, "", tab.header, large = true, hideTitle = true))

                //val info = elements[0].getProperty("videoInfo", HashMap<String, Any>())
                    //rootList.addView(AuthenticElement.createVideo(view.context, info["provider"] as String, info["id"] as String, info["thumbnail"] as String, info["title"] as String, large = true, hideTitle = true))
                }
                //elements.removeAt(0)
                if (elements.count() > 0) {
                    val leftList = LinearLayout(view.context).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                            weight = 0.5f
                        }
                    }
                    val rightList = LinearLayout(view.context).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                            weight = 0.5f
                        }
                    }
                    elements.filterIndexed { i, _ -> i % 2 == 0 }.forEach { e ->
                        val info = e.getProperty("videoInfo", HashMap<String, Any>())
                        leftList.addView(HalfThumbnailButtonView(view.context, info["provider"] as String, info["id"] as String, info["title"] as String, info["thumbnail"] as String, true))
                    }
                    elements.filterIndexed { i, _ -> i % 2 != 0 }.forEach { e ->
                        val info = e.getProperty("videoInfo", HashMap<String, Any>())
                        rightList.addView(HalfThumbnailButtonView(view.context, info["provider"] as String, info["id"] as String, info["title"] as String, info["thumbnail"] as String, true))
                    }
                    list.addView(leftList)
                    list.addView(rightList)
                    rootList.addView(list)
                    replaceContent(view, rootList)
                    leftList.requestLayout()
                    rightList.requestLayout()
                    list.requestLayout()
                    rootList.requestLayout()
                }
            }
            else -> {
                setErrorMessage(view, "Error 426: The page $tabId could not be loaded because it requires an updated version of the Authentic app.  Please check for updates in the Google Play Store.")
            }
        }
    }

    override fun onRefreshView(view: View) {
        binding.swipeRefreshLayout.isRefreshing = true
        binding.nestedScrollView.animate().alpha(0f).setDuration(125L).setListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(p0: Animator?) {
                DatabaseHelper.loadTab(tabId, false) {er, t ->
                    if (er != null)
                        setErrorMessage(view, "Error 400: Bad request.  FB#${er.code}: ${er.message}")
                    else populate(view, t)
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
    }
}