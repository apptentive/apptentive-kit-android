package apptentive.com.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.app.databinding.ItemDeviceBinding

class DebugItemAdapter(private val debugItems: List<DebugItem>) : RecyclerView.Adapter<DebugItemAdapter.BindingViewHolder>() {
    override fun getItemCount() = debugItems.size
    private fun getItem(position: Int) = debugItems[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        return BindingViewHolder(ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        holder.binding.apply {
            val deviceItem = getItem(position)
            deviceItemName.text = deviceItem.name
            deviceItemValue.text = deviceItem.value
        }
    }

    class BindingViewHolder(val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root)
}
