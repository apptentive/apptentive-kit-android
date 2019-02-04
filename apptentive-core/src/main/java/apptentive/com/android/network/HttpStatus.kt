package apptentive.com.android.network

object HttpStatus {
    const val ok = 200
    const val created = 201
    const val accepted = 202
    const val nonAuthoritativeInformation = 203
    const val noContent = 204
    const val resetContent = 205
    const val partialContent = 206
    const val multipleChoices = 300
    const val movedPermanently = 301
    const val found = 302
    const val movedTemporarily = 302
    const val seeOther = 303
    const val notModified = 304
    const val useProxy = 305
    const val temporaryRedirect = 307
    const val badRequest = 400
    const val unauthorized = 401
    const val paymentRequired = 402
    const val forbidden = 403
    const val notFound = 404
    const val methodNotAllowed = 405
    const val notAcceptable = 406
    const val proxyAuthenticationRequired = 407
    const val requestTimeout = 408
    const val conflict = 409
    const val gone = 410
    const val lengthRequired = 411
    const val preconditionFailed = 412
    const val requestEntityTooLarge = 413
    const val requestUriTooLong = 414
    const val unsupportedMediaType = 415
    const val requestedRangeNotSatisfiable = 416
    const val expectationFailed = 417
    const val upgradeRequired = 426
    const val internalServerError = 500
    const val notImplemented = 501
    const val badGateway = 502
    const val serviceUnavailable = 503
    const val gatewayTimeout = 504
    const val httpVersionNotSupported = 505
}