package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName

data class EngagementRecords<Key : Any>(val records: MutableMap<Key, EngagementRecord> = mutableMapOf()) {
    fun totalInvokes(key: Key): Long? {
        return records[key]?.getTotalInvokes()
    }

    fun invokesForVersionCode(key: Key, versionCode: VersionCode): Long? {
        return records[key]?.invokesForVersionCode(versionCode)
    }

    fun invokesForVersionName(key: Key, versionName: VersionName): Long? {
        return records[key]?.invokesForVersionName(versionName)
    }

    fun lastInvoke(key: Key): DateTime? {
        return records[key]?.getLastInvoked()
    }

    fun addInvoke(
        key: Key,
        versionName: VersionName,
        versionCode: VersionCode,
        lastInvoked: DateTime
    ) {
        val existingRecord = records[key]
        if (existingRecord != null) {
            existingRecord.addInvoke(versionCode, versionName, lastInvoked)
        } else {
            records[key] = EngagementRecord(versionCode, versionName, lastInvoked)
        }
    }
}
