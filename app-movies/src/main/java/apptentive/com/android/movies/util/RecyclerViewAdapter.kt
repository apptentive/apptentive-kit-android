package apptentive.com.android.movies.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

open class Item(internal val itemViewType: Int) {
    val itemId: Long get() = 0
}

class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder<Item>>() {
    private val items: MutableList<Item> = mutableListOf()
    private val lookup: MutableMap<Int, Factory<*>> = HashMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<Item> {
        val factory = getFactory(viewType)
        val view = factory.createConvertView(parent)

        @Suppress("UNCHECKED_CAST")
        return factory.createViewHolder(view) as ViewHolder<Item>
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).itemId
    }

    override fun onBindViewHolder(holder: ViewHolder<Item>, position: Int) {
        val view = getItem(position)
        holder.bindView(view, position)
    }

    private fun getItem(position: Int): Item {
        return items[position]
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).itemViewType
    }

    fun register(itemType: Enum<*>, factory: Factory<*>) {
        register(itemType.ordinal, factory)
    }

    fun register(itemType: Int, factory: Factory<*>) {
        lookup[itemType] = factory
    }

    private fun getFactory(itemType: Int): Factory<*> {
        return lookup[itemType] ?: throw IllegalArgumentException("Item type not registered: $itemType")
    }

    fun setItems(newItems: List<Item>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    interface Factory<T : Item> {
        fun createConvertView(parent: ViewGroup): View
        fun createViewHolder(convertView: View): ViewHolder<T>
    }

    abstract class LayoutIdFactory<T : Item>(private val layoutId: Int) :
        Factory<T> {
        override fun createConvertView(parent: ViewGroup): View {
            return LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        }
    }

    abstract class ViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bindView(item: T, position: Int)
    }
}