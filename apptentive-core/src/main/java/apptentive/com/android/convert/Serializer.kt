package apptentive.com.android.convert

/**
 * Represents an abstract object serializer while hiding
 * all the underlying implementation details.
 */
interface Serializer {
    /**
     * Performs object serialization.
     * @param obj an object to be serialized.
     */
    fun serialize(obj: Any)
}