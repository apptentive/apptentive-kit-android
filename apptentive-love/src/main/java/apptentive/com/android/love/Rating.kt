package apptentive.com.android.love

import java.lang.IllegalArgumentException

class Rating(identifier: String, val score: Int) : LoveEntity(identifier) {
    init {
        if (score < 0 || score > 5) {
            throw IllegalArgumentException("Rating score must be within range [1..5]: $score")
        }
    }

    override fun toString(): String {
        return "${super.toString()}: $score"
    }
}