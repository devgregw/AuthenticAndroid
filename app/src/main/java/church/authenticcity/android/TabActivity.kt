package church.authenticcity.android

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import church.authenticcity.android.classes.AuthenticTab
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyTypeface
import church.authenticcity.android.helpers.isNullOrWhiteSpace
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TabActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context, tab: AuthenticTab) {
            val intent = Intent(context, TabActivity::class.java)
            intent.putExtra("id", tab.id)
            intent.putExtra("title", tab.title)
            intent.putExtra("header", tab.header)
            intent.putExtra("hideHeader", tab.hideHeader)
            context.startActivity(intent)
        }

        fun start(context: Context, tabId: String) {
            val intent = Intent(context, TabActivity::class.java)
            intent.putExtra("loadAll", true)
            intent.putExtra("id", tabId)
            context.startActivity(intent)
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

    private fun initialize(title: String, header: String?, id: String) {
        this.title = title
        supportActionBar?.applyTypeface(this, title)
        if (!String.isNullOrWhiteSpace(header))
            Utils.loadFirebaseImage(this, header!!, findViewById(R.id.image))
        else
            findViewById<ImageView>(R.id.image).visibility = View.GONE
        FirebaseDatabase.getInstance().getReference("/tabs/" + id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                setContent(TextView(this@TabActivity).apply { text = p0!!.message })
            }

            override fun onDataChange(p0: DataSnapshot?) {
                val data = p0!!.getValue(AuthenticTab::class.java)!!
                if (data.elementCount > 0)
                    setContent(data.convertedElements.map { it.toView(this@TabActivity) })
                else
                    setContent(RelativeLayout(this@TabActivity).apply {
                        addView(TextView(this@TabActivity).apply {
                            textSize = 22f
                            text = "No content"
                            setTextColor(Color.BLACK)
                            typeface = Utils.getTextTypeface(this@TabActivity)
                            layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.CENTER_IN_PARENT) }
                        })
                    })
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab)
        findViewById<Toolbar>(R.id.toolbar).apply {
            this@TabActivity.setSupportActionBar(this)
            setBackgroundColor(Color.parseColor("#212121"))
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.applyTypeface(this, "")
        setContent(RelativeLayout(this@TabActivity).apply {
            addView(ProgressBar(this@TabActivity).apply {
                isIndeterminate = true
                val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics).toInt()
                layoutParams = RelativeLayout.LayoutParams(size, size).apply { addRule(RelativeLayout.CENTER_IN_PARENT) }
                indeterminateDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
                //if (Utils.checkSdk(21))
                //indeterminateTintList = ColorStateList.valueOf(Color.WHITE)
            })
        })
        val id = intent.getStringExtra("id")
        if (intent.getBooleanExtra("loadAll", false))
            FirebaseDatabase.getInstance().reference.child("tabs").child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) =
                        Utils.showErrorDialog(this@TabActivity, p0?.code ?: -1, p0?.message
                                ?: "Unknown", p0?.details ?: "Unknown")

                override fun onDataChange(p0: DataSnapshot?) {
                    val data = p0?.getValue(AuthenticTab::class.java)
                    if (data == null)
                        Utils.showErrorDialog(this@TabActivity, 404, "The data could not be found; it may have been deleted.", "Firebase/Database - /tabs/" + id)
                    else
                        this@TabActivity.initialize(data.title, if (data.hideHeader) null else data.header, id)
                }
            })
        else {
            val h = intent.getStringExtra("header")
            initialize(intent.getStringExtra("title"), if (intent.getBooleanExtra("hideHeader", false)) null else h, id)
        }
    }
}
