package apptentive.com.android.feedback.model

import apptentive.com.android.core.DefaultTimeSource
import apptentive.com.android.core.TimeSource
import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName
import apptentive.com.android.util.copyAndAdd

data class VersionHistory(
    internal val items: List<VersionHistoryItem> = emptyList(),
    private val timeSource: TimeSource = DefaultTimeSource
) {
    fun updateVersionHistory(
        timestamp: Double,
        versionCode: VersionCode,
        versionName: VersionName
    ): VersionHistory {
        // if an item exists - return same instance
        if (items.any { it.versionCode == versionCode && it.versionName == versionName }) {
            return this
        }

        val item = VersionHistoryItem(
            timestamp = timestamp,
            versionCode = versionCode,
            versionName = versionName
        )
        return copy(
            items = items.copyAndAdd(item)
        )
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
