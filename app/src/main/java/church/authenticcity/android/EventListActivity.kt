package church.authenticcity.android

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import church.authenticcity.android.classes.AuthenticAppearance
import church.authenticcity.android.classes.AuthenticElement
import church.authenticcity.android.classes.AuthenticEventPlaceholder
import church.authenticcity.android.helpers.SimpleAnimatorListener
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyColorsAndTypefaces
import church.authenticcity.android.helpers.applyTypeface
import church.authenticcity.android.views.recyclerView.Tile
import church.authenticcity.android.views.recyclerView.TileAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.perf.FirebasePerformance
import kotlinx.android.synthetic.main.activity_event_list.*

class EventListActivity : AppCompatActivity() {

    companion object {
        fun start(parentActivity: Activity, appearance: AuthenticAppearance.Events) {
            parentActivity.startActivity(Intent(parentActivity, EventListActivity::class.java).apply { putExtra("title", appearance.title) })
            //parentActivity.overridePendingTransition(R.anim.slide_up, R.anim.empty)
        }
    }

    private fun loadEvents(refreshed: Boolean) {
        val trace = FirebasePerformance.startTrace("load events")
        if (refreshed)
            trace.incrementMetric("refresh events", 1L)
        val eventsRef = FirebaseDatabase.getInstance().getReference("/events/")
        eventsRef.keepSynced(true)
        eventsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onCancelled(p0: DatabaseError) {
                root.removeAllViews()
                root.addView(TextView(this@EventListActivity).apply {
                    text = "ERROR: ${p0.message}"
                    typeface = Utils.getTextTypeface(this@EventListActivity)
                    setTextColor(Color.WHITE)
                })
            }

            override fun onDataChange(p0: DataSnapshot) {
                root.findViewWithTag<LinearLayout>("recyclerViewHost").animate().alpha(0f).setDuration(250L).setStartDelay(0L).setListener(object : SimpleAnimatorListener() {
                    override fun onAnimationEnd(animator: Animator?) {
                        root.removeAllViews()
                        swipe_refresh_layout.isRefreshing = false
                        if (!p0.exists() || p0.childrenCount == 0L) {
                            root.addView(AuthenticElement.createText(this@EventListActivity, "There are no upcoming events.", "center", size = 22f))
                            return
                        }
                        runOnUiThread {
                            val createHandler: (AuthenticEventPlaceholder) -> ((e: AuthenticEventPlaceholder) -> Unit) = {
                                when {
                                    it.action != null -> { e -> e.action!!.invoke(this@EventListActivity) }
                                    it.canOpen -> { e -> EventActivity.start(this@EventListActivity, e) }
                                    else -> { _ -> }
                                }
                            }
                            val constructed = p0.children.map { Utils.Constructors.constructEvent(it.value!!) }.filter { it != null }.map { it!! }
                            val tiles = constructed.filter { it is AuthenticEventPlaceholder }.map { it as AuthenticEventPlaceholder }.sortedBy { it.index }.map { Tile(it.title, it.hideTitle, it.header, it, createHandler(it)) } + constructed.filter { it !is AuthenticEventPlaceholder }.filter { !it.getShouldBeHidden() }.sortedBy { it.getNextOccurrence().startDate.toEpochSecond() }.map { Tile(it.title, it.hideTitle, it.header, it) { e -> EventActivity.start(this@EventListActivity, e) } }
                            if (tiles.isEmpty()) {
                                root.addView(AuthenticElement.createText(this@EventListActivity, "There are no upcoming events.", "center", size = 22f))
                            } else {
                                val recyclerView = RecyclerView(this@EventListActivity)
                                recyclerView.adapter = TileAdapter(this@EventListActivity, tiles, true, false, 0)
                                recyclerView.layoutManager = LinearLayoutManager(this@EventListActivity)
                                recyclerView.addItemDecoration(DividerItemDecoration(this@EventListActivity, (recyclerView.layoutManager as LinearLayoutManager).orientation))
                                root.addView(LinearLayout(this@EventListActivity).apply {
                                    addView(recyclerView)
                                    tag = "recyclerViewHost"
                                    orientation = LinearLayout.VERTICAL
                                    alpha = 0f
                                    animate().setStartDelay(250L).alpha(1f).duration = 250L
                                })
                            }
                            if (constructed.count { it == null } > 0)
                                AlertDialog.Builder(this@EventListActivity).setTitle("Error").setMessage("We're having trouble loading content.  Please try again later.  We apologise for the inconvenience.").setPositiveButton("Dismiss", null).create().applyColorsAndTypefaces().show()
                            trace.stop()
                        }
                    }
                })
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            onBackPressed()
            true
        }
        else super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finish()
        //overridePendingTransition(R.anim.empty, R.anim.slide_down)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = intent.getStringExtra("title")
        setContentView(R.layout.activity_event_list)
        findViewById<Toolbar>(R.id.toolbar).apply {
            this@EventListActivity.setSupportActionBar(this)
            setBackgroundColor(Color.parseColor("#212121"))
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.applyTypeface(this, title as String)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        findViewById<TextView>(R.id.toolbar_title).apply {
            typeface = Utils.getTitleTypeface(this@EventListActivity)
            text = title as String
        }
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left_white_36dp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        Handler().postDelayed({
            runOnUiThread {
                swipe_refresh_layout.isRefreshing = true
                loadEvents(false)
            }
        }, 500L)
        swipe_refresh_layout.apply {
            setOnRefreshListener {
                loadEvents(true)
            }
        }
    }
}
