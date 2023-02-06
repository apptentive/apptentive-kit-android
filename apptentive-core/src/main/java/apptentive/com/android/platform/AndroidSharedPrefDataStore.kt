package apptentive.com.android.platform

import android.content.SharedPreferences
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface AndroidSharedPrefDataStore {
    fun getSharedPrefForSDK(file: String): SharedPreferences
    fun getString(file: String, keyEntry: String, defaultValue: String = ""): String
    fun getBoolean(file: String, keyEntry: String, defaultValue: Boolean = false): Boolean
    fun putString(file: String, keyEntry: String, value: String)
    fun putBoolean(file: String, keyEntry: String, value: Boolean)
    fun containsKey(file: String, keyEntry: String): Boolean
}
