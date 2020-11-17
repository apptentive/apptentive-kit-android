package apptentive.com.android.network

interface HttpLoggingInterceptor {
    fun intercept(request: HttpRequest<*>)
    fun intercept(response: HttpNetworkResponse)
}