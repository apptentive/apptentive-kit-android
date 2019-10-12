package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.engagement.Event

/* Note: this class was generated via script */
@Suppress("ClassName")
sealed class Field {
    object application {
        object version_code : Field()
        object version_name : Field()
    }

    object sdk {
        object version : Field()
    }

    object current_time : Field()
    object is_update {
        object version_code : Field()
        object version_name : Field()
    }

    object time_at_install {
        object total : Field()
        object version_code : Field()
        object version_name : Field()
    }

    object code_point {
        object invokes {
            data class total(val event: Event) : Field()
            data class version_code(val event: Event) : Field()
            data class version_name(val event: Event) : Field()
        }

        object last_invoked_at {
            data class total(val event: Event) : Field()
        }
    }

    object interactions {
        object invokes {
            data class total(val interactionId: String) : Field()
            data class version_code(val interactionId: String) : Field()
            data class version_name(val interactionId: String) : Field()
        }

        object last_invoked_at {
            data class total(val interactionId: String) : Field()
        }
    }

    object person {
        object name : Field()
        object email : Field()
        data class custom_data(val key: String) : Field()
    }

    object device {
        object os_name : Field()
        object os_version : Field()
        object os_build : Field()
        object manufacturer : Field()
        object model : Field()
        object board : Field()
        object product : Field()
        object brand : Field()
        object cpu : Field()
        object hardware : Field()
        object device : Field()
        object uuid : Field()
        object carrier : Field()
        object current_carrier : Field()
        object network_type : Field()
        object build_type : Field()
        object build_id : Field()
        object bootloader_version : Field()
        object radio_version : Field()
        object locale_country_code : Field()
        object locale_language_code : Field()
        object locale_raw : Field()
        object os_api_level : Field()
        object utc_offset : Field()
        data class custom_data(val key: String) : Field()
    }

    data class unknown(val path: String) : Field()

    companion object {
        @Suppress("LocalVariableName")
        fun parse(path: String): Field {
            val components = path.split("/")
            when (components[0]) {
                "application" -> when (components[1]) {
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
            }
            return unknown(path)
        }
    }
}