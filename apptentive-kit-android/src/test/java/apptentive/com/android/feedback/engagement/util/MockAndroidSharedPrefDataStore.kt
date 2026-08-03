package apptentive.com.android.feedback.engagement.util

import android.content.SharedPreferences
import apptentive.com.android.core.platform.AndroidSharedPrefDataStore
import apptentive.com.android.core.platform.SharedPrefConstants.CRYPTO_ENABLED
import apptentive.com.android.core.platform.SharedPrefConstants.SDK_VERSION
import io.mockk.mockk

class MockAndroidSharedPrefDataStore(private val containsKey: Boolean = true, private val isEncryptionEnabled: Boolean = false) :
    AndroidSharedPrefDataStore {

    private var version = ""
    private val booleans = mutableMapOf<String, Boolean>()

    override fun deleteSharedPrefForSDK(file: String, mode: Int) {
    }

    override fun putString(file: String, keyEntry: String, value: String?) {
        version = value ?: ""
    }

    override fun putBoolean(file: String, keyEntry: String, value: Boolean) {
        booleans["$file:$keyEntry"] = value
    }

    override fun getBoolean(file: String, keyEntry: String, defaultValue: Boolean): Boolean {
        // CRYPTO_ENABLED is driven by the constructor flag, not by stored writes, so encryption
        // tests keep asserting on the value they set up.
        if (keyEntry == CRYPTO_ENABLED) return isEncryptionEnabled
        return booleans["$file:$keyEntry"] ?: defaultValue
    }

    override fun getString(file: String, keyEntry: String, defaultValue: String): String {
        return when (keyEntry) {
            SDK_VERSION -> "7.2.0"
            CRYPTO_ENABLED -> isEncryptionEnabled.toString()
            else -> version
        }
    }

    override fun getNullableString(file: String, keyEntry: String, defaultValue: String?): String? {
        return null
    }

    override fun getSharedPrefForSDK(file: String): SharedPreferences {
        return mockk<SharedPreferences>()
    }

    override fun containsKey(file: String, keyEntry: String): Boolean {
        return containsKey
    }

    override fun getInt(file: String, keyEntry: String, defaultValue: Int): Int {
        return 0
    }

    override fun putInt(file: String, keyEntry: String, value: Int) {
    }

    override fun getLong(file: String, keyEntry: String, defaultValue: Long): Long {
        return 0
    }

    override fun putLong(file: String, keyEntry: String, value: Long) {
    }
}
