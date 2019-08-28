package apptentive.com.exercise.viewmodels

import androidx.lifecycle.*
import apptentive.com.exercise.data.LanguageRepository

class LanguageDetailViewModel internal constructor(
    private val repository: LanguageRepository,
    private val languageName: String
) : ViewModel() {
    val language = repository.getLanguage(languageName)

    fun toggleFavorite() {
        repository.toggleFavourite(languageName)
    }
}

class LanguageDetailViewModelFactory(private val repository: LanguageRepository, private val languageName: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass == LanguageDetailViewModel::class.java) {
            @Suppress("UNCHECKED_CAST")
            return LanguageDetailViewModel(repository, languageName) as T
        }

        throw IllegalStateException("Unexpected model class: $modelClass")
    }
}