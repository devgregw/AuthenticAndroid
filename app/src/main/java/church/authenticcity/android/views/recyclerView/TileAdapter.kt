package church.authenticcity.android.views.recyclerView

import android.app.Activity
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 6/8/2018 at 8:26 PM.
 * Licensed under the MIT License.
 */
class TileAdapter(private val activity: Activity, private val tiles: List<Tile<*>>, private val fullWidth: Boolean, private val fillColumn: Boolean, private val height: Int) : RecyclerView.Adapter<TileViewHolder>() {
    private fun getHeight() = if (itemCount > 4) null else (height.toDouble() / itemCount).roundToInt()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TileViewHolder.getInstance(activity, fullWidth, if (fillColumn) getHeight() else null, parent)

    override fun getItemCount() = tiles.size

    override fun onBindViewHolder(holder: TileViewHolder, position: Int) {
        holder.bind(tiles[position])
    }
}