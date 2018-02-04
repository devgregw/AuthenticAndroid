package church.authenticcity.android.classes

class AuthenticTab(val header: String? = null, val id: String = "", val index: Int = -1, val hideTitle: Boolean = false, val hideHeader: Boolean = false, val title: String = "", val elements: List<HashMap<String, Any>>? = null) {
    val convertedElements: List<AuthenticElement>
        get() = elements?.map(::AuthenticElement) ?: ArrayList()

    val elementCount: Int
        get() = convertedElements.count()
}