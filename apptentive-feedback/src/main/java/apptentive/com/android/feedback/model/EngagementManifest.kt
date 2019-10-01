package apptentive.com.android.feedback.model

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.feedback.model.interactions.Interaction
import apptentive.com.android.serialization.Decoder
import apptentive.com.android.serialization.Encoder
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.serialization.json.JsonException

data class EngagementManifest(
    val interactions: List<Interaction> = listOf(),
    val targets: Map<String, List<Target>> = mapOf(),
    val expiry: TimeInterval = 0.0
) {
    companion object {
        @Throws(JsonException::class)
        fun fromJson(json: String): EngagementManifest {
            val jsonMap = JsonConverter.toMap(json)
            return fromJson(jsonMap)
        }

        fun fromJson(json: Map<String, *>): EngagementManifest {
            return EngagementManifest(
                interactions = (json["interactions"] as List<*>).map { Interaction.fromJson(it as Map<String, *>) },
                targets = (json["targets"] as Map<String, *>).mapValues { (key, targets) ->
                    (targets as List<*>).map { Target.fromJson(it as Map<String, *>) }
                }
            )
        }
    }
}

internal fun Encoder.encodeEngagementManifest(obj: EngagementManifest) {
    TODO()
}

internal fun Decoder.decodeEngagementManifest(): EngagementManifest {
    TODO()
}