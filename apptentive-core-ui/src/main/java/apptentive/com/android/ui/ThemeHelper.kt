package apptentive.com.android.ui

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import apptentive.com.android.R
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags

fun Context.overrideTheme() {
    /* Step 1: Apply Apptentive default theme layer.
	 * If host activity is an activity, the base theme already has Apptentive defaults applied, so skip Step 1.
	 * If parent activity is NOT an activity, first apply Apptentive defaults.
	 */
    if (this !is Activity) {
        theme.applyStyle(R.style.Theme_Apptentive, true)
    }

    /* Step 2: Inherit app default theme */
    val appThemeId = getAppThemeId()
    if (appThemeId != null) {
        theme.applyStyle(appThemeId, true)
    }

    /* Step 3: Restore Apptentive UI window properties that may have been overridden in Step 2. This theme
     * is to ensure Apptentive interaction has a modal feel-n-look.
     */
    // TODO: we don't have any specific things yet but when we do - they must be applied here

    /* Step 4: Apply optional theme override specified in host app's style */
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