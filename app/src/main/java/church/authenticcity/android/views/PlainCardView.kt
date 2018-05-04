package church.authenticcity.android.views

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 4/7/2018 at 9:27 AM.
 * Licensed under the MIT License.
 */


import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.support.v7.widget.CardView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import church.authenticcity.android.EventActivity
import church.authenticcity.android.R
import church.authenticcity.android.TabActivity
import church.authenticcity.android.classes.AuthenticEvent
import church.authenticcity.android.classes.AuthenticTab
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.isNullOrWhiteSpace
import kotlin.math.roundToInt

class PlainCardView(context: Context, header: String, title: String, handler: () -> Unit) : CardView(context) {
    constructor(context: Context, tab: AuthenticTab) : this(context, tab.header, if (tab.hideTitle) "" else tab.title, { TabActivity.start(context, tab) })
    constructor(context: Context, event: AuthenticEvent) : this(context, event.header, if (event.hideTitle) "" else event.title, { EventActivity.start(context, event) })

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_card_basic, this)
        Utils.loadFirebaseImage(context, header, view.findViewById(R.id.image))
        if (!String.isNullOrWhiteSpace(title)) {
            view.findViewById<TextView>(R.id.dataCardTitle).apply {
                text = title
                typeface = Utils.getTitleTypeface(context)
            }
            view.minimumHeight = 0
        } else {
            view.findViewById<LinearLayout>(R.id.dataCardTitleContainer).visibility = View.GONE
            view.minimumHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 256f, resources.displayMetrics).roundToInt()
        }
        setBackgroundColor(Color.TRANSPARENT)
        view.findViewById<CardView>(R.id.card).apply {
            if (Utils.checkSdk(21))
                foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(128, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
            setOnClickListener { handler() }
        }
    }
}