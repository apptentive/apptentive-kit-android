package apptentive.com.android.network

// TODO: doc-comments
interface HttpRequestListener<T> {
    fun onSuccess(request: HttpRequest<T>, response: HttpResponse<T>)
    fun onError(request: HttpResponse<T>, error: Exception)
}
