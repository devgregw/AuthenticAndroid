package church.authenticcity.android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import church.authenticcity.android.R
import church.authenticcity.android.databinding.FragmentContentBasicBinding
import church.authenticcity.android.databinding.ViewToolbarBinding

typealias OnFragmentTitleChangeListener = (String) -> Unit

abstract class AuthenticFragment<TBinding>(private var _title: String, private val binder: (LayoutInflater, ViewGroup?, Boolean) -> TBinding /*@LayoutRes private val layoutRes: Int = R.layout.fragment_content_basic*/, private val listener: OnFragmentTitleChangeListener? = null) : Fragment() {
    var title: String
        get() = _title
        set(new) {
            _title = new
            listener?.invoke(new)
        }

    private var _binding: TBinding? = null
    protected val binding get() = _binding!!

    protected abstract fun onCreateView(view: View)

    protected abstract fun onRefreshView(view: View)

    protected fun onRefresh() {
        if (view != null)
            onRefreshView(view!!)
        return
    }

    protected abstract val root: View

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = binder(inflater, container, false)
        //val inflatedView = inflater.inflate(layoutRes, container, false)
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

