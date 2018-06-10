package church.authenticcity.android.views.recyclerView

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 6/8/2018 at 8:26 PM.
 * Licensed under the MIT License.
 */
class TileAdapter(private val activity: Activity, private val tiles: List<Tile<*>>) : RecyclerView.Adapter<TileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TileViewHolder(activity, parent)

    override fun getItemCount() = tiles.size

    override fun onBindViewHolder(holder: TileViewHolder, position: Int) {
        holder.initialize(tiles[position])
    }
}