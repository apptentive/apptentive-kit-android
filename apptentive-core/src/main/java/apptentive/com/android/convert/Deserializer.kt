package apptentive.com.android.convert

/**
 * Represents an abstract object deserializer while hiding
 * all the underlying implementation details.
 */
interface Deserializer {
    /**
     * Performs object de-serialization.
     * @return a de-serialized object.
     */
    fun deserialize(): Any
}