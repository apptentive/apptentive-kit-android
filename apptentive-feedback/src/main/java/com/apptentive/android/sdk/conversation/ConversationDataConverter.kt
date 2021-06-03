package com.apptentive.android.sdk.conversation

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.engagement.criteria.Version
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.model.*
import apptentive.com.android.util.parseInt
import java.io.Serializable

typealias LegacyConversationData = ConversationData
typealias LegacyAppRelease = com.apptentive.android.sdk.storage.AppRelease
typealias LegacyCustomData = com.apptentive.android.sdk.storage.CustomData
typealias LegacyDevice = com.apptentive.android.sdk.storage.Device
typealias LegacyEventData = com.apptentive.android.sdk.storage.EventData
typealias LegacyEventRecord = com.apptentive.android.sdk.storage.EventRecord
typealias LegacyIntegrationConfig = com.apptentive.android.sdk.storage.IntegrationConfig
typealias LegacyPerson = com.apptentive.android.sdk.storage.Person
typealias LegacySdk = com.apptentive.android.sdk.storage.Sdk
typealias LegacyVersionHistory = com.apptentive.android.sdk.storage.VersionHistory
typealias LegacyIntegrationConfigItem = com.apptentive.android.sdk.storage.IntegrationConfigItem
typealias LegacyDateTime = com.apptentive.android.sdk.DateTime
typealias LegacyVersion = com.apptentive.android.sdk.Version
typealias LegacyVersionHistoryItem = com.apptentive.android.sdk.storage.VersionHistoryItem


/**
 * Converts legacy SDK conversation data into the current [Conversation] data format.
 * Used in legacy SDK data migration.
 */
fun LegacyConversationData.toConversation() = Conversation(
    localIdentifier = localIdentifier,
    conversationToken = conversationToken,
    conversationId = conversationId,
    device = device.toLatestFormat(),
    person = person.toLatestFormat(),
    sdk = sdk.toLatestFormat(),
    appRelease = appRelease.toLatestFormat(),
    engagementData = eventData.toEngagementData(versionHistory)
)

fun LegacyDevice.toLatestFormat() = Device(
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
    advertiserId = advertiserId,
    customData = customData.toLatestFormat(),
    integrationConfig = integrationConfig.toLatestFormat()
)

fun LegacyPerson.toLatestFormat() = Person(
    id = id,
    email = email,
    name = name,
    facebookId = facebookId,
    phoneNumber = phoneNumber,
    street = street,
    city = city,
    zip = zip,
    country = country,
    birthday = birthday,
    mParticleId = mParticleId,
    customData = customData.toLatestFormat()
)

fun LegacySdk.toLatestFormat() = SDK(
    version = version,
    platform = platform,
    distribution = distribution,
    distributionVersion = distributionVersion,
    programmingLanguage = programmingLanguage,
    authorName = authorName,
    authorEmail = authorEmail
)

fun LegacyAppRelease.toLatestFormat() = AppRelease(
    type = type,
    identifier = identifier,
    versionCode = versionCode.toLong(),
    versionName = versionName,
    targetSdkVersion = targetSdkVersion,
    debug = isDebug,
    inheritStyle = isInheritStyle,
    overrideStyle = isOverrideStyle,
    appStore = appStore
)

fun LegacyCustomData.toLatestFormat() = CustomData(
    content = mapValues(::transformCustomDataValues)
)

private fun transformCustomDataValues(it: Map.Entry<String, Serializable>): Any =
    when (val value = it.value) {
        is LegacyDateTime -> value.toLatestFormat()
        is LegacyVersion -> value.toLatestFormat()
        else -> value
    }

private fun LegacyDateTime.toLatestFormat() = DateTime(seconds = dateTime)

private fun Double.toLatestDateTime(): DateTime = DateTime(seconds = this)

private fun LegacyVersion.toLatestFormat() = Version.parse(value = version)

fun LegacyIntegrationConfig.toLatestFormat(): IntegrationConfig = IntegrationConfig(
    apptentive = apptentive?.toLatestFormat(),
    amazonAwsSns = amazonAwsSns?.toLatestFormat(),
    urbanAirship = urbanAirship?.toLatestFormat(),
    parse = parse?.toLatestFormat()
)

private fun LegacyIntegrationConfigItem.toLatestFormat() = IntegrationConfigItem(
    contents = this.contents.mapValues { it.value }
)

fun LegacyEventData.toEngagementData(versionHistory: LegacyVersionHistory?) = EngagementData(
    events = events.toEngagementEventsRecords(),
    interactions = interactions.toEngagementInteractionsRecords(),
    versionHistory = versionHistory?.toLatestFormat()
        ?: VersionHistory()
)

fun Map<String, LegacyEventRecord>.toEngagementEventsRecords(): EngagementRecords<Event> =
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


fun Map<String, LegacyEventRecord>.toEngagementInteractionsRecords(): EngagementRecords<InteractionId> =
    EngagementRecords(
        records = this.entries
            .associate { it.key to it.value.toEngagementRecord() }
            .toMutableMap()
    )

fun LegacyVersionHistory.toLatestFormat(): VersionHistory = VersionHistory(
    items = versionHistoryItems.map {
        it.toLatestFormat()
    }
)

private fun LegacyVersionHistoryItem.toLatestFormat() = VersionHistoryItem(
    timestamp = timestamp,
    versionCode = versionCode.toLong(),
    versionName = versionName
)
