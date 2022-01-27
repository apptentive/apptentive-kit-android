package apptentive.com.android.ui

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.view.ContextThemeWrapper
import apptentive.com.android.R
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags

/**
 * Allows inheritance of app's theme
 *
 * Layers on themes on top of each other by importance.
 * Later layers will override previous layers and take priority.
 */
internal fun Context.overrideTheme() {
    /* Layer 1: Apptentive default theme.
	 * If host activity is an activity, the base theme already has Apptentive defaults applied, so skip layer 1.
	 * If parent activity is NOT an activity, first apply Apptentive defaults.
	 */
    if (this !is Activity) {
        theme.applyStyle(R.style.Theme_Apptentive, true)
    }

    /* Layer 2: App default theme */
    applyAppTheme()

    /* Layer 3: Disable the problem style -> android:background */
    theme.applyStyle(R.style.DisableAndroidBackgroundStyle, true)

    /* Layer 4: Optional theme override specified in host app's style */
    applyApptentiveThemeOverride()
}

/**
 * Allows the usage of ContextThemeWrapper to overwrite base Apptentive theme values
 */
fun ContextThemeWrapper.overrideTheme() {
    val contextTheme = themeResId

    /* Layer 1: Apptentive default theme */
    theme.applyStyle(R.style.Theme_Apptentive, true)

    /* Layer 2: ContextThemeWrapper theme the wrapper was created with */
    theme.applyStyle(contextTheme, true)

    /* Layer 3: App default theme */
    applyAppTheme()

    /* Layer 4: Disable the problem style -> android:background */
    theme.applyStyle(R.style.DisableAndroidBackgroundStyle, true)

    /* Layer 5: Optional theme override specified in host app's style */
    applyApptentiveThemeOverride()
}

private fun Context.applyAppTheme() {
    val appThemeId = getAppThemeId()
    if (appThemeId != null) {
        theme.applyStyle(appThemeId, true)
    }
}

private fun Context.applyApptentiveThemeOverride() {
    val themeOverrideResId: Int = resources.getIdentifier(
        "ApptentiveThemeOverride",
        "style", packageName
    )
    if (themeOverrideResId != 0) {
        theme.applyStyle(themeOverrideResId, true)
    }
}

/**
 * A style resource identifier (in the package's resources) of the
 * default visual theme of the application. From the "theme" attribute
 * or, if not set, null.
 */
private fun Context.getAppThemeId(): Int? {
    try {
        val appPackageName = packageName
        val packageManager = packageManager
        val packageInfo = packageManager.getPackageInfo(
            appPackageName,
            PackageManager.GET_META_DATA or PackageManager.GET_RECEIVERS
        )
        val ai = packageInfo.applicationInfo
        val theme = ai.theme
        if (theme != 0) {
            return theme
        }
    } catch (e: Exception) {
        Log.e(LogTags.CORE, "Unable to resolve application default theme", e)
    }
    return null
}