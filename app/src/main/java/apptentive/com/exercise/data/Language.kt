package apptentive.com.exercise.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "languages")
data class Language(
    @PrimaryKey @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "release_date") val releaseDate: Date,
    @ColumnInfo(name = "favorite") val favorite: Boolean,
    @ColumnInfo(name = "description") val description: String?
)
