package church.authenticcity.android.views.recyclerView

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 6/8/2018 at 8:27 PM.
 * Licensed under the MIT License.
 */
class Tile<T>(val title: String, val header: String, val argument: T, val handler: (T) -> Unit)