package apptentive.com.exercise.util

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import apptentive.com.exercise.data.SortMode

object QueryUtil {
    fun createLanguageQuery(sortMode: SortMode, favoritesOnly: Boolean): SupportSQLiteQuery {
        var query = "SELECT * FROM languages"
        if (favoritesOnly) {
            query += " WHERE favorite = 1"
        }

        when (sortMode) {
            SortMode.NAME, SortMode.RELEASE_DATE -> query += " ORDER BY $sortMode ASC"
            SortMode.DEFAULT -> {
                // do nothing
            }
        }

        return SimpleSQLiteQuery(query)
    }
}