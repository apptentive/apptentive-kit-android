package apptentive.com.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.util.Log
import apptentive.com.app.databinding.ActivityDebugInfoBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)

        val isApptentiveTheme = prefs.getBoolean(EXTRA_APPTENTIVE_THEME, false)
        theme.applyStyle(if (isApptentiveTheme) R.style.Theme_Apptentive else R.style.AppTheme, true)

        val isNightMode = prefs.getBoolean(EXTRA_NIGHT_MODE, false)
        delegate.localNightMode = if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        val binding = ActivityDebugInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { onBackPressed() }

        binding.appIcon.setImageResource(applicationInfo.icon)
        val sdf = SimpleDateFormat(BUILD_TIME_FORMAT, Locale.US)
        val buildDateTime = sdf.format(BuildConfig.APP_BUILD_DATE)
        binding.appVersionText.text = getString(R.string.sdk_version, Constants.SDK_VERSION)
        binding.buildDateTimeText.text = getString(R.string.build_date_time, buildDateTime)

        setStyles(binding, isApptentiveTheme)
        setSDKInfo(binding)
        // setAppInfo(binding)
        setDeviceInfo(binding)

        binding.themeSwitch.isChecked = isApptentiveTheme
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(EXTRA_APPTENTIVE_THEME, isChecked).apply()
            startActivity(intent)
            finish()
        }

        binding.nightSwitch.isChecked = isNightMode
        binding.nightSwitch.setOnCheckedChangeListener { _, isChecked ->
            delegate.localNightMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            prefs.edit().putBoolean(EXTRA_NIGHT_MODE, isChecked).apply()
        }
    }

    @SuppressLint("ResourceType")
    private fun setStyles(binding: ActivityDebugInfoBinding, isApptentiveTheme: Boolean) {
        binding.stylesLayout.themeNameText.text = resources.getResourceEntryName(if (isApptentiveTheme) R.style.Theme_Apptentive else R.style.AppTheme)
        binding.stylesLayout.colorPrimaryText.text = getString(getColorFromAttr(R.attr.colorPrimary))
        binding.stylesLayout.colorOnPrimaryText.text = getString(getColorFromAttr(R.attr.colorOnPrimary))
        binding.stylesLayout.colorPrimaryVariantText.text = getString(getColorFromAttr(R.attr.colorPrimaryVariant))
        binding.stylesLayout.colorSecondaryText.text = getString(getColorFromAttr(R.attr.colorSecondary))
        binding.stylesLayout.colorSecondaryVariantText.text = getString(getColorFromAttr(R.attr.colorSecondaryVariant))
        binding.stylesLayout.colorOnSecondaryText.text = getString(getColorFromAttr(R.attr.colorOnSecondary))
        binding.stylesLayout.androidColorBackgroundText.text = getString(getColorFromAttr(android.R.attr.colorBackground))
        binding.stylesLayout.colorOnBackgroundText.text = getString(getColorFromAttr(R.attr.colorOnBackground))
        binding.stylesLayout.androidTextColorSecondaryText.text = getString(getColorFromAttr(android.R.attr.textColorSecondary))
        binding.stylesLayout.colorErrorText.text = getString(getColorFromAttr(R.attr.colorError))
    }

    @ColorRes
    private fun getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.resourceId
    }

    private fun setSDKInfo(binding: ActivityDebugInfoBinding) {
        val deviceItems = listOf(
            InfoItem("Key", configuration.apptentiveKey),
            InfoItem("Signature", configuration.apptentiveSignature),
            InfoItem("Log Level", Log.logLevel.name),
            InfoItem("Sanitize Sensitive Logs", configuration.shouldSanitizeLogMessages.toString()),
            InfoItem("API Version", Constants.API_VERSION.toString()),
            InfoItem("SDK Version", Constants.SDK_VERSION),
            InfoItem("Base URL", Constants.SERVER_URL),
        )
        val adapter = InfoItemAdapter(deviceItems)
        binding.sdkRecycler.adapter = adapter
    }

/*    private fun setAppInfo(binding: ActivityDebugInfoBinding) {
        val appInfo = RuntimeUtils.getApplicationInfo(this)
        val deviceItems = listOf(
            InfoItem("Label", getString(applicationInfo.labelRes)),
            InfoItem("Package Name", appInfo.packageName),
            InfoItem("Version Code", appInfo.versionCode.toString()),
            InfoItem("Version Name", appInfo.versionName),
            InfoItem("Target SDK Version", appInfo.targetSdkVersion.toString()),
            InfoItem("Min SDK Version", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) applicationInfo.minSdkVersion.toString() else "NEED SDK N+"),
            InfoItem("Build Type", BuildConfig.BUILD_TYPE),
            InfoItem("Debuggable", appInfo.debuggable.toString()),
        )
        val adapter = InfoItemAdapter(deviceItems)
        binding.appRecycler.adapter = adapter
    }*/

    private fun setDeviceInfo(binding: ActivityDebugInfoBinding) {
        val device = getDevice()

        // Display Info
        val displayMetrics = DisplayMetrics()
        val display = windowManager.defaultDisplay
        display.getMetrics(displayMetrics)
        val density = getDensityString(displayMetrics)

        // Network Info
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = cm.activeNetworkInfo
        val allNetworkInfo = cm.allNetworkInfo

        // Internet / WiFi / Mobile Data Connection
        val connectedToInternet = activeNetworkInfo?.isConnectedOrConnecting == true
        var connectedToWiFi = false
        var connectedToMobileData = false
        for (ni in allNetworkInfo) {
            if (ni.typeName.equals("WIFI") && ni.isConnected) connectedToWiFi = true
            if (ni.typeName.equals("MOBILE") && ni.isConnected) connectedToMobileData = true
        }

        val isEmulator = isDeviceAnEmulator()

        // Phone / Sim states
        val deviceCanMakeCalls = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        val simStateSlot1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) getSimState((getSystemService(TELEPHONY_SERVICE) as TelephonyManager).getSimState(0)) else "NEED SDK O+"
        val simStateSlot2 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) getSimState((getSystemService(TELEPHONY_SERVICE) as TelephonyManager).getSimState(1)) else "NEED SDK O+"

        val deviceItems = listOf(
            InfoItem("Operating System", "${device.osName} ${device.osVersion}"),
            InfoItem("OS API Level", device.osApiLevel.toString()),
            InfoItem("OS Build Version", device.osBuild),
            InfoItem("Manufacturer", device.manufacturer),
            InfoItem("Model", device.model),
            InfoItem("Is Device an Emulator", isEmulator.toString()),
            InfoItem("Connected to Internet", connectedToInternet.toString()),
            InfoItem("Connected by WiFi", connectedToWiFi.toString()),
            InfoItem("Connected by Mobile Data", connectedToMobileData.toString()),
            InfoItem("Device can make calls", deviceCanMakeCalls.toString()),
            InfoItem("SIM state status slot 1", simStateSlot1),
            InfoItem("SIM state status slot 2", simStateSlot2),
            InfoItem("Density Bucket", density),
            InfoItem("Density Multiplier", displayMetrics.density.toString()),
            InfoItem("Density DPI", displayMetrics.densityDpi.toString()),
            InfoItem("Pixels", "${displayMetrics.widthPixels}w x ${displayMetrics.heightPixels}h"),
            InfoItem("Scaled Density", displayMetrics.scaledDensity.toString()),
            InfoItem("Locale Country Code", device.localeCountryCode),
            InfoItem("Locale Language Code", device.localeLanguageCode),
            InfoItem("Locale Raw", device.localeRaw),
            InfoItem("UTC Offset", device.utcOffset.toString()),
            InfoItem("Board", device.board),
            InfoItem("Product", device.product),
            InfoItem("Brand", device.brand),
            InfoItem("CPU", device.cpu),
            InfoItem("Device", device.device),
            InfoItem("UUID", device.uuid),
            InfoItem("Build Type", device.buildType),
            InfoItem("Build ID", device.buildId),
            InfoItem("Carrier", device.carrier.toString()),
            InfoItem("Current Carrier", device.currentCarrier.toString()),
            InfoItem("Boot Loader Version", device.bootloaderVersion.toString()),
            InfoItem("Radio Version", device.radioVersion.toString()),
        )

        val adapter = InfoItemAdapter(deviceItems)
        binding.deviceRecycler.adapter = adapter
    }

    // Is Device an Emulator (taken from https://github.com/flutter/plugins/blob/master/packages/device_info/device_info/android/src/main/java/io/flutter/plugins/deviceinfo/MethodCallHandlerImpl.java#L115-L131)
    private fun isDeviceAnEmulator() =
        (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
            Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.startsWith("unknown") ||
            Build.HARDWARE.contains("goldfish") ||
            Build.HARDWARE.contains("ranchu") ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for x86") ||
            Build.MANUFACTURER.contains("Genymotion") ||
            Build.PRODUCT.contains("sdk_google") ||
            Build.PRODUCT.contains("google_sdk") ||
            Build.PRODUCT.contains("sdk") ||
            Build.PRODUCT.contains("sdk_x86") ||
            Build.PRODUCT.contains("vbox86p") ||
            Build.PRODUCT.contains("emulator") ||
            Build.PRODUCT.contains("simulator")

    private fun getDensityString(displayMetrics: DisplayMetrics) = when {
        displayMetrics.densityDpi <= DisplayMetrics.DENSITY_LOW -> "ldpi"
        displayMetrics.densityDpi <= DisplayMetrics.DENSITY_MEDIUM -> "mdpi"
        displayMetrics.densityDpi <= DisplayMetrics.DENSITY_HIGH -> "hdpi"
        displayMetrics.densityDpi <= DisplayMetrics.DENSITY_XHIGH -> "xhdpi"
        displayMetrics.densityDpi <= DisplayMetrics.DENSITY_XXHIGH -> "xxhdpi"
        displayMetrics.densityDpi <= DisplayMetrics.DENSITY_XXXHIGH -> "xxxhdpi"
        displayMetrics.densityDpi <= DisplayMetrics.DENSITY_TV -> "tvdpi"
        displayMetrics.densityDpi <= DisplayMetrics.DENSITY_DEVICE_STABLE -> "device_stable"
        else -> "Unknown"
    }

    private fun getSimState(simState: Int) = when (simState) {
        TelephonyManager.SIM_STATE_UNKNOWN -> "UNKNOWN OR NO SLOT"
        TelephonyManager.SIM_STATE_ABSENT -> "ABSENT"
        TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN REQUIRED"
        TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK REQUIRED"
        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "NETWORK LOCKED"
        TelephonyManager.SIM_STATE_READY -> "READY"
        TelephonyManager.SIM_STATE_NOT_READY -> "NOT READY"
        TelephonyManager.SIM_STATE_PERM_DISABLED -> "PERM DISABLED"
        TelephonyManager.SIM_STATE_CARD_IO_ERROR -> "CARD IO ERROR"
        TelephonyManager.SIM_STATE_CARD_RESTRICTED -> "CARD RESTRICTED"
        else -> "LOADED OR PRESENT"
    }

    private fun getDevice() = Device(
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
        carrier = (getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).simOperatorName,
        currentCarrier = (getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).networkOperatorName,
        bootloaderVersion = Build::class.java.getField("BOOTLOADER").get(null) as String,
        radioVersion = Build.getRadioVersion(),
        localeCountryCode = Locale.getDefault().country,
        localeLanguageCode = Locale.getDefault().language,
        localeRaw = Locale.getDefault().toString(),
        utcOffset = TimeZone.getDefault().rawOffset / 1000
    )
}
