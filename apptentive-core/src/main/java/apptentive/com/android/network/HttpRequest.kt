package apptentive.com.android.network

class HttpRequest(val url: String, val method: HttpMethod = HttpMethod.GET) {
    private val headers: HttpHeaders = HttpHeaders()
}