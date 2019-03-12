package apptentive.com.android.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ItemFragment : Fragment() {

    // TODO: Customize parameters
    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                val items = listOf(
                    BeverageItem("1739787", R.drawable.ic_coffee_01, "Iced Caramel Cloud Macchiato"),
                    BeverageItem("4686989", R.drawable.ic_coffee_02, "Iced Cinnamon Cloud Macchiato"),
                    BeverageItem("7501926", R.drawable.ic_coffee_03, "Iced Matcha Green Tea Latte"),
                    BeverageItem("2724579", R.drawable.ic_coffee_04, "Cold Brew with Cascara Cold Foam"),
                    FeedbackItem("How was your last store experience?"),
                    BeverageItem("6402270", R.drawable.ic_coffee_05, "Nitro Cold Brew"),
                    BeverageItem("6810919", R.drawable.ic_coffee_06, "Matcha Green Tea Crème Frappuccino®"),
                    BeverageItem("5951855", R.drawable.ic_coffee_07, "Starbucks® Blonde Flat White"),
                    BeverageItem("2225867", R.drawable.ic_coffee_08, "Mango Dragonfruit Starbucks Refreshers® Beverage"),
                    BeverageItem("1598848", R.drawable.ic_coffee_09, "Iced Passion Tango™ Tea Lemonade")
                )

                adapter = RecyclerViewAdapter(items).apply {
                    register(ItemType.BEVERAGE, object: RecyclerViewAdapter.LayoutIdFactory<BeverageItem>(R.layout.beverage_item) {
                        override fun createViewHolder(convertView: View): RecyclerViewAdapter.ViewHolder<BeverageItem> {
                            return BeverageItem.ViewHolder(convertView)
                        }
                    })
                    register(ItemType.FEEDBACK, object: RecyclerViewAdapter.LayoutIdFactory<FeedbackItem>(R.layout.feedback_item) {
                        override fun createViewHolder(convertView: View): RecyclerViewAdapter.ViewHolder<FeedbackItem> {
                            return FeedbackItem.ViewHolder(convertView)
                        }
                    })
                }
            }
        }
        return view
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            ItemFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
