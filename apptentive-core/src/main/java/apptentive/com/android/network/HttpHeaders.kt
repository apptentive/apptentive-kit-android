package apptentive.com.android.network

data class HttpHeader(val name: String, val value: String) {
    override fun toString(): String {
        return "$name: \"$value\""
    }
}

open class HttpHeaders(headers: Map<String, HttpHeader> = mapOf()) : Iterable<HttpHeader> {
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

    companion object {
        const val ACCEPT = "accept"
        const val ACCEPT_CHARSET = "accept-charset"
        const val ACCEPT_ENCODING = "accept-encoding"
        const val ACCEPT_LANGUAGE = "accept-language"
        const val ACCEPT_RANGES = "accept-ranges"
        const val AGE = "age"
        const val ALLOW = "allow"
        const val AUTHORIZATION = "authorization"
        const val CACHE_CONTROL = "cache-control"
        const val CONNECTION = "connection"
        const val CONTENT_ENCODING = "content-encoding"
        const val CONTENT_LANGUAGE = "content-language"
        const val CONTENT_LENGTH = "content-length"
        const val CONTENT_LOCATION = "content-location"
        const val CONTENT_MD5 = "content-md5"
        const val CONTENT_RANGE = "content-range"
        const val CONTENT_TYPE = "content-type"
        const val DATE = "date"
        const val ETAG = "etag"
        const val EXPECT = "expect"
        const val EXPIRES = "expires"
        const val FROM = "from"
        const val HOST = "host"
        const val IF_MATCH = "if-match"
        const val IF_MODIFIED_SINCE = "if-modified-since"
        const val IF_NONE_MATCH = "if-none-match"
        const val IF_RANGE = "if-range"
        const val IF_UNMODIFIED_SINCE = "if-unmodified-since"
        const val LAST_MODIFIED = "last-modified"
        const val LOCATION = "location"
        const val MAX_FORWARDS = "max-forwards"
        const val PRAGMA = "pragma"
        const val PROXY_AUTHENTICATE = "proxy-authenticate"
        const val PROXY_AUTHORIZATION = "proxy-authorization"
        const val RANGE = "range"
        const val REFERER = "referer"
        const val RETRY_AFTER = "retry-after"
        const val SERVER = "server"
        const val TE = "te"
        const val TRAILER = "trailer"
        const val TRANSFER_ENCODING = "transfer-encoding"
        const val UPGRADE = "upgrade"
        const val USER_AGENT = "user-agent"
        const val VARY = "vary"
        const val VIA = "via"
        const val WARNING = "warning"
        const val WWW_AUTHENTICATE = "www-authenticate"
    }
}

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
