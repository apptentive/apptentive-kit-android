package apptentive.com.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import apptentive.com.android.core.Callback

object InteractionViewModelFactoryProvider {
    private val lookup = mutableMapOf<String, ViewModelFactory>()

    @Synchronized
    fun registerViewModelFactory(instanceId: String, factory: () -> ApptentiveViewModel) {
        val existingFactory = lookup[instanceId]
        if (existingFactory != null) {

        }
        lookup[instanceId] = ViewModelFactory(factory) {
            unregisterViewModelFactory(instanceId)
        }
    }

    @Synchronized
    private fun unregisterViewModelFactory(instanceId: String) {
        val factory = lookup.remove(instanceId)
        if (factory == null) {

        }
    }

    @Synchronized
    fun getViewModelFactory(instanceId: String): ViewModelProvider.Factory {
        return lookup[instanceId]
            ?: throw IllegalArgumentException("View model factory not found: $instanceId")
    }
}

private class ViewModelFactory(
    private val factory: () -> ApptentiveViewModel,
    private val clearCallback: Callback
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val viewModel = factory()
        viewModel.clearCallback = clearCallback

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}

