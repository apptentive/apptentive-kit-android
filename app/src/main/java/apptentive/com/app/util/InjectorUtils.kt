package apptentive.com.app.util

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import apptentive.com.app.data.LanguageDatabase
import apptentive.com.app.data.LanguageRepository
import apptentive.com.app.viewmodels.AddLanguageViewModelFactory
import apptentive.com.app.viewmodels.LanguageDetailViewModelFactory
import apptentive.com.app.viewmodels.LanguageListViewModelFactory

object InjectorUtils {
    private fun getRepository(context: Context, executors: AppExecutors?): LanguageRepository {
        val dao = LanguageDatabase.getDatabase(context.applicationContext).itemDao()
        return LanguageRepository.getInstance(dao, executors ?: AppExecutors.defaultExecutors)
    }

    fun provideLanguageListViewModelFactory(
        context: Context,
        executors: AppExecutors? = null
    ): ViewModelProvider.Factory {
        val repository = getRepository(context, executors)
        return LanguageListViewModelFactory(repository)
    }

    fun provideLanguageDetailViewModelFactory(
        context: Context,
        languageName: String,
        executors: AppExecutors? = null
    ): ViewModelProvider.Factory {
        val repository = getRepository(context, executors)
        return LanguageDetailViewModelFactory(repository, languageName)
    }

    fun provideAddLanguageViewModelFactory(
        context: Context,
        executors: AppExecutors? = null
    ): ViewModelProvider.Factory {
        val repository = getRepository(context, executors)
        return AddLanguageViewModelFactory(repository)
    }
}