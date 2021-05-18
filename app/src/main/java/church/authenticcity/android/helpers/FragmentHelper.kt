package church.authenticcity.android.helpers

import church.authenticcity.android.databinding.FragmentContentBasicBinding
import church.authenticcity.android.fragments.*

class FragmentHelper {
    companion object {
        fun getTabFragment(id: String, title: String, specialType: String?, listener: OnFragmentTitleChangeListener?): AuthenticFragment<*> {
            return when(specialType) {
                "upcoming_events" -> EventListFragment.getInstance()
                //"watchPlaylist" -> VideoPlaylistFragment(ids[position])
                //"wallpapers" -> WallpaperListFragment(ids[position])
                else -> when (id) {
                    "OPQ26R4SRP" -> WatchFragment.getInstance(id, "WATCH", null)
                    else -> TabFragment.getInstance(id, title, listener)
                }
            }
        }

        fun getEventFragment(id: String, title: String, listener: OnFragmentTitleChangeListener?): AuthenticFragment<FragmentContentBasicBinding> =
                EventFragment.getInstance(id, title, listener)
    }
}