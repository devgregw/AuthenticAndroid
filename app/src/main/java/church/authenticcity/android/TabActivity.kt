package church.authenticcity.android

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import church.authenticcity.android.classes.AuthenticTab
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.isNullOrWhiteSpace
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class TabActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context, tab: AuthenticTab) {
            val intent = Intent(context, TabActivity::class.java)
            intent.putExtra("id", tab.id)
            intent.putExtra("title", tab.title)
            intent.putExtra("header", tab.header)
            context.startActivity(intent)
        }

        fun start(context: Context, tabId: String) {
            val intent = Intent(context, TabActivity::class.java)
            intent.putExtra("loadAll", true)
            intent.putExtra("id", tabId)
            context.startActivity(intent)
        }
    }

    private fun setContent(vararg views: View) {
        setContent(views.toList())
    }

    private fun setContent(views: List<View>) {
        val layout = findViewById<LinearLayout>(R.id.content_list)
        layout.removeAllViews()
        views.forEach {
            layout.addView(it)
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
        if (!String.isNullOrWhiteSpace(header))
            Glide.with(this).load(FirebaseStorage.getInstance().reference.child(header!!)).transition(DrawableTransitionOptions.withCrossFade()).into(findViewById(R.id.image))
        FirebaseDatabase.getInstance().getReference("/tabs/" + id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                setContent(TextView(this@TabActivity).apply { text = p0!!.message })
            }

            override fun onDataChange(p0: DataSnapshot?) {
                val data = p0!!.getValue(AuthenticTab::class.java)!!
                if (data.bundles != null)
                    setContent(data.getSortedBundles()!!.map { it.toView(this@TabActivity) })
                else
                    setContent(RelativeLayout(this@TabActivity).apply {
                        addView(TextView(this@TabActivity).apply {
                            textSize = resources.getDimension(R.dimen.textSize)
                            text = "No content"
                            setTextColor(Color.BLACK)
                            layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.CENTER_IN_PARENT) }
                        })
                    })
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<Toolbar>(R.id.toolbar).setBackgroundColor(Color.parseColor("#212121"))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setContent(RelativeLayout(this@TabActivity).apply {
            addView(ProgressBar(this@TabActivity).apply {
                isIndeterminate = true
                val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics).toInt()
                layoutParams = RelativeLayout.LayoutParams(size, size).apply {
                    addRule(RelativeLayout.CENTER_IN_PARENT)
                }
                if (Utils.checkSdk(21))
                    indeterminateTintList = ColorStateList.valueOf(Color.WHITE)
            })
        })
        val id = intent.getStringExtra("id")
        if (intent.getBooleanExtra("loadAll", false))
            FirebaseDatabase.getInstance().reference.child("tabs").child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) =
                        Utils.showErrorDialog(this@TabActivity, p0?.code ?: -1, p0?.message ?: "Unknown", p0?.details ?: "Unknown")

                override fun onDataChange(p0: DataSnapshot?) {
                    val data = p0?.getValue(AuthenticTab::class.java)
                    if (data == null)
                        Utils.showErrorDialog(this@TabActivity, 404, "The data could not be found; it may have been deleted or moved.", "Firebase/Database - /tabs/" + id)
                    else
                        this@TabActivity.initialize(data.title, data.header, id)
                }
            })
        else
            initialize(intent.getStringExtra("title"), intent.getStringExtra("header"), id)
    }
}
