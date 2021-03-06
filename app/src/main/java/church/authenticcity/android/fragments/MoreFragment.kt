package church.authenticcity.android.fragments

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import church.authenticcity.android.R
import church.authenticcity.android.activities.FragmentActivity
import church.authenticcity.android.databinding.FragmentMoreBinding
import church.authenticcity.android.helpers.Utils

class MoreFragment : AuthenticFragment<FragmentMoreBinding>() {
    override val root
        get() = binding?.root

    private val ids: Array<String>
        get() = arguments?.getStringArray("ids") ?: Array(0) { "" }

    private val titles: Array<String>
        get() = arguments?.getStringArray("titles") ?: Array(0) { "" }

    private val specialTypes: Array<String>
        get() = arguments?.getStringArray("specialTypes") ?: Array(0) { "" }

    companion object {
        fun getInstance(ids: Array<String>, titles: Array<String>, specialTypes: Array<String>) = MoreFragment().apply {
            arguments = Bundle().apply {
                putStringArray("ids", ids)
                putStringArray("titles", titles)
                putStringArray("specialTypes", specialTypes)
            }
            setup("MORE", {i, c, a -> FragmentMoreBinding.inflate(i, c, a)})
        }
    }

    class DataModel(val title: String, val id: String, val specialType: String)

    class ListViewAdapter(context: Context, private val items: Array<DataModel>) : ArrayAdapter<DataModel>(context, android.R.layout.simple_list_item_1, items), View.OnClickListener {
        class ViewHolder(val textView: TextView)

        override fun onClick(p0: View?) {
            if (p0 != null) {
                val model = items[p0.tag as Int]
                FragmentActivity.startTab(context, model.id, model.title, model.specialType)
            }
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val model = items[position]
            val result: View
            val viewHolder: ViewHolder
            if (convertView == null) {
                val newView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
                viewHolder = ViewHolder(newView as TextView)
                newView.tag = viewHolder
                result = newView
            } else {
                viewHolder = convertView.tag as ViewHolder
                result = convertView
            }
            viewHolder.textView.text = Utils.makeTypefaceSpan(model.title, context)
            viewHolder.textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            viewHolder.textView.setOnClickListener(this)
            viewHolder.textView.tag = position
            return result
        }
    }

    override fun onCreateView(view: View) {
        view.setBackgroundColor(ResourcesCompat.getColor(view.context.resources, R.color.colorBackground, null))
        binding?.listView?.adapter = ListViewAdapter(view.context, ids.mapIndexed { index, s -> DataModel(titles[index], s, specialTypes[index])}.toTypedArray())
    }

    override fun onRefreshView(view: View) {

    }
}