package apptentive.com.android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface ViewHolderFactory {
    fun createItemView(parent: ViewGroup): View
    fun createViewHolder(itemView: View): ApptentiveViewHolder<*>
}

@InternalUseOnly
class LayoutViewHolderFactory(
    private val layoutId: Int,
    private val viewHolderCreator: (View) -> ApptentiveViewHolder<*>,
) : ViewHolderFactory {
    override fun createItemView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
    }

    override fun createViewHolder(itemView: View): ApptentiveViewHolder<*> {
        return viewHolderCreator.invoke(itemView)
    }
}
