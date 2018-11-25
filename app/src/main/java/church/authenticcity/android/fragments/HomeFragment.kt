package church.authenticcity.android.fragments


import android.animation.Animator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import church.authenticcity.android.BuildConfig
import church.authenticcity.android.HomeActivity
import church.authenticcity.android.R
import church.authenticcity.android.helpers.SimpleAnimatorListener
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.applyColorsAndTypefaces
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_home.*
import kotlin.math.roundToInt

class HomeFragment : Fragment() {
    fun finishInitialization() {
        if (context == null) {
            Log.w("HomeFragment", "WARNING: skipping animations!")
            return
        }
        val image = view!!.findViewById<ImageView>(R.id.logo)
        val button = view!!.findViewById<ImageButton>(R.id.tabsButton).apply { setOnClickListener { (this@HomeFragment.requireActivity() as HomeActivity).goToTabs() } }
        view!!.findViewById<ProgressBar>(R.id.progress_bar).animate().setStartDelay(100L).alpha(0f).setInterpolator(AccelerateDecelerateInterpolator()).duration = 250L
        image.animate().setStartDelay(100L).alpha(1f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(250L).setListener(object : SimpleAnimatorListener() {
            override fun onAnimationEnd(p0: Animator?) {
                button.animate().setStartDelay(250L).translationY(0f).setInterpolator(AccelerateDecelerateInterpolator()).setDuration(500L).setListener(object : SimpleAnimatorListener() {
                    override fun onAnimationEnd(p0: Animator?) {
                        button.animate().setStartDelay(0L).alpha(1f).setInterpolator(AccelerateDecelerateInterpolator()).duration = 50L
                    }
                })
                if (context != null) // ¯\_(ツ)_/¯
                    image.animate().setStartDelay(250L).translationY(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -37.5f, resources.displayMetrics)).setInterpolator(AccelerateDecelerateInterpolator()).duration = 500L
            }
        })
    }

    private fun initialize() {
        if (context == null) {
            Log.w("HomeFragment", "WARNING: skipping animations!")
            return
        }
        if (BuildConfig.DEBUG) {
            debug_label.text = "DEBUG BUILD NOT FOR PRODUCTION\nVERSION ${BuildConfig.VERSION_NAME} BUILD ${BuildConfig.VERSION_CODE}"
        }
        else {
            debug_label.visibility = View.GONE
        }
        view!!.findViewById<ImageButton>(R.id.tabsButton).apply {
            translationY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75f, resources.displayMetrics)
            if (Utils.checkSdk(23))
                background = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 255, 255, 255)), null, null).apply { radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics).roundToInt() }
        }
        FirebaseDatabase.getInstance().reference.child("versions").child("android").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                AlertDialog.Builder(this@HomeFragment.requireContext()).setTitle("Unexpected Error").setCancelable(false).setMessage("An unexpected error occurred while checking for updates. You may be able to continue using the app.\n\nCode: ${p0.code}\nMessage: ${p0.message}\nDetails: ${p0.details}").setPositiveButton("Dismiss") { _, _ -> this@HomeFragment.finishInitialization() }.create().applyColorsAndTypefaces().show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (Utils.isUpdateAvailable(p0.value!!.toString().toInt())) {
                    AlertDialog.Builder(this@HomeFragment.requireContext()).setTitle("Update Available").setCancelable(false).setMessage("An update is available for the Authentic City Church app.  We highly recommend that you update to avoid missing out on new features.").setPositiveButton("Update") { _, _ -> this@HomeFragment.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${this@HomeFragment.requireContext().packageName}"))) }.setNegativeButton("Not Now") { _, _ -> this@HomeFragment.finishInitialization() }.create().applyColorsAndTypefaces().show()
                } else {
                    this@HomeFragment.finishInitialization()
                }
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initialize()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_home, container, false)
}
