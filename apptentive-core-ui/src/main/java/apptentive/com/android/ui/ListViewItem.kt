package apptentive.com.android.ui

abstract class ListViewItem(val id: String, val itemType: Int) {
    open fun areContentsTheSame(other: ListViewItem): Boolean {
        return this == other
    }

    open fun getChangePayloadMask(oldItem: ListViewItem): Int {
        return 0xffff // everything changed
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ListViewItem) return false

        if (id != other.id) return false
        if (itemType != other.itemType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + itemType
        return result
    }
}