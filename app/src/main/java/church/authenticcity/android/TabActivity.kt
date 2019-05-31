package church.authenticcity.android

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import church.authenticcity.android.classes.AuthenticElement
import church.authenticcity.android.classes.AuthenticTab
import church.authenticcity.android.classes.ImageResource
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyColorsAndTypefaces
import church.authenticcity.android.helpers.applyTypeface
import church.authenticcity.android.views.LoadingIndicatorImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_content_basic.*
import java.util.*
import kotlin.math.roundToInt

class TabActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context, tab: AuthenticTab) {
            Utils.Temp.putTab(tab)
            context.startActivity(Intent(context, TabActivity::class.java).apply { putExtra("id", tab.id) })
        }

        private fun makePath(path: String) = (if (AuthenticApplication.useDevelopmentDatabase) "/dev" else "") + path

        fun start(context: Context, tabId: String) {
            val tab = Utils.Temp.getTab(tabId)
            if (tab != null) {
                start(context, tab)
                return
            }
            val dialog = Utils.createIndeterminateDialog(context, "Loading...")
            dialog.show()
            FirebaseDatabase.getInstance().getReference(makePath("/tabs/$tabId")).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    dialog.dismiss()
                    AlertDialog.Builder(context).setTitle("Unexpected Error").setCancelable(false).setMessage("An unexpected error occurred while loading data.\n\nCode: ${p0.code}\nMessage: ${p0.message}\nDetails: ${p0.details}").setPositiveButton("Dismiss", null).create().applyColorsAndTypefaces().show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    dialog.dismiss()
                    if (p0.value == null) {
                        AlertDialog.Builder(context).setTitle("Error").setCancelable(false).setMessage("We were unable to open the page because it does not exist.").setPositiveButton("Dismiss", null).create().applyColorsAndTypefaces().show()
                        return
                    }
                    start(context, Utils.Constructors.constructTab(p0.value!!)!!)
                }
            })
        }
    }

    private fun setContent(vararg views: View) = setContent(views.toList())

    private fun setContent(views: List<View>) {
        findViewById<LinearLayout>(R.id.content_list).apply {
            removeAllViews()
            views.forEach { addView(it) }
            addView(AuthenticElement.createSeparator(this@TabActivity, false))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> false
        }
    }

    private lateinit var tab: AuthenticTab

    private fun initialize() {
        val uncheckedTab = Utils.Temp.getTab(intent.getStringExtra("id"))
        if (uncheckedTab == null) {
            AlertDialog.Builder(this).setTitle("Not Found").setMessage("The tab ${intent.getStringExtra("id")} could not be loaded.  It may have been deleted.").setPositiveButton("Dismiss") { _, _ ->
                this@TabActivity.finish()
            }.create().applyColorsAndTypefaces().show()
            return
        }
        tab = uncheckedTab
        this.title = tab.title
        supportActionBar?.applyTypeface(this, tab.title)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        findViewById<TextView>(R.id.toolbar_title).apply {
            typeface = Utils.getTitleTypeface(this@TabActivity)
            text = tab.title
        }
        findViewById<ImageView>(R.id.image).visibility = View.GONE
        if (tab.specialType != null) {
            initialize(tab.specialType!!)
            return
        }
        if (tab.elementCount > 0)
            setContent(tab.convertedElements.map { it.toView(this@TabActivity) })
        else
            setContent(RelativeLayout(this@TabActivity).apply {
                addView(TextView(this@TabActivity).apply {
                    textSize = 22f
                    text = this@TabActivity.getString(R.string.no_content)
                    setTextColor(Color.BLACK)
                    typeface = Utils.getTextTypeface(this@TabActivity)
                    layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.CENTER_IN_PARENT) }
                })
            })
    }

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
            //Utils.loadFirebaseImage(context, resource.imageName, itemView.findViewById(R.id.image) as ImageView)
            resource.load(context, itemView.findViewById(R.id.image) as ImageView)
            itemView.setOnClickListener {
                /*AlertDialog.Builder(context).setCancelable(true).setTitle("Preview").setPositiveButton("Save") { _, _ ->
                    resource.saveToGallery(context)
                }.setNeutralButton("Back", null).setView(LoadingIndicatorImageView.create(context, resource, LoadingIndicatorImageView.ANCHOR_CENTER, ImageView.ScaleType.CENTER_INSIDE, false, LoadingIndicatorImageView.WRAP_CONTENT, LoadingIndicatorImageView.WRAP_CONTENT)).create().applyColorsAndTypefaces().show()*/
                context.startActivity(Intent(context, WallpaperPreviewActivity::class.java).apply {
                    putExtra("imageName", resource.imageName)
                    putExtra("width", resource.width)
                    putExtra("height", resource.height)
                })
            }
        }
    }

    private fun initialize(specialType: String) {
        when (specialType) {
            "wallpapers" -> {
                val recyclerView = RecyclerView(this)
                recyclerView.isNestedScrollingEnabled = false
                recyclerView.layoutManager = GridLayoutManager(this, 2)
                recyclerView.adapter = object : RecyclerView.Adapter<WallpaperViewHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder = WallpaperViewHolder(this@TabActivity)

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
                setContent(recyclerView)
            }
            "fullexp" -> {
                coordinator_layout.apply {
                    removeViewAt(0)
                    addView(tab.convertedElements[0].toView(this@TabActivity), ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_basic)
        findViewById<Toolbar>(R.id.toolbar).apply {
            this@TabActivity.setSupportActionBar(this)
            setBackgroundColor(Color.parseColor("#212121"))
        }
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left_white_36dp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.applyTypeface(this, "")
        initialize()
    }
}
