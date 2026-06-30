package apptentive.com.android.core.network

import apptentive.com.android.core.TimeInterval

internal interface HttpLoggingInterceptor {
    fun intercept(request: HttpRequest<*>)
    fun intercept(response: HttpNetworkResponse)
    fun retry(request: HttpRequest<*>, delay: TimeInterval)
}
