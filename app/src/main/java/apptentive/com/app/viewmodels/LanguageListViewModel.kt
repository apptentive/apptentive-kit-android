package apptentive.com.app.viewmodels

import androidx.lifecycle.*
import apptentive.com.app.data.Language
import apptentive.com.app.data.LanguageRepository
import apptentive.com.app.data.SortMode

class LanguageListViewModel internal constructor(repository: LanguageRepository) : ViewModel() {
    val itemList: LiveData<List<Language>> = repository.getLanguages(SortMode.DEFAULT, favoritesOnly = false)

    fun sort(sortMode: SortMode) {
        TODO("Sort language list")
    }

    fun filterByFavorite(flag: Boolean) {
        TODO("Filter language list by favorites")
    }
}

class LanguageListViewModelFactory(private val repository: LanguageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass == LanguageListViewModel::class.java) {
            @Suppress("UNCHECKED_CAST")
            return LanguageListViewModel(repository) as T
        }

        throw IllegalStateException("Unexpected model class: $modelClass")
    }
}