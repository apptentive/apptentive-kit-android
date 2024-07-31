package apptentive.com.android.core

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
open class ApptentiveException(message: String, cause: Throwable? = null) : Exception(message, cause)
