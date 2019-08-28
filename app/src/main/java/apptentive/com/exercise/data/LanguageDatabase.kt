package apptentive.com.exercise.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.*
import java.util.concurrent.Executors


@Database(entities = [Language::class], version = 1)
@TypeConverters(Converters::class)
abstract class LanguageDatabase : RoomDatabase() {
    abstract fun itemDao(): LanguageDao

    companion object {
        @Volatile
        private var INSTANCE: LanguageDatabase? = null

        fun getDatabase(context: Context): LanguageDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LanguageDatabase::class.java,
                    "languages"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            populateDatabase(context)
                        }
                    })
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        private fun populateDatabase(context: Context) {
            Executors.newSingleThreadExecutor().execute {
                getDatabase(context).itemDao().insertAll(PREPOPULATE_DATA)
            }
        }

        private val PREPOPULATE_DATA = listOf(
            Language(
                "Java",
                dateOf(1995, 1, 23),
                favorite = true,
                description = "Java is a general-purpose programming language that is class-based, object-oriented, and designed to have as few implementation dependencies as possible."
            ),
            Language(
                "Kotlin",
                dateOf(2011, 7, 19),
                favorite = true,
                description = "Kotlin is a cross-platform, statically typed, general-purpose programming language with type inference."
            ),
            Language(
                "Swift",
                dateOf(2014, 6, 2),
                favorite = false,
                description = "Swift is a general-purpose, multi-paradigm, compiled programming language developed by Apple Inc. for iOS, macOS, watchOS, tvOS, Linux, and z/OS."
            ),
            Language(
                "JavaScript",
                dateOf(1998, 10, 19),
                favorite = false,
                description = "JavaScript, often abbreviated as JS, is a high-level, interpreted programming language that conforms to the ECMAScript specification. JavaScript has curly-bracket syntax, dynamic typing, prototype-based object-orientation, and first-class functions."
            ),
            Language(
                "Ruby",
                dateOf(1996, 12, 25),
                favorite = false,
                description = "Ruby is an interpreted, high-level, general-purpose programming language."
            ),
            Language(
                "Dart",
                dateOf(2013, 11, 14),
                favorite = true,
                description = "Dart is a client-optimized programming language for fast apps on multiple platforms."
            )
        )

        private fun dateOf(year: Int, month: Int, day: Int): Date {
            return GregorianCalendar(year, month - 1, day).time
        }
    }
}