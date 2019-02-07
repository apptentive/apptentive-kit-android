package apptentive.com.android.network

abstract class HttpHeaders : Iterable<Map.Entry<String, String>> {
    abstract val size: Int
    abstract operator fun get(name: String): String?

    companion object {
        const val acceptHeader = "accept"
        const val acceptCharsetHeader = "accept-charset"
        const val acceptEncodingHeader = "accept-encoding"
        const val acceptLanguageHeader = "accept-language"
        const val acceptRangesHeader = "accept-ranges"
        const val ageHeader = "age"
        const val allowHeader = "allow"
        const val authorizationHeader = "authorization"
        const val cacheControlHeader = "cache-control"
        const val connectionHeader = "connection"
        const val contentEncodingHeader = "content-encoding"
        const val contentLanguageHeader = "content-language"
        const val contentLengthHeader = "content-length"
        const val contentLocationHeader = "content-location"
        const val contentMD5Header = "content-md5"
        const val contentRangeHeader = "content-range"
        const val contentTypeHeader = "content-type"
        const val dateHeader = "date"
        const val etagHeader = "etag"
        const val expectHeader = "expect"
        const val expiresHeader = "expires"
        const val fromHeader = "from"
        const val hostHeader = "host"
        const val ifMatchHeader = "if-match"
        const val ifModifiedSinceHeader = "if-modified-since"
        const val ifNoneMatchHeader = "if-none-match"
        const val ifRangeHeader = "if-range"
        const val ifUnmodifiedSinceHeader = "if-unmodified-since"
        const val lastModifiedHeader = "last-modified"
        const val locationHeader = "location"
        const val maxForwardsHeader = "max-forwards"
        const val pragmaHeader = "pragma"
        const val proxyAuthenticateHeader = "proxy-authenticate"
        const val proxyAuthorizationHeader = "proxy-authorization"
        const val rangeHeader = "range"
        const val refererHeader = "referer"
        const val retryAfterHeader = "retry-after"
        const val serverHeader = "server"
        const val teHeader = "te"
        const val trailerHeader = "trailer"
        const val transferEncodingHeader = "transfer-encoding"
        const val upgradeHeader = "upgrade"
        const val userAgentHeader = "user-agent"
        const val varyHeader = "vary"
        const val viaHeader = "via"
        const val warningHeader = "warning"
        const val wwwAuthenticateHeader = "www-authenticate"
    }
}

class MutableHttpHeaders : HttpHeaders() {
    private val headers = mutableMapOf<String, String>()

    override val size: Int get() = headers.size

    override fun get(name: String): String? = headers[name]

    fun set(name: String, value: Int) {
        set(name, value.toString())
    }

    fun set(name: String, value: Boolean) {
        set(name, value.toString())
    }

    operator fun set(name: String, value: String) {
        headers[name] = value
    }

    override fun iterator(): Iterator<Map.Entry<String, String>> {
        return headers.iterator()
    }
}
