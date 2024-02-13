package apptentive.com.android.feedback.textmodal

import android.view.ViewGroup
import android.widget.LinearLayout

internal fun getAlternateTextGravity(scaleType: LayoutOptions): Int {
    return when (scaleType) {
        LayoutOptions.FULL_WIDTH -> android.view.Gravity.CENTER
        LayoutOptions.CENTER -> android.view.Gravity.CENTER
        LayoutOptions.ALIGN_LEFT -> android.view.Gravity.START
        LayoutOptions.ALIGN_RIGHT -> android.view.Gravity.END
    }
}

internal fun getAdjustedDeviceDensity(deviceDensity: Float): Float = when {
    deviceDensity <= 1 -> 1f // mdpi
    deviceDensity <= 1.5 -> 1.5f // hdpi
    deviceDensity <= 2 -> 2f // xhdpi
    deviceDensity <= 3 -> 3f // xxhdpi
    else -> 4f // xxxhdpi
}

internal fun getAdjustedModalHeight(maxModalHeight: Int, defaultModalHeight: Int, maxHeightPercentage: Int): Int {
    val newCalculatedHeight = (maxModalHeight * maxHeightPercentage / 100) + (defaultModalHeight * 0.05).toInt() // extra padding to support stacked buttons
    return if (defaultModalHeight > newCalculatedHeight) newCalculatedHeight else defaultModalHeight
}

internal fun getPaddingForTheImagePositioning(paddingFromDimen: Float, scaleType: LayoutOptions): Int {
    return if (scaleType != LayoutOptions.FULL_WIDTH) paddingFromDimen.toInt() else 0
}

internal fun getLayoutParamsForTheImagePositioning(currentLayoutParams: LinearLayout.LayoutParams, imageHeight: Int, scaleType: LayoutOptions): ViewGroup.LayoutParams {
    return when (scaleType) {
        LayoutOptions.FULL_WIDTH -> {
            currentLayoutParams.apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = imageHeight
            }
        }
        LayoutOptions.CENTER -> currentLayoutParams
        LayoutOptions.ALIGN_LEFT -> currentLayoutParams.apply {
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            gravity = android.view.Gravity.START
        }
        LayoutOptions.ALIGN_RIGHT -> currentLayoutParams.apply {
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            gravity = android.view.Gravity.END
        }
    }
}

internal fun getImageScaleTypeFromConfig(scaleType: LayoutOptions): android.widget.ImageView.ScaleType = when (scaleType) {
    LayoutOptions.FULL_WIDTH -> android.widget.ImageView.ScaleType.FIT_XY
    LayoutOptions.CENTER -> android.widget.ImageView.ScaleType.CENTER_INSIDE
    LayoutOptions.ALIGN_LEFT -> android.widget.ImageView.ScaleType.FIT_START
    LayoutOptions.ALIGN_RIGHT -> android.widget.ImageView.ScaleType.FIT_END
}
