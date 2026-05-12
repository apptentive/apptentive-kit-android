package apptentive.com.android.core

import apptentive.com.android.core.util.InternalUseOnly

@InternalUseOnly
open class ApptentiveException(message: String, cause: Throwable? = null) : Exception(message, cause)
