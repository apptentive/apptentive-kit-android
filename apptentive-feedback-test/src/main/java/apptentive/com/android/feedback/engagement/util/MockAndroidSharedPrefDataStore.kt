package apptentive.com.android.feedback.engagement.util

import android.content.SharedPreferences
import apptentive.com.android.core.getTimeSeconds
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import apptentive.com.android.platform.SharedPrefConstants.CRYPTO_ENABLED
import apptentive.com.android.platform.SharedPrefConstants.FAN_SIGNAL_TIME_STAMP
import io.mockk.mockk

class MockAndroidSharedPrefDataStore(private val containsKey: Boolean = true, private val isEncryptionEnabled: Boolean = false) : AndroidSharedPrefDataStore {

    private var version = ""

    override fun deleteSharedPrefForSDK(file: String, mode: Int) {
    }

    override fun putString(file: String, keyEntry: String, value: String?) {
        version = value ?: ""
    }

    override fun putBoolean(file: String, keyEntry: String, value: Boolean) {
    }

    override fun getBoolean(file: String, keyEntry: String, defaultValue: Boolean): Boolean {
        return if (keyEntry == CRYPTO_ENABLED) isEncryptionEnabled
        else false
    }

    override fun getString(file: String, keyEntry: String, defaultValue: String): String {
        return if (keyEntry == FAN_SIGNAL_TIME_STAMP)
            getTimeSeconds().toString()
        else
            version
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
