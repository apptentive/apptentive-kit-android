package apptentive.com.exercise.viewmodels

import androidx.lifecycle.*
import apptentive.com.exercise.data.Language
import apptentive.com.exercise.data.LanguageRepository
import java.util.*

class AddLanguageViewModel internal constructor(private val repository: LanguageRepository) : ViewModel() {
    private val releaseDateData = MutableLiveData<Date>().apply { value = Date() }
    val releaseDate: LiveData<Date> = releaseDateData

    fun setReleaseDate(date: Date) {
        releaseDateData.value = date
    }

    fun addLanguage(name: String, description: String?) {
        repository.insert(Language(name, releaseDate.value!!, false, description))
    }
}

class AddLanguageViewModelFactory(private val repository: LanguageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass == AddLanguageViewModel::class.java) {
            @Suppress("UNCHECKED_CAST")
            return AddLanguageViewModel(repository) as T
        }

        throw IllegalStateException("Unexpected model class: $modelClass")
    }

}