package apptentive.com.android.network

import apptentive.com.android.util.InternalUseOnly

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
