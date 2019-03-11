package apptentive.com.android.love

enum class SentimentType {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

data class Sentiment(val identifier: String, val type: SentimentType)


