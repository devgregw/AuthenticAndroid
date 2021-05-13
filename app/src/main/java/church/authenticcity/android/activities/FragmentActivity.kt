package church.authenticcity.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import church.authenticcity.android.R
import church.authenticcity.android.databinding.ActivityFragmentBinding
import church.authenticcity.android.fragments.EventListFragment
import church.authenticcity.android.fragments.OnFragmentTitleChangeListener
import church.authenticcity.android.helpers.FragmentHelper
import church.authenticcity.android.helpers.Utils

class FragmentActivity : AppCompatActivity() {
    companion object {
        fun startTab(context: Context, id: String, title: String, specialType: String?) {
            context.startActivity(Intent(context, FragmentActivity::class.java).apply {
                putExtra("type", "tab")
                putExtra("id", id)
                putExtra("title", title)
                putExtra("specialType", specialType)
            })
        }

        fun startTab(context: Context, id: String) {
            startTab(context, id, "", null)
        }

        fun startEvent(context: Context, id: String, title: String) {
            context.startActivity(Intent(context, FragmentActivity::class.java).apply {
                putExtra("type", "event")
                putExtra("id", id)
                putExtra("title", title)
            })
        }

        fun startEvent(context: Context, id: String) {
            startEvent(context, id, "")
        }

        fun startEventList(context: Context) {
            context.startActivity(Intent(context, FragmentActivity::class.java).apply {
                putExtra("type", "eventList")
            })
        }
    }

    private lateinit var binding: ActivityFragmentBinding

    private fun setupToolbar(title: String) {
        binding.root.findViewById<ImageButton>(R.id.toolbar_back).setOnClickListener {
            finish()
        }
        binding.root.findViewById<TextView>(R.id.toolbar_title).text = Utils.makeTypefaceSpan(title, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFragmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val listener: OnFragmentTitleChangeListener = {new -> setupToolbar(new)}
        val fragment = when (intent.getStringExtra("type")) {
            "tab" -> FragmentHelper.getTabFragment(intent.getStringExtra("id") ?: "/ERROR/", intent.getStringExtra("title") ?: "Error 400", intent.getStringExtra("specialType") ?: "", listener)
            "event" -> FragmentHelper.getEventFragment(intent.getStringExtra("id") ?: "/ERROR/", intent.getStringExtra("title") ?: "Error 400", listener)
            "eventList" -> EventListFragment()
            else -> FragmentHelper.getTabFragment("/ERROR/", "Error 400", "", listener)
        }
        setupToolbar(fragment.title)
        supportFragmentManager.beginTransaction().add(R.id.frame_layout, fragment).commit()
    }
}
