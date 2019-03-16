package church.authenticcity.android.views

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.provider.Settings
import android.support.v7.widget.PopupMenu
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import church.authenticcity.android.AuthenticApplication
import church.authenticcity.android.BuildConfig
import church.authenticcity.android.R
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.helpers.Utils
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.title_bar_view.view.*
import kotlin.math.roundToInt

class TitleBarView {
    companion object {
        fun create(context: Context, viewGroup: ViewGroup, goHome: () -> Unit, loadTabs: (Boolean) -> Unit): View {
            val view = LayoutInflater.from(context).inflate(R.layout.title_bar_view, viewGroup, false) as RelativeLayout
            if (Utils.checkSdk(23)) {
                val buttonWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, context.resources.displayMetrics).roundToInt()
                val ripple = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 255, 255, 255)), null, null).apply { radius = buttonWidth / 2 }
                view.home_down_arrow.foreground = ripple
                view.expanded_menu.foreground = ripple
            }
            view.home_livestream_container.addView(LivestreamView.create(context, viewGroup))
            view.home_title.typeface = Utils.getTitleTypeface(context)
            view.home_down_arrow.setOnClickListener { goHome() }
            view.expanded_menu.apply {
                val popup = PopupMenu(context, view.expanded_menu)
                popup.menuInflater.inflate(R.menu.menu_info, popup.menu)
                if (!BuildConfig.DEBUG)
                    popup.menu.removeItem(R.id.menu_advanced)
                else
                    popup.menu.findItem(R.id.menu_db).setTitle(if (AuthenticApplication.useDevelopmentDatabase) R.string.db_prod else R.string.db_dev)
                popup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_settings -> {
                            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            })
                            true
                        }
                        R.id.menu_privacy -> {
                            ButtonAction.openUrl("https://authenticdocs.gregwhatley.dev/privacy-policy").invoke(context)
                            true
                        }
                        R.id.menu_licenses -> {
                            context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                            true
                        }
                        R.id.menu_copy_fcm -> {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.primaryClip = ClipData.newPlainText("fcm", FirebaseInstanceId.getInstance().token
                                    ?: "<unavailable>")
                            Utils.makeToast(context, "Your FCM Registration Token was copied.", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.menu_db -> {
                            AuthenticApplication.useDevelopmentDatabase = !AuthenticApplication.useDevelopmentDatabase
                            loadTabs(true)
                            true
                        }
                        else -> false
                    }
                }
                view.expanded_menu.setOnClickListener {
                    popup.show()
                }
            }
            return view
        }
    }
}