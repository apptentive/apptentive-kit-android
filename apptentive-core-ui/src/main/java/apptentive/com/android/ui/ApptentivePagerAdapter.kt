package apptentive.com.android.ui

import android.util.SparseArray
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
class ApptentivePagerAdapter : RecyclerView.Adapter<ApptentiveViewHolder<ListViewItem>>() {
    private val pages = mutableListOf<ListViewItem>()
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

    override fun onBindViewHolder(holder: ApptentiveViewHolder<ListViewItem>, position: Int) {
        holder.bindView(pages[position], position)
    }

    override fun getItemCount(): Int = pages.size

    override fun getItemViewType(position: Int): Int {
        return pages[position].itemType
    }

    fun register(itemType: Int, factory: ViewHolderFactory) {
        viewHolderFactoryLookup.put(itemType, factory)
    }

    fun addOrUpdatePage(page: ListViewItem, isFocusEditText: Boolean) {
        val index = pages.indexOfFirst { it.id == page.id }
        if (index == -1) {
            pages.add(page)
            notifyItemInserted(pages.size - 1)
        } else {
            pages[index] = page
            if (!isFocusEditText) notifyItemChanged(index)
        }
    }
}
