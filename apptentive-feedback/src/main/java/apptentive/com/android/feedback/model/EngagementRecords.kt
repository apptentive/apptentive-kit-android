package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName

data class EngagementRecords<Key : Any>(private val records: MutableMap<Key, EngagementRecord> = mutableMapOf()) {
    fun totalInvokes(key: Key): Long? {
        val record = getRecord(key)
        return record?.totalInvokes
    }

    fun invokesForVersionCode(key: Key, versionCode: VersionCode): Long? {
        val record = getRecord(key)
        return record?.invokesForVersionCode(versionCode)
    }

    fun invokesForVersionName(key: Key, versionName: VersionName): Long? {
        val record = getRecord(key)
        return record?.invokesForVersionName(versionName)
    }

    fun lastInvoke(key: Key): DateTime? {
        val record = getRecord(key)
        return record?.lastInvoked
    }

    private fun getRecord(key: Key) = records[key]
}
