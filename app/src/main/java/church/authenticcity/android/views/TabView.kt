package church.authenticcity.android.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import church.authenticcity.android.R
import church.authenticcity.android.TabActivity
import church.authenticcity.android.classes.AuthenticTab
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.isNullOrWhiteSpace

class TabView(context: Context, tab: AuthenticTab) : CardView(context) {
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_tab, this)
        if (!String.isNullOrWhiteSpace(tab.header))
            Utils.loadFirebaseImage(context, tab.header!!, view.findViewById(R.id.image))
        if (!tab.hideTitle)
            view.findViewById<TextView>(R.id.dataCardTitle).apply {
                text = tab.title
                typeface = Utils.getTitleTypeface(context)
            }
        else
            view.findViewById<LinearLayout>(R.id.dataCardTitleContainer).visibility = View.GONE
        setBackgroundColor(Color.TRANSPARENT)
        view.findViewById<CardView>(R.id.card).apply {
            if (Utils.checkSdk(21))
                foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
            setOnClickListener {TabActivity.start(context, tab) }
        }
    }
}