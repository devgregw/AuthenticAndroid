package church.authenticcity.android

import android.animation.Animator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import church.authenticcity.android.classes.AuthenticTab
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.views.TabView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private var sheetView: NestedScrollView? = null

    private var dialog: BottomSheetDialog? = null

    private fun loadTabs() {
        sheetView!!.removeAllViews()
        sheetView!!.addView(RelativeLayout(this).apply {
            addView(ProgressBar(this@MainActivity).apply {
                isIndeterminate = true
                val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75f, resources.displayMetrics).toInt()
                layoutParams = RelativeLayout.LayoutParams(size, size).apply {
                    addRule(RelativeLayout.CENTER_IN_PARENT)
                }
                if (Utils.checkSdk(21))
                    indeterminateTintList = ColorStateList.valueOf(Color.WHITE)
            })
        })
        val loader = {
            FirebaseDatabase.getInstance().getReference("/tabs/").orderByChild("index").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    sheetView!!.removeAllViews()
                    sheetView!!.addView(TextView(this@MainActivity).apply {
                        text = "ERROR: " + p0?.message
                        setTextColor(Color.BLACK)
                    })
                }

                override fun onDataChange(p0: DataSnapshot?) {
                    sheetView!!.removeAllViews()
                    val layout = LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        addView(RelativeLayout(this@MainActivity).apply {
                            layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                var px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, this@MainActivity.resources.displayMetrics).roundToInt()
                                setMargins(px, px / 4, px, 0)
                            }
                            addView(ImageButton(this@MainActivity).apply {
                                layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.ALIGN_PARENT_LEFT) }
                                setImageResource(R.drawable.ic_keyboard_arrow_down_white_36dp)
                                setBackgroundColor(Color.TRANSPARENT)
                                setOnClickListener { dialog!!.dismiss() }
                            })
                            addView(ImageButton(this@MainActivity).apply {
                                layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.ALIGN_PARENT_RIGHT) }
                                setImageResource(R.drawable.ic_refresh_white_36dp)
                                setBackgroundColor(Color.TRANSPARENT)
                                setOnClickListener { loadTabs() }
                            })
                        })
                    }
                    p0?.children?.forEach {
                        val data = it.getValue(AuthenticTab::class.java)
                        if (data !== null) {
                            /*layout.addView(ImageButton(this@MainActivity).also {
                                it.text = data.header + "\n" + data.id + "\n" + data.index + "\n" + data.title
                                it.setOnClickListener { TabActivity.start(this@MainActivity, data) }
                                Glide.with(this@MainActivity).load(FirebaseStorage.getInstance().reference.child(data.header)).into(it)
                            })*/
                            layout.addView(TabView(this@MainActivity, data))
                        }
                    }
                    sheetView!!.addView(layout)
                }
            })
        }
        if (FirebaseAuth.getInstance().currentUser != null)
            loader()
        else
            FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { _ -> loader() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<ImageButton>(R.id.tabsButton)
        button.translationY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75f, resources.displayMetrics)
        sheetView = NestedScrollView(this).apply { setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorBackgroundDark)) }
        dialog = BottomSheetDialog(this).apply { setContentView(sheetView!!) }
        //val tb = findViewById<Toolbar>(R.id.toolbar)
        //setSupportActionBar(tb)
        button.animate().setStartDelay(500L).translationY(0f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(500L).setListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(p0: Animator?) {
                button.animate().setStartDelay(0L).alpha(1f).setInterpolator(AccelerateDecelerateInterpolator()).duration = 250L
            }

            override fun onAnimationRepeat(p0: Animator?) {
                //not implemented
            }

            override fun onAnimationCancel(p0: Animator?) {
                //not implemented
            }

            override fun onAnimationStart(p0: Animator?) {
                //not implemented
            }
        })
        val image = findViewById<ImageView>(R.id.logo)
        image.animate().setStartDelay(500L).translationY(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -37.5f, resources.displayMetrics)).setInterpolator(AccelerateDecelerateInterpolator()).duration = 500L
        button.setOnClickListener {
            dialog!!.show()
        }
        loadTabs()
    }
}
