package church.authenticcity.android.fragments

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
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
import church.authenticcity.android.views.HalfThumbnailButtonView
import java.util.Random
import kotlin.math.roundToInt

class TabFragment : AuthenticFragment<FragmentContentBasicBinding>() {
    companion object {
        fun getInstance(tabId: String, title: String, listener: OnFragmentTitleChangeListener?) = TabFragment().apply {
            arguments = Bundle().apply {
                putString("tabId", tabId)
            }
            setup(title, {i, c, a -> FragmentContentBasicBinding.inflate(i, c, a)}, listener)
        }
    }

    private val tabId: String
        get() = arguments?.getString("tabId", "") ?: ""

    override val root
        get() = binding?.root

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
            resource.load(context, itemView.findViewById<ImageView>(R.id.image)!!)
            itemView.setOnClickListener {
                context.startActivity(Intent(context, WallpaperPreviewActivity::class.java).apply {
                    putExtra("imageName", resource.imageName)
                    putExtra("width", resource.width)
                    putExtra("height", resource.height)
                })
            }
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

    private fun setContent(views: Array<View>) {
        binding?.contentList?.removeAllViews()
        views.forEach { binding?.contentList?.addView(it) }
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

    private fun setErrorMessage(message: String) {
        setContent(TextView(requireContext()).apply {
            text = Utils.makeTypefaceSpan(message, requireContext())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(Color.BLACK)
        })
    }

    private fun passwordLoop(view: View, tab: AuthenticTab) {
        val field = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Password"
        }
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(tab.title)
            .setMessage("Enter password to access.")
            .setView(field)
            .setPositiveButton("Done") { _, _ ->
                val pwd = field.text.toString()
                if (tab.verifyPassword(pwd))
                    finishLoading(view, tab)
                else passwordLoop(view, tab)
            }
            .setNegativeButton("Cancel") { _, _ ->
                activity?.finish()
            }
            .create()
            dialog.setOnShowListener {
                val a = it as AlertDialog
                a.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                    setTextColor(Color.WHITE)
                    typeface = Utils.getTextTypeface(a.context)
                }
                a.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                    setTextColor(Color.WHITE)
                    typeface = Utils.getTextTypeface(a.context)
                }
                a.findViewById<TextView>(R.id.alertTitle)?.typeface = Utils.getTitleTypeface(a.context)
                if (field.requestFocus())
                    (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.showSoftInput(field, InputMethodManager.SHOW_FORCED)
            }
            dialog.show()
    }

    private fun finishLoading(view: View, tab: AuthenticTab?) {
        if (tab != null)
            title = tab.title
        when {
            tab == null -> {
                setErrorMessage("Error 404: The page $tabId could not be found.")
            }
            tab.action != null -> {
                tab.action.invoke(view.context)
                if (this@TabFragment.activity is FragmentActivity) {
                    this@TabFragment.activity?.finish()
                }
            }
            tab.specialType != null -> populate(view, tab, tab.specialType)
            else -> setContent(tab.convertedElements.map { it.toView(view.context) }.toTypedArray())
        }
    }

    private fun populate(view: View, tab: AuthenticTab?) {
        Handler(Looper.getMainLooper()).post {
            if (tab == null)
                setErrorMessage("Error 404: The page $tabId could not be found.")
            else {
                title = tab.title
                if (tab.hasPassword)
                    passwordLoop(view, tab)
                else finishLoading(view, tab)
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
                replaceContent(recyclerView)
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
                if (elements.isNotEmpty()) {
                    rootList.addView(AuthenticElement.createThumbnailButton(view.context, ButtonAction.empty, "", tab.header, large = true, hideTitle = true))

                //val info = elements[0].getProperty("videoInfo", HashMap<String, Any>())
                    //rootList.addView(AuthenticElement.createVideo(view.context, info["provider"] as String, info["id"] as String, info["thumbnail"] as String, info["title"] as String, large = true, hideTitle = true))
                }
                //elements.removeAt(0)
                if (elements.isNotEmpty()) {
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
                    replaceContent(rootList)
                    leftList.requestLayout()
                    rightList.requestLayout()
                    list.requestLayout()
                    rootList.requestLayout()
                }
            }
            else -> {
                setErrorMessage("Error 426: The page $tabId could not be loaded because it requires an updated version of the Authentic app.  Please check for updates in the Google Play Store.")
            }
        }
    }

    override fun onRefreshView(view: View) {
        binding?.apply {
            swipeRefreshLayout.isRefreshing = true
            nestedScrollView.animate().alpha(0f).setDuration(125L).setListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(p0: Animator) {
                    DatabaseHelper.loadTab(tabId, false) {er, t ->
                        if (er != null)
                            setErrorMessage("Error 400: Bad request.  FB#${er.code}: ${er.message}")
                        else populate(view, t)
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
    }
}