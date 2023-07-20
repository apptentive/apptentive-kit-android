package apptentive.com.app

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

data class Device(
    val osName: String,
    val osVersion: String,
    val osBuild: String,
    val osApiLevel: Int,
    val manufacturer: String,
    val model: String,
    val board: String,
    val product: String,
    val brand: String,
    val cpu: String,
    val device: String,
    val uuid: String,
    val buildType: String,
    val buildId: String,
    val carrier: String? = null,
    val currentCarrier: String? = null,
    val networkType: String? = null,
    val bootloaderVersion: String? = null,
    val radioVersion: String? = null,
    val localeCountryCode: String,
    val localeLanguageCode: String,
    val localeRaw: String,
    val utcOffset: Int,
)

fun getDevice(context: Context) = Device(
    osName = "Android",
    osVersion = Build.VERSION.RELEASE,
    osBuild = Build.VERSION.INCREMENTAL,
    osApiLevel = Build.VERSION.SDK_INT,
    manufacturer = Build.MANUFACTURER,
    model = Build.MODEL,
    board = Build.BOARD,
    product = Build.PRODUCT,
    brand = Build.BRAND,
    cpu = Build.CPU_ABI,
    device = Build.DEVICE,
    uuid = UUID.randomUUID().toString(),
    buildType = Build.TYPE,
    buildId = Build.ID,
    carrier = (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).simOperatorName,
    currentCarrier = (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).networkOperatorName,
    bootloaderVersion = Build::class.java.getField("BOOTLOADER").get(null) as String,
    radioVersion = Build.getRadioVersion(),
    localeCountryCode = Locale.getDefault().country,
    localeLanguageCode = Locale.getDefault().language,
    localeRaw = Locale.getDefault().toString(),
    utcOffset = TimeZone.getDefault().rawOffset / 1000
)
