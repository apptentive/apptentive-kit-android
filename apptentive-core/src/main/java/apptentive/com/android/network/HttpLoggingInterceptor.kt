package apptentive.com.android.network

import apptentive.com.android.core.TimeInterval

interface HttpLoggingInterceptor {
    fun intercept(request: HttpRequest<*>)
    fun intercept(response: HttpNetworkResponse)
    fun retry(request: HttpRequest<*>, delay: TimeInterval)
}
