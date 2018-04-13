package church.authenticcity.android

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import church.authenticcity.android.classes.AuthenticElement
import church.authenticcity.android.classes.AuthenticEvent
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyColorsAndTypefaces
import church.authenticcity.android.helpers.applyTypeface
import church.authenticcity.android.helpers.isNullOrWhiteSpace
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EventActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context, event: AuthenticEvent) {
            Utils.Temp.putEvent(event)
            context.startActivity(Intent(context, EventActivity::class.java).apply { putExtra("id", event.id) })
        }

        fun start(context: Context, eventId: String) {
            val dialog = Utils.createIndeterminateDialog(context, "Loading...")
            dialog.show()
            FirebaseDatabase.getInstance().getReference("/events/$eventId").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    dialog.dismiss()
                    AlertDialog.Builder(context).setTitle("Unexpected Error").setCancelable(false).setMessage("An unexpected error occurred while loading data.\n\nCode: ${p0?.code
                            ?: "unknown"}\nMessage: ${p0?.message
                            ?: "unknown"}\nDetails: ${p0?.details
                            ?: "unknown"}").setPositiveButton("Dismiss", null).create().applyColorsAndTypefaces().show()
                }

                override fun onDataChange(p0: DataSnapshot?) {
                    dialog.dismiss()
                    EventActivity.start(context, p0!!.getValue(AuthenticEvent::class.java)!!)
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home)
            onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private fun initialize() {
        val event = Utils.Temp.getEvent(intent.getStringExtra("id"))!!
        supportActionBar?.applyTypeface(this, event.title)
        findViewById<LinearLayout>(R.id.content_list).apply {
            addView(AuthenticElement.createImage(this@EventActivity, event.header))
            addView(AuthenticElement.createCustomText(this@EventActivity, event.title, 32f, Utils.getTitleTypeface(this@EventActivity), "center", Color.BLACK).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt(), 0, 0)
                }
            })
            //addView(AuthenticElement.createTitle(this@EventActivity, event.title, "center"))
            addView(AuthenticElement.createCustomText(this@EventActivity, event.description, 20f, Utils.getTextTypeface(this@EventActivity), "left", Color.BLACK))
            //addView(AuthenticElement.createText(this@EventActivity, event.description, "left"))

            addView(AuthenticElement.createSeparator(this@EventActivity, true))
            addView(AuthenticElement.createTitle(this@EventActivity, "Date & Time", "center"))
            addView(AuthenticElement.createText(this@EventActivity, event.getNextOccurrence().format(), "left"))
            if (event.recurs)
                addView(AuthenticElement.createText(this@EventActivity, event.recurrenceRule!!.format(), "left"))
            addView(AuthenticElement.createButton(this@EventActivity, ButtonAction(HashMap<String, Any>().apply {
                put("group", 0)
                put("type", "AddToCalendarAction")
                put("eventId", event.id)
            }), "Add to Calendar"))

            addView(AuthenticElement.createSeparator(this@EventActivity, true))
            addView(AuthenticElement.createTitle(this@EventActivity, "Location", "center"))
            addView(AuthenticElement.createText(this@EventActivity, event.location, "left"))
            if (!String.isNullOrWhiteSpace(event.address)) {
                addView(AuthenticElement.createText(this@EventActivity, event.address, "left"))
                addView(AuthenticElement.createButton(this@EventActivity, ButtonAction(HashMap<String, Any>().apply {
                    put("group", -1)
                    put("type", "GetDirectionsAction")
                    put("address", event.address)
                }), "Get Directions"))
                addView(AuthenticElement.createButton(this@EventActivity, ButtonAction(HashMap<String, Any>().apply {
                    put("group", -1)
                    put("type", "ShowMapAction")
                    put("address", event.address)
                }), "Show on Map"))
            }

            addView(AuthenticElement.createSeparator(this@EventActivity, true))
            addView(AuthenticElement.createTitle(this@EventActivity, "Price & Registration", "center"))
            if (!String.isNullOrWhiteSpace(event.registrationUrl)) {
                addView(AuthenticElement.createText(this@EventActivity, if (event.price > 0) "$${event.price}" else "Free", "left"))
                addView(AuthenticElement.createText(this@EventActivity, "Registration is required", "left"))
                addView(AuthenticElement.createButton(this@EventActivity, ButtonAction(HashMap<String, Any>().apply {
                    put("group", -1)
                    put("type", "OpenURLAction")
                    put("url", event.registrationUrl)
                }), "Register Now"))
            } else {
                addView(AuthenticElement.createText(this@EventActivity, "Free", "left"))
                addView(AuthenticElement.createText(this@EventActivity, "Registration is not required", "left"))
            }
            addView(AuthenticElement.createSeparator(this@EventActivity, false))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_basic)
        findViewById<Toolbar>(R.id.toolbar).apply {
            this@EventActivity.setSupportActionBar(this)
            setBackgroundColor(Color.parseColor("#212121"))
        }
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left_white_36dp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initialize()
    }
}
