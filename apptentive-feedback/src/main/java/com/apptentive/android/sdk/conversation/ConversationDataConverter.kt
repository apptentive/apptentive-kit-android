package com.apptentive.android.sdk.conversation

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.engagement.criteria.Version
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.CustomData
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.EngagementData
import apptentive.com.android.feedback.model.EngagementRecord
import apptentive.com.android.feedback.model.EngagementRecords
import apptentive.com.android.feedback.model.IntegrationConfig
import apptentive.com.android.feedback.model.IntegrationConfigItem
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.feedback.model.VersionHistory
import apptentive.com.android.feedback.model.VersionHistoryItem
import apptentive.com.android.feedback.utils.parseInt
import java.io.Serializable

internal typealias LegacyConversationData = ConversationData
internal typealias LegacyAppRelease = com.apptentive.android.sdk.storage.AppRelease
internal typealias LegacyCustomData = com.apptentive.android.sdk.storage.CustomData
internal typealias LegacyDevice = com.apptentive.android.sdk.storage.Device
internal typealias LegacyEventData = com.apptentive.android.sdk.storage.EventData
internal typealias LegacyEventRecord = com.apptentive.android.sdk.storage.EventRecord
internal typealias LegacyIntegrationConfig = com.apptentive.android.sdk.storage.IntegrationConfig
internal typealias LegacyPerson = com.apptentive.android.sdk.storage.Person
internal typealias LegacySdk = com.apptentive.android.sdk.storage.Sdk
internal typealias LegacyVersionHistory = com.apptentive.android.sdk.storage.VersionHistory
internal typealias LegacyIntegrationConfigItem = com.apptentive.android.sdk.storage.IntegrationConfigItem
internal typealias LegacyDateTime = com.apptentive.android.sdk.DateTime
internal typealias LegacyVersion = com.apptentive.android.sdk.Version
internal typealias LegacyVersionHistoryItem = com.apptentive.android.sdk.storage.VersionHistoryItem

/**
 * Converts legacy SDK conversation data into the current [Conversation] data format.
 * Used in legacy SDK data migration.
 */
internal fun LegacyConversationData.toConversation() = Conversation(
    localIdentifier = localIdentifier,
    conversationToken = conversationToken,
    conversationId = conversationId,
    device = device.toLatestFormat(),
    person = person.toLatestFormat(),
    sdk = sdk.toLatestFormat(),
    appRelease = appRelease.toLatestFormat(),
    engagementData = eventData.toEngagementData(versionHistory)
)

internal fun LegacyDevice.toLatestFormat() = Device(
    osName = osName,
    osVersion = osVersion,
    osBuild = osBuild,
    osApiLevel = osApiLevel,
    manufacturer = manufacturer,
    model = model,
    board = board,
    product = product,
    brand = brand,
    cpu = cpu,
    device = device,
    uuid = uuid,
    buildType = buildType,
    buildId = buildId,
    carrier = carrier,
    currentCarrier = currentCarrier,
    networkType = networkType,
    bootloaderVersion = bootloaderVersion,
    radioVersion = radioVersion,
    localeCountryCode = localeCountryCode,
    localeLanguageCode = localeLanguageCode,
    localeRaw = localeRaw,
    utcOffset = parseInt(utcOffset) ?: 0,
    customData = customData.toLatestFormat(),
    integrationConfig = integrationConfig.toLatestFormat()
)

internal fun LegacyPerson.toLatestFormat() = Person(
    id = id,
    email = email,
    name = name,
    mParticleId = mParticleId,
    customData = customData.toLatestFormat()
)

internal fun LegacySdk.toLatestFormat() = SDK(
    version = version,
    platform = platform,
    distribution = distribution,
    distributionVersion = distributionVersion,
    programmingLanguage = programmingLanguage,
    authorName = authorName,
    authorEmail = authorEmail
)

internal fun LegacyAppRelease.toLatestFormat() = AppRelease(
    type = type,
    identifier = identifier,
    versionCode = versionCode.toLong(),
    versionName = versionName,
    targetSdkVersion = targetSdkVersion,
    minSdkVersion = "0",
    debug = isDebug,
    inheritStyle = isInheritStyle,
    overrideStyle = isOverrideStyle,
    appStore = appStore
)

internal fun LegacyCustomData.toLatestFormat() = CustomData(
    content = mapValues(::transformCustomDataValues)
)

private fun transformCustomDataValues(it: Map.Entry<String, Serializable>): Any? =
    when (val value = it.value) {
        is LegacyDateTime -> value.toLatestFormat()
        is LegacyVersion -> value.toLatestFormat()
        else -> value
    }

private fun LegacyDateTime.toLatestFormat() = DateTime(seconds = dateTime)

private fun Double.toLatestDateTime(): DateTime = DateTime(seconds = this)

private fun LegacyVersion.toLatestFormat() = Version.parse(value = version)

internal fun LegacyIntegrationConfig.toLatestFormat(): IntegrationConfig = IntegrationConfig(
    apptentive = apptentive?.toLatestFormat(),
    amazonAwsSns = amazonAwsSns?.toLatestFormat(),
    urbanAirship = urbanAirship?.toLatestFormat(),
    parse = parse?.toLatestFormat()
)

private fun LegacyIntegrationConfigItem.toLatestFormat() = IntegrationConfigItem(
    contents = this.contents.mapValues { it.value }
)

internal fun LegacyEventData.toEngagementData(versionHistory: LegacyVersionHistory?) = EngagementData(
    events = events.toEngagementEventsRecords(),
    interactions = interactions.toEngagementInteractionsRecords(),
    versionHistory = versionHistory?.toLatestFormat()
        ?: VersionHistory()
)

internal fun Map<String, LegacyEventRecord>.toEngagementEventsRecords(): EngagementRecords<Event> =
    EngagementRecords(
        records = this.entries
            .associate { Event.parse(it.key) to it.value.toEngagementRecord() }
            .toMutableMap()
    )

private fun LegacyEventRecord.toEngagementRecord(): EngagementRecord =
    EngagementRecord(
        totalInvokes = total,
        versionCodeLookup = versionCodes.mapKeys { it.key.toLong() }.toMutableMap(),
        versionNameLookup = versionNames,
        lastInvoked = last.toLatestDateTime()
    )

internal fun Map<String, LegacyEventRecord>.toEngagementInteractionsRecords(): EngagementRecords<InteractionId> =
    EngagementRecords(
        records = this.entries
            .associate { it.key to it.value.toEngagementRecord() }
            .toMutableMap()
    )

internal fun LegacyVersionHistory.toLatestFormat(): VersionHistory = VersionHistory(
    items = versionHistoryItems.map {
        it.toLatestFormat()
    }
)

private fun LegacyVersionHistoryItem.toLatestFormat() = VersionHistoryItem(
    timestamp = timestamp,
    versionCode = versionCode.toLong(),
    versionName = versionName
)
