package church.authenticcity.android

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.PopupMenu
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.*
import church.authenticcity.android.classes.AuthenticAppearance
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.views.PlainCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.roundToInt

class TabListActivity : AppCompatActivity() {
    companion object {
        fun start(parentActivity: Activity) {
            parentActivity.startActivity(Intent(parentActivity, TabListActivity::class.java))
            parentActivity.overridePendingTransition(R.anim.slide_up, R.anim.empty)
        }
    }

    private var root: LinearLayout? = null

    private fun loadTabs() {
        root!!.removeAllViews()
        root!!.addView(RelativeLayout(this).apply {
            addView(ProgressBar(this@TabListActivity).apply {
                isIndeterminate = true
                val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75f, resources.displayMetrics).toInt()
                layoutParams = RelativeLayout.LayoutParams(size, size).apply {
                    addRule(RelativeLayout.CENTER_IN_PARENT)
                }
                indeterminateDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
            })
        })
        val appRef = FirebaseDatabase.getInstance().getReference("/appearance/")
        appRef.keepSynced(true)
        appRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                root!!.removeAllViews()
                root!!.addView(TextView(this@TabListActivity).apply {
                    text = "ERROR: ${p0?.message}"
                    typeface = Utils.getTextTypeface(this@TabListActivity)
                    setTextColor(Color.WHITE)
                })
            }

            override fun onDataChange(p0: DataSnapshot?) {
                val appearance = AuthenticAppearance(p0!!.value as HashMap<String, Any>)
                val tabsRef = FirebaseDatabase.getInstance().getReference("/tabs/")
                tabsRef.keepSynced(true)
                tabsRef.orderByChild("index").addListenerForSingleValueEvent(object : ValueEventListener {
                    @SuppressLint("SetTextI18n")
                    override fun onCancelled(p0: DatabaseError?) {
                        root!!.removeAllViews()
                        root!!.addView(TextView(this@TabListActivity).apply {
                            text = "ERROR: ${p0?.message}"
                            typeface = Utils.getTextTypeface(this@TabListActivity)
                            setTextColor(Color.WHITE)
                        })
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        root!!.apply {
                            removeAllViews()
                            //orientation = LinearLayout.VERTICAL
                            addView(RelativeLayout(this@TabListActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, this@TabListActivity.resources.displayMetrics).roundToInt()
                                setPadding(px, px / 4, px, 0)
                                val buttonWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, resources.displayMetrics).roundToInt()
                                addView(ImageButton(this@TabListActivity).apply {
                                    layoutParams = RelativeLayout.LayoutParams(buttonWidth, ViewGroup.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.ALIGN_PARENT_LEFT) }
                                    setImageResource(R.drawable.ic_keyboard_arrow_down_black_36dp)
                                    setBackgroundColor(Color.TRANSPARENT)
                                    if (Utils.checkSdk(23))
                                        foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(128, 0, 0, 0)), null, null).apply { radius = buttonWidth / 2 }
                                    setOnClickListener { onBackPressed() }
                                })
                                addView(TextView(this@TabListActivity).apply {
                                    text = Utils.makeTypefaceSpan("HOME", Utils.getTitleTypeface(this@TabListActivity))
                                    textSize = 30f
                                    setTextColor(Color.BLACK)
                                    layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.CENTER_IN_PARENT) }
                                })
                                addView(ImageButton(this@TabListActivity).apply {
                                    layoutParams = RelativeLayout.LayoutParams(buttonWidth, ViewGroup.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.ALIGN_PARENT_RIGHT) }
                                    id = R.id.expanded_menu
                                    setImageResource(R.drawable.ic_more_vert_black_36dp)
                                    setBackgroundColor(Color.TRANSPARENT)
                                    if (Utils.checkSdk(23))
                                        foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, null).apply { radius = buttonWidth / 2 }
                                    val menu = PopupMenu(this@TabListActivity, this)
                                    menu.inflate(R.menu.menu_tab_list_popup)
                                    menu.menu.getItem(0).setTitle(Utils.makeTypefaceSpan("About", Utils.getTextTypeface(this@TabListActivity)))
                                    menu.menu.getItem(1).setTitle(Utils.makeTypefaceSpan("Settings", Utils.getTextTypeface(this@TabListActivity)))
                                    menu.setOnMenuItemClickListener { item ->
                                        var handled = true
                                        when (item.itemId) {
                                            R.id.popupMenu_about -> this@TabListActivity.startActivity(Intent(this@TabListActivity, AboutActivity::class.java))
                                            R.id.popupMenu_settings -> this@TabListActivity.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.parse("package:${this@TabListActivity.packageName}")
                                            })
                                            else -> handled = false
                                        }
                                        handled
                                    }
                                    setOnClickListener {
                                        menu.show()
                                    }
                                })
                                addView(ImageButton(this@TabListActivity).apply {
                                    layoutParams = RelativeLayout.LayoutParams(buttonWidth, ViewGroup.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.LEFT_OF, R.id.expanded_menu) }
                                    setImageResource(R.drawable.ic_refresh_black_36dp)
                                    setBackgroundColor(Color.TRANSPARENT)
                                    if (Utils.checkSdk(23))
                                        foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, null).apply { radius = buttonWidth / 2 }
                                    setOnClickListener { loadTabs() }
                                })
                            })
                        }
                        root!!.addView(PlainCardView(this@TabListActivity, appearance.events.header, if (appearance.events.hideTitle) "" else appearance.events.title, { EventListActivity.start(this@TabListActivity, appearance.events) }))
                        p0?.children?.map { Utils.Constructors.constructTab(it.value!!) }?.filter {
                            !it.getShouldBeHidden()
                        }?.forEach {
                            root!!.addView(PlainCardView(this@TabListActivity, it))
                            Utils.Temp.putTab(it)
                        }
                        //layout!!.addView(layout)
                    }
                })
            }
        })
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.empty, R.anim.slide_down)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = intent.getStringExtra("title")
        setContentView(R.layout.activity_tab_list)
        root = findViewById(R.id.content_list)
        loadTabs()
    }
}
