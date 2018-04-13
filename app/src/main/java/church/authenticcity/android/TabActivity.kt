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
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import church.authenticcity.android.classes.AuthenticTab
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyColorsAndTypefaces
import church.authenticcity.android.helpers.applyTypeface
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TabActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context, tab: AuthenticTab) {
            Utils.Temp.putTab(tab)
            context.startActivity(Intent(context, TabActivity::class.java).apply { putExtra("id", tab.id) })
        }

        fun start(context: Context, tabId: String) {
            val dialog = Utils.createIndeterminateDialog(context, "Loading...")
            dialog.show()
            FirebaseDatabase.getInstance().getReference("/tabs/$tabId").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    dialog.dismiss()
                    AlertDialog.Builder(context).setTitle("Unexpected Error").setCancelable(false).setMessage("An unexpected error occurred while loading data.\n\nCode: ${p0?.code
                            ?: "unknown"}\nMessage: ${p0?.message
                            ?: "unknown"}\nDetails: ${p0?.details
                            ?: "unknown"}").setPositiveButton("Dismiss", null).create().applyColorsAndTypefaces().show()
                }

                override fun onDataChange(p0: DataSnapshot?) {
                    dialog.dismiss()
                    start(context, p0!!.getValue(AuthenticTab::class.java)!!)
                }
            })
        }
    }

    private fun setContent(vararg views: View) = setContent(views.toList())

    private fun setContent(views: List<View>) {
        findViewById<LinearLayout>(R.id.content_list).apply {
            removeAllViews()
            views.forEach { addView(it) }
            addView(LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt())
                setBackgroundColor(Color.TRANSPARENT)
            })
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

    private fun initialize() {
        val tab = Utils.Temp.getTab(intent.getStringExtra("id"))!!
        this.title = tab.title
        supportActionBar?.applyTypeface(this, tab.title)
        if (tab.hideHeader) findViewById<ImageView>(R.id.image).visibility = View.GONE else Utils.loadFirebaseImage(this, tab.header, findViewById(R.id.image))
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
