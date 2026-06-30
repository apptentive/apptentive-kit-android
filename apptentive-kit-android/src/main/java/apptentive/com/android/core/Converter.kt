package apptentive.com.android.core

internal interface Converter<in Source : Any, out Target : Any> {
    fun convert(source: Source): Target
}
