package apptentive.com.android.movies

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import apptentive.com.android.love.LoveEntitySnapshot
import apptentive.com.android.movies.util.Item
import apptentive.com.android.movies.util.RecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_statistics.*

class StatisticsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val adapter = createAdapter()
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        val viewModel = getViewModel()
        viewModel.entries.observe(this, Observer {
            adapter.setItems(it.map { entity -> LoveEntityItem(entity) })
        })
    }

    private fun getViewModel(): StatisticsViewModel {
        return ViewModelProviders.of(this).get(StatisticsViewModel::class.java)
    }

    private fun createAdapter(): RecyclerViewAdapter {
        val adapter = RecyclerViewAdapter()
        adapter.register(
            ItemType.LOVE_ENTITY,
            object : RecyclerViewAdapter.LayoutIdFactory<LoveEntityItem>(R.layout.snapshot_item) {
                override fun createViewHolder(convertView: View): RecyclerViewAdapter.ViewHolder<LoveEntityItem> {
                    return LoveEntityItem.ViewHolder(convertView)
                }
            })

        return adapter
    }

    private enum class ItemType {
        LOVE_ENTITY
    }

    private class LoveEntityItem(private val loveEntitySnapshot: LoveEntitySnapshot) :
        Item(ItemType.LOVE_ENTITY.ordinal) {
        class ViewHolder(view: View) : RecyclerViewAdapter.ViewHolder<LoveEntityItem>(view) {
            private val textView: TextView = view.findViewById(R.id.textView)

            override fun bindView(item: LoveEntityItem, position: Int) {
                textView.text = item.loveEntitySnapshot.description()
            }
        }
    }
}