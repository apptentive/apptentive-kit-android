package apptentive.com.android.core

import apptentive.com.android.util.InternalUseOnly
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A property delegation class that allows resetting its value to an uninitialized state.
 *
 * @param <T> the type of the delegated property
 * @param uninitializedValue the value representing an uninitialized state
 * @param initializer a lambda function that provides the initial value when accessed for first time
 */
@InternalUseOnly
class ResettableDelegate<T>(private val uninitializedValue: T, val initializer: () -> T) : ReadWriteProperty<Any, T> {

    private var _value: T = uninitializedValue

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (_value == uninitializedValue) {
            _value = initializer()
        }
        return _value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        _value = value
    }
}
