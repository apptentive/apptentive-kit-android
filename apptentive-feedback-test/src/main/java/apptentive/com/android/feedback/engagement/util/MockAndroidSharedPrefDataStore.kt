package apptentive.com.android.feedback.engagement.util

import android.content.SharedPreferences
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import apptentive.com.android.platform.SharedPrefConstants.CRYPTO_ENABLED
import io.mockk.mockk

class MockAndroidSharedPrefDataStore(private val containsKey: Boolean = true, private val isEncryptionEnabled: Boolean = false) : AndroidSharedPrefDataStore {

    private var version = ""

    override fun putString(file: String, keyEntry: String, value: String) {
        version = value
    }

    override fun putBoolean(file: String, keyEntry: String, value: Boolean) {
    }

    override fun getBoolean(file: String, keyEntry: String, defaultValue: Boolean): Boolean {
        return if (keyEntry == CRYPTO_ENABLED) isEncryptionEnabled
        else false
    }

    override fun getString(file: String, keyEntry: String, defaultValue: String): String {
        return version
    }

    override fun getSharedPrefForSDK(file: String): SharedPreferences {
        return mockk<SharedPreferences>()
    }

    override fun containsKey(file: String, keyEntry: String): Boolean {
        return containsKey
    }
}
