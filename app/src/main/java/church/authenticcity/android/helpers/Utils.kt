package church.authenticcity.android.helpers

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.support.v7.app.AlertDialog

fun String.Companion.isNullOrWhiteSpace(string: String?): Boolean {
    if (string != null) {
        var i = 0
        while (i < string.length) {
            if (!string[i].isWhitespace())
                return false
            ++i
        }
    }
    return true
}

class Utils {
    companion object {
        fun showErrorDialog(activity: Activity, code: Int, message: String, details: String) {
            AlertDialog.Builder(activity).setCancelable(false).setTitle("Error").setMessage(String.format("Unfortunately, an error occurred.\nCode: %s\nMessage: %s\nDetails: %s", code, message, details)).setPositiveButton("Close", { _, _ -> activity.finish()}).create().apply { setOnShowListener { dialog -> (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE) } }.show()
        }

        fun checkSdk(apiLevel: Int): Boolean = Build.VERSION.SDK_INT >= apiLevel
    }
}