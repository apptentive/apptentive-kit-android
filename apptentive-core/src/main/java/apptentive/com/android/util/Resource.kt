package apptentive.com.android.util

/**
 * A generic class that contains data and status about loading this data.
 */
internal data class Resource<T>(
    val status: Status,
    val data: T?,
    val message: String?
) {
    enum class Status {
        SUCCESS, ERROR, LOADING
    }

    companion object {
        fun <T> success(data: T): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T? = null): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}
