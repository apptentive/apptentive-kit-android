package apptentive.com.android.network

interface HttpRequest {
    val url: String // TODO: should we use more specific type?
    val method: HttpMethod
    val body: ByteArray
    val headers: HttpHeaders
}