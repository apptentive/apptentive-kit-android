package apptentive.com.android.network

import apptentive.com.android.util.InternalUseOnly
import java.lang.StringBuilder

@InternalUseOnly
data class HttpHeader(val name: String, val value: String) {
    override fun toString(): String {
        return "$name: \"$value\""
    }
}

@InternalUseOnly
open class HttpHeaders(headers: Map<String, HttpHeader> = emptyMap()) : Iterable<HttpHeader> {
    protected val headers = mutableMapOf<String, HttpHeader>().apply { putAll(headers) }

    val size: Int get() = headers.size
    operator fun get(name: String): HttpHeader? = headers[name]

    override fun iterator(): Iterator<HttpHeader> {
        return headers.values.iterator()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpHeaders

        if (headers != other.headers) return false

        return true
    }

    override fun hashCode(): Int {
        return headers.hashCode()
    }

    override fun toString(): String {
        val result = StringBuilder()
        for (header in headers.values) {
            if (result.isNotEmpty()) {
                result.append('\n')
            }
            result.append(header)
        }
        return result.toString()
    }

    companion object {
        const val ACCEPT_ENCODING = "Accept-Encoding"
        const val CACHE_CONTROL = "Cache-Control"
        const val CONTENT_ENCODING = "Content-Encoding"
        const val CONTENT_LENGTH = "Content-Length"
        const val CONTENT_TYPE = "Content-Type"
    }
}

@InternalUseOnly
class MutableHttpHeaders : HttpHeaders() {
    operator fun set(name: String, value: Int) {
        set(name, value.toString())
    }

    operator fun set(name: String, value: Boolean) {
        set(name, value.toString())
    }

    operator fun set(name: String, value: String) {
        headers[name] = HttpHeader(name, value)
    }

    fun addAll(headers: HttpHeaders) {
        for (header in headers) {
            this[header.name] = header.value
        }
    }

    fun clear() {
        headers.clear()
    }
}
