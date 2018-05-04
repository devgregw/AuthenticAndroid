package church.authenticcity.android

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import church.authenticcity.android.classes.AuthenticAppearance
import church.authenticcity.android.classes.AuthenticElement
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyTypeface
import church.authenticcity.android.views.PlainCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.roundToInt

class EventListActivity : AppCompatActivity() {

    companion object {
        fun start(parentActivity: Activity, appearance: AuthenticAppearance.Events) {
            parentActivity.startActivity(Intent(parentActivity, EventListActivity::class.java).apply { putExtra("title", appearance.title) })
            parentActivity.overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.empty)
        }
    }

    private var layout: LinearLayout? = null

    private fun loadEvents() {
        layout!!.removeAllViews()
        layout!!.addView(RelativeLayout(this).apply {
            addView(ProgressBar(this@EventListActivity).apply {
                isIndeterminate = true
                val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75f, resources.displayMetrics).toInt()
                layoutParams = RelativeLayout.LayoutParams(size, size).apply {
                    addRule(RelativeLayout.CENTER_IN_PARENT)
                }
                indeterminateDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
            })
        })
        val eventsRef = FirebaseDatabase.getInstance().getReference("/events/")
        eventsRef.keepSynced(true)
        eventsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onCancelled(p0: DatabaseError?) {
                layout!!.removeAllViews()
                layout!!.addView(TextView(this@EventListActivity).apply {
                    text = "ERROR: ${p0?.message}"
                    typeface = Utils.getTextTypeface(this@EventListActivity)
                    setTextColor(Color.WHITE)
                })
            }

            override fun onDataChange(p0: DataSnapshot?) {
                layout!!.removeAllViews()
                layout!!.apply {
                    addView(RelativeLayout(this@EventListActivity).apply {
                        layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                            val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, this@EventListActivity.resources.displayMetrics).roundToInt()
                            setMargins(px, px / 4, px, 0)
                        }
                    })
                }
                if (p0?.children?.count() == 0)
                    layout!!.addView(AuthenticElement.createCustomText(this@EventListActivity, "There are no upcoming events yet.", 22f, Utils.getTextTypeface(this@EventListActivity), "center", Color.BLACK))
                else
                    p0?.children?.map { Utils.Constructors.constructEvent(it.value!!) }?.filter { it.getIsVisible() }?.sortedBy { it.getNextOccurrence().startDate.toEpochSecond() }?.forEach {
                        Utils.Temp.putEvent(it)
                        layout!!.addView(PlainCardView(this@EventListActivity, it))
                    }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home)
            onBackPressed()
        else if (item?.itemId == R.id.menu_refresh)
            loadEvents()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.empty, R.anim.abc_slide_out_bottom)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_events, menu!!)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = intent.getStringExtra("title")
        setContentView(R.layout.activity_event_list)
        layout = findViewById(R.id.content_list)
        findViewById<Toolbar>(R.id.toolbar).apply {
            this@EventListActivity.setSupportActionBar(this)
            setBackgroundColor(Color.parseColor("#212121"))
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.applyTypeface(this, title as String)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_down_white_36dp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        loadEvents()
    }
}
