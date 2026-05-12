package apptentive.com.android.core.network

import apptentive.com.android.core.util.InternalUseOnly

@InternalUseOnly
enum class HttpMethod {
    GET,
    HEAD,
    POST,
    PUT,
    DELETE,
    CONNECT,
    OPTIONS,
    TRACE,
    PATCH
}
