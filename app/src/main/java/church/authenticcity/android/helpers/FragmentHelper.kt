package church.authenticcity.android.helpers

import church.authenticcity.android.fragments.*

class FragmentHelper {
    companion object {
        fun getTabFragment(id: String, title: String, specialType: String?, listener: OnFragmentTitleChangeListener?): AuthenticFragment {
            return when(specialType) {
                "upcoming_events" -> EventListFragment()
                //"watchPlaylist" -> VideoPlaylistFragment(ids[position])
                //"wallpapers" -> WallpaperListFragment(ids[position])
                else -> when (id) {
                    "OPQ26R4SRP" -> WatchFragment("OPQ26R4SRP", "WATCH",null)
                    else -> TabFragment(id, title, listener)
                }
            }
        }

        fun getEventFragment(id: String, title: String, listener: OnFragmentTitleChangeListener?): AuthenticFragment =
                EventFragment(id, title, listener)
    }
}