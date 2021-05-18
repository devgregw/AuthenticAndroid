package church.authenticcity.android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

typealias OnFragmentTitleChangeListener = (String) -> Unit

abstract class AuthenticFragment<TBinding> : Fragment() {
    private var listener: OnFragmentTitleChangeListener? = null
    private var binder: ((LayoutInflater, ViewGroup?, Boolean) -> TBinding)? = null
    private var _binding: TBinding? = null
    protected val binding get() = _binding!!

    private var _title: String = ""

    protected fun setup(title: String, binder: (LayoutInflater, ViewGroup?, Boolean) -> TBinding, listener: OnFragmentTitleChangeListener? = null) {
        _title = title
        this.binder = binder
        this.listener = listener
    }

    var title: String
        get() = _title
        set(value) {
            _title = value
            listener?.invoke(value)
        }

    protected abstract fun onCreateView(view: View)

    protected abstract fun onRefreshView(view: View)

    protected fun onRefresh() {
        if (view != null)
            onRefreshView(view!!)
        return
    }

    protected abstract val root: View

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (this.binder == null)
            return null
        _binding = binder!!.invoke(inflater, container, false)
        retainInstance = true
        onCreateView(root)
        onRefreshView(root)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

