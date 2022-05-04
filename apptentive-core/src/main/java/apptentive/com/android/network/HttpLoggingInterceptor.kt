package apptentive.com.android.network

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface HttpLoggingInterceptor {
    fun intercept(request: HttpRequest<*>)
    fun intercept(response: HttpNetworkResponse)
    fun retry(request: HttpRequest<*>, delay: TimeInterval)
}
