package apptentive.com.android.ui

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
class ListViewAdapter : ListAdapter<ListViewItem, ListViewAdapter.ViewHolder<ListViewItem>>(DIFF) {
    private val viewHolderFactoryLookup = SparseArray<ViewHolderFactory>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ListViewItem> {
        // resolve factory object
        val viewHolderFactory = viewHolderFactoryLookup.get(viewType)

        // create item view
        val itemView = viewHolderFactory.createItemView(parent)

        // create view holder
        @Suppress("UNCHECKED_CAST")
        return viewHolderFactory.createViewHolder(itemView) as ViewHolder<ListViewItem>
    }

    override fun onBindViewHolder(
        holder: ViewHolder<ListViewItem>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val changeMask = payloads.firstOrNull() as? Int
        if (changeMask != null) {
            holder.updateView(getItem(position), position, changeMask)
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder<ListViewItem>, position: Int) {
        holder.bindView(getItem(position), position)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).itemType
    }

    fun register(itemType: Int, factory: ViewHolderFactory) {
        viewHolderFactoryLookup[itemType] = factory
    }

    abstract class ViewHolder<T : ListViewItem> constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        abstract fun bindView(item: T, position: Int)
        open fun updateView(item: T, position: Int, changeMask: Int) {
            bindView(item, position)
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<ListViewItem>() {
            override fun areItemsTheSame(oldItem: ListViewItem, newItem: ListViewItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ListViewItem, newItem: ListViewItem): Boolean {
                return oldItem.areContentsTheSame(newItem)
            }

            override fun getChangePayload(oldItem: ListViewItem, newItem: ListViewItem): Any {
                return oldItem.getChangePayloadMask(newItem)
            }
        }
    }
}
