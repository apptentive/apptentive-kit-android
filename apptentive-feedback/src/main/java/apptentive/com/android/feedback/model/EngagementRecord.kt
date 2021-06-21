package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName

data class EngagementRecord(
    private var totalInvokes: Long = 0,
    private val versionCodeLookup: MutableMap<VersionCode, Long> = mutableMapOf(),
    private val versionNameLookup: MutableMap<VersionName, Long> = mutableMapOf(),
    private var lastInvoked: DateTime = DateTime(0.0)
) {
    val versionCodes: Map<VersionCode, Long> = versionCodeLookup
    val versionNames: Map<VersionName, Long> = versionNameLookup

    constructor(versionCode: VersionCode, versionName: VersionName, lastInvoked: DateTime) : this(
        totalInvokes = 1,
        versionCodeLookup = mutableMapOf(versionCode to 1L),
        versionNameLookup = mutableMapOf(versionName to 1L),
        lastInvoked = lastInvoked
    )

    fun getTotalInvokes() = totalInvokes

    fun getLastInvoked() = lastInvoked

    fun invokesForVersionCode(versionCode: VersionCode): Long? {
        return versionCodeLookup[versionCode]
    }

    fun invokesForVersionName(versionName: VersionName): Long? {
        return versionNameLookup[versionName]
    }

    fun addInvoke(versionCode: VersionCode, versionName: VersionName, lastInvoked: DateTime): EngagementRecord {
        this.lastInvoked = lastInvoked

        totalInvokes += 1
        versionCodeLookup.apply {
            this[versionCode] = 1 + (this[versionCode] ?: 0)
        }
        versionNameLookup.apply {
            this[versionName] = 1 + (this[versionName] ?: 0)
        }

        return this
    }
}
