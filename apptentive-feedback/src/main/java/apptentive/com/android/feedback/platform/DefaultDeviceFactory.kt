package apptentive.com.android.feedback.platform

import android.content.Context
import android.os.Build
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.util.Factory

// TODO: rename to AndroidDefaultDeviceFactory
class DefaultDeviceFactory(
    private val context: Context
) : Factory<Device> {
    @Suppress("DEPRECATION")
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
            uuid = AndroidUtils.getAndroidID(context),
            buildType = Build.TYPE,
            buildId = Build.ID,
            advertiserId = null, // FIXME: collect advertiser id
            carrier = AndroidUtils.getSimOperatorName(context),
            currentCarrier = AndroidUtils.getNetworkOperatorName(context),
            networkType = AndroidUtils.getNetworkType(context),
            bootloaderVersion = AndroidUtils.getBootloaderVersion(),
            radioVersion = AndroidUtils.getRadioVersion(),
            localeCountryCode = AndroidUtils.getLocaleCountryCode(),
            localeLanguageCode = AndroidUtils.getLocaleLanguageCode(),
            localeRaw = AndroidUtils.getLocaleRaw(),
            utcOffset = AndroidUtils.getUtcOffset()
        )
    }
}

