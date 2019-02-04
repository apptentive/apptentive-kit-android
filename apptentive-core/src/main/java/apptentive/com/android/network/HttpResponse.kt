package apptentive.com.android.network

interface HttpResponse {
    val headers: HttpHeaders
    val statusCode: Int
    val reasonPhrase: String
    val contentLength: Int
    val body: ByteArray
}