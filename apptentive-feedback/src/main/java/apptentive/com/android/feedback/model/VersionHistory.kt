package apptentive.com.android.feedback.model

import androidx.annotation.VisibleForTesting
import apptentive.com.android.core.DefaultTimeSource
import apptentive.com.android.core.TimeSource
import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName
import apptentive.com.android.util.copyAndAdd

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
data class VersionHistory(
    internal val items: List<VersionHistoryItem> = emptyList(),
    private val timeSource: TimeSource = DefaultTimeSource
) {
    fun updateVersionHistory(
        timestamp: Double,
        versionCode: VersionCode,
        versionName: VersionName
    ): VersionHistory {
        // if an item exists - return the same instance
        val versionHistoryItem = VersionHistoryItem(timestamp, versionCode, versionName)
        val index = items.indexOf(versionHistoryItem)
        return if (index != -1) this
        else copy(items = items.copyAndAdd(versionHistoryItem))
    }

    fun getTimeAtInstallTotal(): DateTime {
        // Simply return the first item's timestamp, if there is one.
        val timestamp = items.firstOrNull()?.timestamp ?: timeSource.getTimeSeconds()
        return DateTime(timestamp)
    }

    /**
     * Returns the timestamp at the first install of the current versionCode of this app that Apptentive was aware of.
     */
    fun getTimeAtInstallForVersionCode(versionCode: VersionCode): DateTime {
        for (item in items) {
            if (item.versionCode == versionCode) {
                return DateTime(item.timestamp)
            }
        }
        return DateTime(timeSource.getTimeSeconds())
    }

    /**
     * Returns the timestamp at the first install of the current versionName of this app that Apptentive was aware of.
     */
    fun getTimeAtInstallForVersionName(versionName: String): DateTime {
        for (item in items) {
            if (item.versionName == versionName) {
                return DateTime(item.timestamp)
            }
        }
        return DateTime(timeSource.getTimeSeconds())
    }

    /**
     * Returns true if the current versionCode is not the first version or build that we have seen. Basically, it just
     * looks for two or more versionCodes.
     *
     * @return True if this is not the first versionCode of the app we've seen.
     */
    fun isUpdateForVersionCode(): Boolean {
        val uniques = items.map { it.versionCode }.toSet()
        return uniques.size > 1
    }

    /**
     * Returns true if the current versionName is not the first version or build that we have seen. Basically, it just
     * looks for two or more versionNames.
     *
     * @return True if this is not the first versionName of the app we've seen.
     */
    fun isUpdateForVersionName(): Boolean {
        val uniques = items.map { it.versionName }.toSet()
        return uniques.size > 1
    }

    /** Return last seen [VersionHistoryItem] if any */
    fun getLastVersionSeen(): VersionHistoryItem? {
        return items.lastOrNull()
    }
}
