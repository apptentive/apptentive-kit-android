package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.engagement.criteria.DateTime
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionHistoryTest {
    @Test
    fun testLastVersionSeen() {
        var versionHistory = VersionHistory()
        versionHistory = versionHistory.updateVersionHistory(10.0, 100, "1.0.0")
        versionHistory = versionHistory.updateVersionHistory(20.0, 200, "2.0.0")
        val actual = versionHistory.getLastVersionSeen()
        val expected = VersionHistoryItem(
            timestamp = 20.0,
            versionCode = 200,
            versionName = "2.0.0"
        )
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testTimeAtInstall() {
        var versionHistory = VersionHistory()
        versionHistory = versionHistory.updateVersionHistory(10.0, 100, "1.0.0")
        versionHistory = versionHistory.updateVersionHistory(20.0, 200, "2.0.0")
        versionHistory = versionHistory.updateVersionHistory(30.0, 300, "3.0.0")
        versionHistory = versionHistory.updateVersionHistory(40.0, 100, "1.0.0")

        assertThat(versionHistory.getTimeAtInstallTotal()).isEqualTo(DateTime(10.0))

        assertThat(versionHistory.getTimeAtInstallForVersionCode(100)).isEqualTo(DateTime(10.0))
        assertThat(versionHistory.getTimeAtInstallForVersionName("1.0.0")).isEqualTo(DateTime(10.0))

        assertThat(versionHistory.getTimeAtInstallForVersionCode(200)).isEqualTo(DateTime(20.0))
        assertThat(versionHistory.getTimeAtInstallForVersionName("2.0.0")).isEqualTo(DateTime(20.0))

        assertThat(versionHistory.getTimeAtInstallForVersionCode(300)).isEqualTo(DateTime(30.0))
        assertThat(versionHistory.getTimeAtInstallForVersionName("3.0.0")).isEqualTo(DateTime(30.0))
    }

    @Test
    fun testIsUpdateForVersionCode() {
        var versionHistory = VersionHistory()
        assertFalse(versionHistory.isUpdateForVersionCode())
        assertFalse(versionHistory.isUpdateForVersionName())

        versionHistory = versionHistory.updateVersionHistory(10.0, 100, "1.0.0")
        assertFalse(versionHistory.isUpdateForVersionCode())
        assertFalse(versionHistory.isUpdateForVersionName())

        versionHistory = versionHistory.updateVersionHistory(20.0, 100, "1.0.1")
        assertFalse(versionHistory.isUpdateForVersionCode())
        assertTrue(versionHistory.isUpdateForVersionName())

        versionHistory = versionHistory.updateVersionHistory(30.0, 200, "1.0.1")
        assertTrue(versionHistory.isUpdateForVersionCode())
        assertTrue(versionHistory.isUpdateForVersionName())
    }
}