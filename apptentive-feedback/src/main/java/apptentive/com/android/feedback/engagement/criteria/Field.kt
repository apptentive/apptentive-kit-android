package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.utils.appVersionCode
import apptentive.com.android.feedback.utils.appVersionName
import apptentive.com.android.util.InternalUseOnly

@Suppress("ClassName")
@InternalUseOnly
sealed class Field(val type: Type, val description: String) {
    enum class Type {
        String,
        Boolean,
        Number,
        DateTime,
        Version,
        Any
    }

    object application {
        object build_type : Field(type = Type.Boolean, description = "application build type")
        object version_code : Field(type = Type.Number, description = "application versionCode")
        object version_name : Field(type = Type.Version, description = "application versionName")
    }

    object sdk {
        object version : Field(type = Type.Version, description = "SDK version")
    }

    object current_time : Field(type = Type.DateTime, description = "current time")

    object is_update {
        object version_code : Field(
            type = Type.Boolean,
            description = "app version code changed"
        )

        object version_name : Field(
            type = Type.Boolean,
            description = "app version name changed"
        )
    }

    object time_at_install {
        object total : Field(
            type = Type.DateTime,
            description = "time at install"
        )

        object version_code : Field(
            type = Type.DateTime,
            description = "time at install for version code '$appVersionCode'"
        )

        object version_name : Field(
            type = Type.DateTime,
            description = "time at install for version name '$appVersionName'"
        )
    }

    object code_point {
        object invokes {
            data class total(val event: Event) : Field(
                type = Type.Number,
                description = "total number of invokes for event $event"
            )

            data class version_code(val event: Event) : Field(
                type = Type.Number,
                description = "number of invokes for event '${event.name}' for version code '$appVersionCode'"
            )

            data class version_name(val event: Event) : Field(
                type = Type.Number,
                description = "number of invokes for event '${event.name}' for version name '$appVersionName'"
            )
        }

        object last_invoked_at {
            data class total(val event: Event) : Field(
                type = Type.DateTime,
                description = "last time event '${event.name}' was invoked"
            )
        }
    }

    object interactions {
        object invokes {
            data class total(val interactionId: String) : Field(
                type = Type.Number,
                description = "total number of invokes for interaction id $interactionId"
            )

            data class version_code(val interactionId: String) : Field(
                type = Type.Number,
                description = "number of invokes for interaction id '$interactionId' for version code '$appVersionCode'"
            )

            data class version_name(val interactionId: String) : Field(
                type = Type.Number,
                description = "number of invokes for interaction id '$interactionId' for version name '$appVersionName'"
            )
        }

        object last_invoked_at {
            data class total(val interactionId: InteractionId) : Field(
                type = Type.DateTime,
                description = "last time interaction id '$interactionId' was invoked"
            )
        }

        object answers {
            data class id(val responseId: InteractionId) : Field(
                type = Type.Any, // Can be String or Boolean
                description = "answer id responseId:$responseId"
            )
            data class value(val responseId: InteractionId) : Field(
                type = Type.Any, // Can be String, Boolean, or Long
                description = "answer value responseId:$responseId"
            )
        }

        object current_answer {
            data class id(val responseId: InteractionId) : Field(
                type = Type.Any, // Can be String or Boolean
                description = "current answer id responseId:$responseId"
            )
            data class value(val responseId: InteractionId) : Field(
                type = Type.Any, // Can be String, Boolean, or Long
                description = "current answer value responseId:$responseId"
            )
        }
    }

    object person {
        object name : Field(type = Type.String, description = "person name")

        object email : Field(type = Type.String, description = "person email")

        data class custom_data(val key: String) : Field(
            type = Type.Any,
            description = "person custom_data[$key]"
        )
    }

    object device {
        object os_name : Field(
            type = Type.String,
            description = "device OS"
        )

        object os_version : Field(
            type = Type.Version,
            description = "device OS version"
        )

        object os_build : Field(
            type = Type.String,
            description = "device OS build"
        )

        object manufacturer : Field(
            type = Type.String,
            description = "device manufacturer"
        )

        object model : Field(
            type = Type.String,
            description = "device model"
        )

        object board : Field(
            type = Type.String,
            description = "device board"
        )

        object product : Field(
            type = Type.String,
            description = "device product"
        )

        object brand : Field(
            type = Type.String,
            description = "device brand"
        )

        object cpu : Field(
            type = Type.String,
            description = "device CPU"
        )

        object hardware : Field(
            type = Type.String,
            description = "device hardware"
        )

        object device : Field(
            type = Type.String,
            description = "device"
        )

        object uuid : Field(
            type = Type.String,
            description = "device UUID"
        )

        object carrier : Field(
            type = Type.String,
            description = "device carrier"
        )

        object current_carrier : Field(
            type = Type.String,
            description = "device current carrier"
        )

        object network_type : Field(
            type = Type.String,
            description = "device network type"
        )

        object build_type : Field(
            type = Type.String,
            description = "device build type"
        )

        object build_id : Field(
            type = Type.String,
            description = "device build id"
        )

        object bootloader_version : Field(
            type = Type.Version,
            description = "device bootloader version"
        )

        object radio_version : Field(
            type = Type.Version,
            description = "device radio version"
        )

        object locale_country_code : Field(
            type = Type.String,
            description = "device country code"
        )

        object locale_language_code : Field(
            type = Type.String,
            description = "device language code"
        )

        object locale_raw : Field(
            type = Type.String,
            description = "device locale"
        )

        object os_api_level : Field(
            type = Type.String,
            description = "device OS API level"
        )

        object utc_offset : Field(
            type = Type.String,
            description = "device UTC offset"
        )

        data class custom_data(val key: String) : Field(
            type = Type.Any,
            description = "device custom_data[$key]"
        )
    }

    object random {
        object percent : Field(
            type = Type.Number,
            description = "random sample percentage"
        )
        data class percent_with_id(val randomPercentId: String) : Field(
            type = Type.Number,
            description = "random sample percentage for id: $randomPercentId"
        )
    }

    data class unknown(val path: String) : Field(Type.Any, "unknown path $path")

    companion object {
        @Suppress("LocalVariableName")
        fun parse(path: String): Field {
            val components = path.split("/")
            when (components[0]) {
                "application" -> when (components[1]) {
                    "debug", "release" -> return application.build_type
                    "version_code" -> return application.version_code
                    "version_name" -> return application.version_name
                }
                "sdk" -> when (components[1]) {
                    "version" -> return sdk.version
                }
                "current_time" -> return current_time
                "is_update" -> when (components[1]) {
                    "version_code" -> return is_update.version_code
                    "version_name" -> return is_update.version_name
                }
                "time_at_install" -> when (components[1]) {
                    "total" -> return time_at_install.total
                    "version_code" -> return time_at_install.version_code
                    "version_name" -> return time_at_install.version_name
                }
                "code_point" -> {
                    val code_point_name = Event.parse(components[1])
                    when (components[2]) {
                        "invokes" -> when (components[3]) {
                            "total" -> return code_point.invokes.total(code_point_name)
                            "version_code" -> return code_point.invokes.version_code(code_point_name)
                            "version_name" -> return code_point.invokes.version_name(code_point_name)
                        }
                        "last_invoked_at" -> when (components[3]) {
                            "total" -> return code_point.last_invoked_at.total(code_point_name)
                        }
                    }
                }
                "interactions" -> {
                    val interaction_instance_id = components[1]
                    when (components[2]) {
                        "invokes" -> when (components[3]) {
                            "total" -> return interactions.invokes.total(interaction_instance_id)
                            "version_code" -> return interactions.invokes.version_code(
                                interaction_instance_id
                            )
                            "version_name" -> return interactions.invokes.version_name(
                                interaction_instance_id
                            )
                        }
                        "last_invoked_at" -> when (components[3]) {
                            "total" -> return interactions.last_invoked_at.total(
                                interaction_instance_id
                            )
                        }
                        "answers" -> when (components[3]) {
                            "id" -> return interactions.answers.id(interaction_instance_id)
                            "value" -> return interactions.answers.value(interaction_instance_id)
                        }
                        "current_answer" -> when (components[3]) {
                            "id" -> return interactions.current_answer.id(interaction_instance_id)
                            "value" -> return interactions.current_answer.value(interaction_instance_id)
                        }
                    }
                }
                "person" -> when (components[1]) {
                    "name" -> return person.name
                    "email" -> return person.email
                    "custom_data" -> {
                        val key = components[2]
                        return person.custom_data(key)
                    }
                }
                "device" -> when (components[1]) {
                    "os_name" -> return device.os_name
                    "os_version" -> return device.os_version
                    "os_build" -> return device.os_build
                    "manufacturer" -> return device.manufacturer
                    "model" -> return device.model
                    "board" -> return device.board
                    "product" -> return device.product
                    "brand" -> return device.brand
                    "cpu" -> return device.cpu
                    "hardware" -> return device.hardware
                    "device" -> return device.device
                    "uuid" -> return device.uuid
                    "carrier" -> return device.carrier
                    "current_carrier" -> return device.current_carrier
                    "network_type" -> return device.network_type
                    "build_type" -> return device.build_type
                    "build_id" -> return device.build_id
                    "bootloader_version" -> return device.bootloader_version
                    "radio_version" -> return device.radio_version
                    "locale_country_code" -> return device.locale_country_code
                    "locale_language_code" -> return device.locale_language_code
                    "locale_raw" -> return device.locale_raw
                    "os_api_level" -> return device.os_api_level
                    "utc_offset" -> return device.utc_offset
                    "custom_data" -> {
                        val key = components[2]
                        return device.custom_data(key)
                    }
                }
                "random" -> {
                    if (components[1] == "percent") return random.percent
                    else {
                        val random_percent_id = components[1]
                        if (components[2] == "percent") return random.percent_with_id(random_percent_id)
                    }
                }
            }
            return unknown(path)
        }
    }
}

internal fun Field.convertValue(value: Any?): Any? {
    val converted = convertComplexValue(value)

    return when (type) {
        Field.Type.String -> converted as String
        Field.Type.Number -> converted as Number
        Field.Type.Boolean -> converted as Boolean
        Field.Type.DateTime -> {
            return when (converted) {
                is DateTime -> converted
                is Double -> DateTime(DateTime.now().seconds + converted)
                else -> throw IllegalArgumentException("Illegal value for DateTime: $converted (${converted?.javaClass?.simpleName})")
            }
        }
        Field.Type.Version -> {
            return when (converted) {
                is Version -> converted
                else -> Version.parse(converted.toString())
            }
        }
        Field.Type.Any -> converted as Any
    }
}

private fun convertComplexValue(value: Any?): Any? {
    return when (value) {
        is Map<*, *> -> {
            val type = value["_type"]
            if (type != null) {
                return when (type) {
                    "datetime" -> {
                        val seconds = value["sec"] as Double
                        DateTime(seconds)
                    }
                    "version" -> {
                        val version = value["version"].toString()
                        Version.parse(version)
                    }
                    else -> throw IllegalArgumentException("Unknown complex type: $type")
                }
            }
            throw IllegalArgumentException("Unexpected value: $value")
        }
        else -> value // return as-is
    }
}
