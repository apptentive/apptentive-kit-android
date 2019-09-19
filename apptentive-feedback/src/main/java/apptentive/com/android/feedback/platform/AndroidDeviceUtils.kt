package apptentive.com.android.feedback.platform

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings.Secure.ANDROID_ID
import android.provider.Settings.Secure.getString
import android.telephony.TelephonyManager
import apptentive.com.android.feedback.SYSTEM
import apptentive.com.android.util.Log
import java.util.*

internal object AndroidDeviceUtils {
    @SuppressLint("HardwareIds")
    fun getAndroidID(context: Context): String = getString(context.contentResolver, ANDROID_ID)

    //region Telephony

    /**
     * A list of mobile carrier network types as Strings.
     * From [TelephonyManager][android.telephony.TelephonyManager]
     * @see android.telephony.TelephonyManager
     */
    private val networkTypeLookup = arrayOf(
        "UNKNOWN", //  0
        "GPRS", //  1
        "EDGE", //  2
        "UMTS", //  3
        "CDMA", //  4
        "EVDO_0", //  5
        "EVDO_A", //  6
        "1xRTT", //  7
        "HSDPA", //  8
        "HSUPA", //  9
        "HSPA", // 10
        "IDEN", // 11
        "EVDO_B", // 12
        "LTE", // 13
        "EHRPD", // 14
        "HSPAP", // 15
        "GSM", // 16
        "TD_SCDMA", // 17
        "IWLAN",   // 18
        "LTE_CA" //19
    )

    fun getNetworkType(context: Context): String {
        val networkType = getTelephonyManager(context).networkType
        return if (networkType >= 0 && networkType < networkTypeLookup.size)
            networkTypeLookup[networkType] else "UNKNOWN"
    }

    fun getSimOperatorName(context: Context): String? =
        try {
            getTelephonyManager(context).simOperatorName
        } catch (e: Exception) {
            Log.e(SYSTEM, "Exception while resolving SIM operator name", e)
            null
        }

    fun getNetworkOperatorName(context: Context): String? = try {
        getTelephonyManager(context).networkOperatorName
    } catch (e: Exception) {
        Log.e(SYSTEM, "Exception while resolving network operator name", e)
        null
    }

    private fun getTelephonyManager(context: Context) =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    //endregion

    //region Firmware

    fun getBootloaderVersion(): String? =
        try {
            Build::class.java.getField("BOOTLOADER").get(null) as String // TODO: use util class
        } catch (e: Exception) {
            Log.e(SYSTEM, "Exception while resolving simOperatorName", e)
            null
        }

    /**
     * Returns the version string for the radio firmware.
     * May return null (if, for instance, the radio is not currently on).
     */
    fun getRadioVersion(): String? {
        return Build.getRadioVersion()
    }

    //endregion

    //region Locale

    fun getLocaleCountryCode(): String = Locale.getDefault().country
    fun getLocaleLanguageCode(): String = Locale.getDefault().language
    fun getLocaleRaw() = Locale.getDefault().toString()
    fun getUtcOffset() = TimeZone.getDefault().rawOffset / 1000

    //endregion
}