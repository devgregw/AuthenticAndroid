package church.authenticcity.android.views.recyclerView

import android.app.Activity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import church.authenticcity.android.R
import church.authenticcity.android.classes.AuthenticAppearance

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 6/8/2018 at 8:28 PM.
 * Licensed under the MIT License.
 */
class DualRecyclerView {
    companion object {
        private fun createRecyclerView(activity: Activity, tiles: List<Tile<*>>, fillColumn: Boolean, height: Int) = RecyclerView(activity).apply {
            isNestedScrollingEnabled = false
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                weight = 0.5f
            }
            layoutManager = LinearLayoutManager(context)
            adapter = TileAdapter(activity, tiles, false, fillColumn, height)
        }

        fun create(activity: Activity, tiles: List<Tile<*>>, appearance: AuthenticAppearance, height: Int) = create(activity, tiles.filterIndexed { i, _ -> i % 2 == 0}, tiles.filterIndexed { i, _ -> i % 2 != 0}, appearance, height)

        fun create(activity: Activity, leftTiles: List<Tile<*>>, rightTiles: List<Tile<*>>, appearance: AuthenticAppearance, height: Int) = LinearLayout(activity).apply {
            weightSum = 1f
            orientation = LinearLayout.HORIZONTAL
            layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                addRule(RelativeLayout.BELOW, R.id.toolbar)
            }
            alpha = 0f
            tag = "recyclerViewHost"
            addView(createRecyclerView(activity, leftTiles, appearance.tabs.fill, height))
            addView(createRecyclerView(activity, rightTiles, appearance.tabs.fill, height))
        }
    }
}