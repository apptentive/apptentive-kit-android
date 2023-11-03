package apptentive.com.android.feedback.payload

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
data class MediaType(
    val type: String,
    val subType: String,
    val parameters: Map<String, String>? = null
) {
    override fun toString(): String {
        val parts = mutableListOf("$type/$subType")

        parameters?.let {
            for (parameter in it) {
                parts.add("${parameter.key}=${parameter.value}")
            }
        }

        return parts.mapNotNull { it }.joinToString(";")
    }

    companion object {
        fun parse(value: String): MediaType {
            val parts = value.split(";")
            val typeAndSubtype = parts.first().split("/")
            if (typeAndSubtype.size != 2) {
                throw IllegalArgumentException("Invalid value for media type: $value")
            }

            var parameters: Map<String, String>? = null
            if (parts.size > 1) {
                parameters = mutableMapOf()
                parts.subList(1, parts.size).forEach {
                    val keyAndValue = it.split("=")

                    if (keyAndValue.size != 2) {
                        throw IllegalArgumentException("Invalid parameter for media type: $it")
                    }

                    parameters[keyAndValue[0]] = keyAndValue[1]
                }
            }

            return MediaType(
                type = typeAndSubtype[0],
                subType = typeAndSubtype[1],
                parameters = parameters
            )
        }

        val applicationJson = MediaType("application", "json", mapOf(Pair("charset", "UTF-8")))
        val applicationOctetStream = MediaType("application", "octet-stream")
        fun multipartMixed(boundary: String) = MediaType("multipart", "mixed", mapOf(Pair("boundary", boundary)))
        fun multipartEncrypted(boundary: String) = MediaType("multipart", "encrypted", mapOf(Pair("boundary", boundary)))
    }
}
