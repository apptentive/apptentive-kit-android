package apptentive.com.android.feedback.notes

import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import apptentive.com.android.TestCase
import apptentive.com.android.feedback.textmodal.LayoutOptions
import apptentive.com.android.feedback.textmodal.getAdjustedDeviceDensity
import apptentive.com.android.feedback.textmodal.getAdjustedModalHeight
import apptentive.com.android.feedback.textmodal.getAlternateTextGravity
import apptentive.com.android.feedback.textmodal.getImageScaleTypeFromConfig
import apptentive.com.android.feedback.textmodal.getLayoutParamsForTheImagePositioning
import apptentive.com.android.feedback.textmodal.getPaddingForTheImagePositioning
import junit.framework.TestCase.assertEquals
import org.junit.Test

class RichPromptsHelperTest : TestCase() {
    @Test
    fun testGetAlternateTextGravity() {
        assertEquals(Gravity.CENTER, getAlternateTextGravity(LayoutOptions.FULL_WIDTH))
        assertEquals(Gravity.CENTER, getAlternateTextGravity(LayoutOptions.CENTER))
        assertEquals(Gravity.START, getAlternateTextGravity(LayoutOptions.ALIGN_LEFT))
        assertEquals(Gravity.END, getAlternateTextGravity(LayoutOptions.ALIGN_RIGHT))
    }

    @Test
    fun testGetAdjustedDeviceDensity() {
        assertEquals(1f, getAdjustedDeviceDensity(1f), 0.001f)
        assertEquals(1.5f, getAdjustedDeviceDensity(1.49f), 0.001f)
        assertEquals(2f, getAdjustedDeviceDensity(1.51f), 0.001f)
        assertEquals(3f, getAdjustedDeviceDensity(2.9f), 0.001f)
        assertEquals(4f, getAdjustedDeviceDensity(3.1f), 0.001f)
    }

    @Test
    fun testGetAdjustedModalHeight() {
        assertEquals(10, getAdjustedModalHeight(100, 100, 5))
        assertEquals(55, getAdjustedModalHeight(100, 100, 50))
        assertEquals(100, getAdjustedModalHeight(100, 100, 100))
    }

    @Test
    fun testGetPaddingForTheImagePositioning() {
        assertEquals(10, getPaddingForTheImagePositioning(10f, LayoutOptions.CENTER))
        assertEquals(0, getPaddingForTheImagePositioning(10f, LayoutOptions.FULL_WIDTH))
    }

    @Test
    fun testGetLayoutParamsForTheImagePositioning() {
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val fullWidthLayoutParams = getLayoutParamsForTheImagePositioning(layoutParams, 100, LayoutOptions.FULL_WIDTH)
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, fullWidthLayoutParams.width)
        assertEquals(100, fullWidthLayoutParams.height)

        // reset layoutParams
        layoutParams.apply {
            width = LinearLayout.LayoutParams.WRAP_CONTENT
            height = LinearLayout.LayoutParams.WRAP_CONTENT
        }

        val otherLayoutParams = getLayoutParamsForTheImagePositioning(layoutParams, 100, LayoutOptions.CENTER)
        assertEquals(LinearLayout.LayoutParams.WRAP_CONTENT, otherLayoutParams.width)
        assertEquals(LinearLayout.LayoutParams.WRAP_CONTENT, otherLayoutParams.height)
    }

    @Test
    fun testGetImageScaleTypeFromConfig() {
        assertEquals(ImageView.ScaleType.FIT_XY, getImageScaleTypeFromConfig(LayoutOptions.FULL_WIDTH))
        assertEquals(ImageView.ScaleType.CENTER_INSIDE, getImageScaleTypeFromConfig(LayoutOptions.CENTER))
        assertEquals(ImageView.ScaleType.FIT_START, getImageScaleTypeFromConfig(LayoutOptions.ALIGN_LEFT))
        assertEquals(ImageView.ScaleType.FIT_END, getImageScaleTypeFromConfig(LayoutOptions.ALIGN_RIGHT))
    }
}
