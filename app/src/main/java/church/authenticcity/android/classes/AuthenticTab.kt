package church.authenticcity.android.classes

class AuthenticTab(val header: String? = null, val id: String = "", val index: Int = -1, val title: String = "", var bundles: List<AuthenticBundle>? = null) {
    private var mBundlesAlreadySorted = false

    fun getSortedBundles(): List<AuthenticBundle>? {
        if (!mBundlesAlreadySorted) {
            mBundlesAlreadySorted = true
            bundles = bundles?.sortedBy { b -> b.index }
        }
        return bundles
    }
}