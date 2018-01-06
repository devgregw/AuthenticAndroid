package church.authenticcity.android.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.widget.TextView
import church.authenticcity.android.R
import church.authenticcity.android.TabActivity
import church.authenticcity.android.classes.AuthenticTab
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.isNullOrWhiteSpace
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.storage.FirebaseStorage

/**
 * Created by devgr on 12/26/2017.
 */
class TabView(context: Context, tab: AuthenticTab) : CardView(context) {
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_tab, this)
        if (!String.isNullOrWhiteSpace(tab.header))
        Glide.with(context).load(FirebaseStorage.getInstance().reference.child(tab.header!!)).transition(DrawableTransitionOptions.withCrossFade()).into(view.findViewById(R.id.image))
        view.findViewById<TextView>(R.id.dataCardTitle).text = tab.title
        setBackgroundColor(Color.TRANSPARENT)
        val card = view.findViewById<CardView>(R.id.card)
        if (Utils.checkSdk(21))
            card.foreground = RippleDrawable(ColorStateList.valueOf(Color.argb(64, 0, 0, 0)), null, ColorDrawable(Color.BLACK))
        card.setOnClickListener {
            TabActivity.start(context, tab)
        }
    }
}