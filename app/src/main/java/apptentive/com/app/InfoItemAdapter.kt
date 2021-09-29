package apptentive.com.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.app.databinding.InfoItemBinding

class InfoItemAdapter(private val infoItems: List<InfoItem>) : RecyclerView.Adapter<InfoItemAdapter.BindingViewHolder>() {
    override fun getItemCount() = infoItems.size
    private fun getItem(position: Int) = infoItems[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        return BindingViewHolder(InfoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        holder.binding.apply {
            val deviceItem = getItem(position)
            deviceItemName.text = deviceItem.name
            deviceItemValue.text = deviceItem.value
        }
    }

    class BindingViewHolder(val binding: InfoItemBinding) : RecyclerView.ViewHolder(binding.root)
}
