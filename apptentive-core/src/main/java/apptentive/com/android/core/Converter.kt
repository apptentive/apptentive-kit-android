package apptentive.com.android.core

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface Converter<in Source : Any, out Target : Any> {
    fun convert(source: Source): Target
}
