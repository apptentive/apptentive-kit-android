package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName

data class EngagementRecord(
    val totalInvokes: Long,
    private val versionCodeLookup: Map<VersionCode, Long> = mapOf(),
    private val versionNameLookup: Map<VersionName, Long> = mapOf(),
    val lastInvoked: DateTime
) {
    fun invokesForVersionCode(versionCode: VersionCode): Long? {
        return versionCodeLookup[versionCode]
    }

    fun invokesForVersionName(versionName: VersionName): Long? {
        return versionNameLookup[versionName]
    }
}
