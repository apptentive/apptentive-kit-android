package apptentive.com.android.feedback.platform

import android.content.Context
import android.os.Build
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.util.Factory

abstract class AndroidDeviceFactory(
    private val context: Context
) : Factory<Device> {
    override fun create(): Device {
        return Device(
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
            uuid = AndroidDeviceUtils.getAndroidID(context),
            buildType = Build.TYPE,
            buildId = Build.ID,
            advertiserId = null, // FIXME: collect advertiser id
            carrier = AndroidDeviceUtils.getSimOperatorName(context),
            currentCarrier = AndroidDeviceUtils.getNetworkOperatorName(context),
            networkType = AndroidDeviceUtils.getNetworkType(context),
            bootloaderVersion = AndroidDeviceUtils.getBootloaderVersion(),
            radioVersion = AndroidDeviceUtils.getRadioVersion(),
            localeCountryCode = AndroidDeviceUtils.getLocaleCountryCode(),
            localeLanguageCode = AndroidDeviceUtils.getLocaleLanguageCode(),
            localeRaw = AndroidDeviceUtils.getLocaleRaw(),
            utcOffset = AndroidDeviceUtils.getUtcOffset()
        )
    }
}

