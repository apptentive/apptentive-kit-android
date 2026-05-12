package apptentive.com.android.core

import apptentive.com.android.core.util.InternalUseOnly

/** Provider interface which is responsible for getting instances of the particular class. */

@InternalUseOnly
interface Provider<T> {
    fun get(): T
}
