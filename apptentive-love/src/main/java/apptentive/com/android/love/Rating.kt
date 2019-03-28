package apptentive.com.android.love

class Rating(identifier: String, private val score: Float) : LoveEntity(identifier) {
    override fun toString(): String {
        return "${super.toString()}: $score"
    }
}