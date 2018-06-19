package church.authenticcity.android.views.recyclerView

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import church.authenticcity.android.R

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 6/8/2018 at 8:28 PM.
 * Licensed under the MIT License.
 */
class DualRecyclerView {
    companion object {
        private fun createRecyclerView(activity: Activity, tiles: List<Tile<*>>) = RecyclerView(activity).apply {
            isNestedScrollingEnabled = false
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                weight = 0.5f
            }
            layoutManager = LinearLayoutManager(context)
            adapter = TileAdapter(activity, tiles, false)
        }

        fun create(activity: Activity, tiles: List<Tile<*>>) = create(activity, tiles.filterIndexed({i, t -> i % 2 == 0}), tiles.filterIndexed({i, _ -> i % 2 != 0}))

        fun create(activity: Activity, leftTiles: List<Tile<*>>, rightTiles: List<Tile<*>>) = LinearLayout(activity).apply {
            weightSum = 1f
            orientation = LinearLayout.HORIZONTAL
            layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                addRule(RelativeLayout.BELOW, R.id.toolbar)
            }
            alpha = 0f
            tag = "recyclerViewHost"
            addView(createRecyclerView(activity, leftTiles))
            addView(createRecyclerView(activity, rightTiles))
        }
    }
}