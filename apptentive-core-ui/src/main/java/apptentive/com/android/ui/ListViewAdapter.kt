package apptentive.com.android.ui

import android.util.SparseArray
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
class ListViewAdapter : ListAdapter<ListViewItem, ApptentiveViewHolder<ListViewItem>>(DIFF) {
    private val viewHolderFactoryLookup = SparseArray<ViewHolderFactory>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApptentiveViewHolder<ListViewItem> {
        // resolve factory object
        val viewHolderFactory = viewHolderFactoryLookup.get(viewType)

        // create item view
        val itemView = viewHolderFactory.createItemView(parent)

        // create view holder
        @Suppress("UNCHECKED_CAST")
        return viewHolderFactory.createViewHolder(itemView) as ApptentiveViewHolder<ListViewItem>
    }

    override fun onBindViewHolder(
        holder: ApptentiveViewHolder<ListViewItem>,
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

    override fun onBindViewHolder(holder: ApptentiveViewHolder<ListViewItem>, position: Int) {
        holder.bindView(getItem(position), position)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).itemType
    }

    fun register(itemType: Int, factory: ViewHolderFactory) {
        viewHolderFactoryLookup.put(itemType, factory)
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
