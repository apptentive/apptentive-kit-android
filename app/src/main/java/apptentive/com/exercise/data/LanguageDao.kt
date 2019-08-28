package apptentive.com.exercise.data

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface LanguageDao {
    @RawQuery(observedEntities = [Language::class])
    fun getLanguages(query: SupportSQLiteQuery): LiveData<List<Language>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(language: Language)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(item: List<Language>)

    @Query("SELECT * FROM languages WHERE name = :name")
    fun getLanguage(name: String): LiveData<Language>

    @Query("UPDATE languages SET favorite = NOT favorite WHERE name = :name")
    fun toggleFavourite(name: String)
}