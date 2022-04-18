package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.model.EngagementRecord
import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
data class InteractionResponseData(
    val responses: Set<InteractionResponse> = setOf(),
    val record: EngagementRecord = EngagementRecord()
) {
    fun totalInvokes(): Long = record.getTotalInvokes()
    fun invokesForVersionCode(versionCode: VersionCode): Long =
        record.invokesForVersionCode(versionCode) ?: 0

    fun invokesForVersionName(versionName: VersionName): Long =
        record.invokesForVersionName(versionName) ?: 0

    fun lastInvoke(): DateTime = record.getLastInvoked()

    fun addInvoke(
        versionName: VersionName,
        versionCode: VersionCode,
        lastInvoked: DateTime
    ): EngagementRecord {
        return record.addInvoke(versionCode, versionName, lastInvoked)
    }
}
