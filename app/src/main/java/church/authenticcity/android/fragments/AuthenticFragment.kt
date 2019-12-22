package church.authenticcity.android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import church.authenticcity.android.R

typealias OnFragmentTitleChangeListener = (String) -> Unit

abstract class AuthenticFragment(private var _title: String, @LayoutRes private val layoutRes: Int = R.layout.fragment_content_basic, private val listener: OnFragmentTitleChangeListener? = null) : Fragment() {
    var title: String
        get() = _title
        set(new) {
            _title = new
            listener?.invoke(new)
        }

    protected abstract fun onCreateView(view: View)

    protected abstract fun onRefreshView(view: View)

    protected fun onRefresh() {
        if (view != null)
            onRefreshView(view!!)
        return
    }

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflatedView = inflater.inflate(layoutRes, container, false)
        retainInstance = true
        onCreateView(inflatedView)
        onRefreshView(inflatedView)
        return inflatedView
    }
}

