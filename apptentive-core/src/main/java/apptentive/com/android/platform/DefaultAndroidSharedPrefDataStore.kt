package apptentive.com.android.platform

import android.content.Context
import android.content.SharedPreferences
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
class DefaultAndroidSharedPrefDataStore(val context: Context) : AndroidSharedPrefDataStore {
    override fun getSharedPrefForSDK(file: String): SharedPreferences =
        context.getSharedPreferences(file, Context.MODE_PRIVATE)

    override fun containsKey(file: String, keyEntry: String): Boolean =
        context.getSharedPreferences(file, Context.MODE_PRIVATE).contains(keyEntry)

    override fun getBoolean(file: String, keyEntry: String, defaultValue: Boolean): Boolean =
        context.getSharedPreferences(file, Context.MODE_PRIVATE).getBoolean(keyEntry, defaultValue)

    override fun getString(file: String, keyEntry: String, defaultValue: String): String =
        context.getSharedPreferences(file, Context.MODE_PRIVATE).getString(keyEntry, defaultValue) ?: ""

    override fun putBoolean(file: String, keyEntry: String, value: Boolean) {
        context.getSharedPreferences(file, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(keyEntry, value)
            .apply()
    }

    override fun putString(file: String, keyEntry: String, value: String) {
        context.getSharedPreferences(file, Context.MODE_PRIVATE)
            .edit()
            .putString(keyEntry, value)
            .apply()
    }
}
