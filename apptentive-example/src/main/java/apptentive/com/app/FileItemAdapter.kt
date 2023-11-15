package apptentive.com.app

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.util.InternalUseOnly
import com.apptentive.apptentive_example.databinding.FileItemBinding

class FileItemAdapter(private val fileNames: Array<String>) : RecyclerView.Adapter<FileItemAdapter.FileItemViewHolder>() {
    override fun getItemCount(): Int = fileNames.size

    @OptIn(InternalUseOnly::class)
    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {
        holder.binding.apply {
            fileItemButton.text = fileNames[position]
            fileItemButton.setOnClickListener {
                val jsonString = this.root.context.assets.open("manifest/${fileNames[position]}.json").bufferedReader().use { it.readText() }
                val result = if (Apptentive.setLocalManifest(jsonString)) "is successful" else "failed"
                Toast.makeText(root.context, "${fileNames[position]}.json upload $result", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder {
        return FileItemViewHolder(FileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    class FileItemViewHolder(val binding: FileItemBinding) : ViewHolder(binding.root)
}
