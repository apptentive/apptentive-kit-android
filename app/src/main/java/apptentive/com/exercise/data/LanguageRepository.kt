package apptentive.com.exercise.data

import androidx.lifecycle.LiveData
import apptentive.com.exercise.util.AppExecutors
import apptentive.com.exercise.util.QueryUtil

class LanguageRepository(private val dao: LanguageDao, private val executors: AppExecutors) {
    fun getLanguages(sortMode: SortMode, favoritesOnly: Boolean): LiveData<List<Language>> {
        val query = QueryUtil.createLanguageQuery(sortMode, favoritesOnly)
        return dao.getLanguages(query)
    }

    fun getLanguage(name: String): LiveData<Language> = dao.getLanguage(name)

    fun insert(language: Language) {
        executors.io.execute {
            dao.insert(language)
        }
    }

    fun toggleFavourite(name: String) {
        executors.io.execute {
            dao.toggleFavourite(name)
        }
    }

    companion object {
        @Volatile
        private var instance: LanguageRepository? = null

        fun getInstance(dao: LanguageDao, executors: AppExecutors) =
            instance ?: synchronized(this) {
                instance ?: LanguageRepository(dao, executors).also {
                    instance = it
                }
            }
    }
}