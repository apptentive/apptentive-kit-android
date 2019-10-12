package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.engagement.Event

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

    object unknown : Field()
}