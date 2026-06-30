package apptentive.com.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

internal class ViewModelFactory(
    private val factory: () -> ViewModel,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = factory()
        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
