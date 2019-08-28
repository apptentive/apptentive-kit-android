package apptentive.com.exercise.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.exercise.data.Language
import apptentive.com.exercise.databinding.LanguageListItemBinding

class LanguageListAdapter : ListAdapter<Language, LanguageListAdapter.ViewHolder>(LanguageDiffCallback()) {
    var languageClickListener: LanguageClickListener? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)!!
        holder.bind(createClickListener(item), item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LanguageListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    private fun createClickListener(language: Language): View.OnClickListener {
        return View.OnClickListener { languageClickListener?.onClick(language) }
    }

    class ViewHolder(
        private val binding: LanguageListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: View.OnClickListener, language: Language) {
            binding.apply {
                this.clickListener = clickListener
                this.language = language
                executePendingBindings()
            }
        }
    }

    interface LanguageClickListener {
        fun onClick(language: Language)
    }
}

private class LanguageDiffCallback : DiffUtil.ItemCallback<Language>() {
    override fun areItemsTheSame(oldItem: Language, newItem: Language): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Language, newItem: Language): Boolean {
        return oldItem == newItem
    }
}


