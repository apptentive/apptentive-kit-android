package apptentive.com.android.feedback.model

import apptentive.com.android.serialization.Decoder
import apptentive.com.android.serialization.Encoder
import apptentive.com.android.serialization.decodeNullableString
import apptentive.com.android.serialization.encodeNullableString

data class Person(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val facebookId: String? = null,
    val phoneNumber: String? = null,
    val street: String? = null,
    val city: String? = null,
    val zip: String? = null,
    val country: String? = null,
    val birthday: String? = null,
    val mParticleId: String? = null,
    val customData: CustomData = CustomData()
)

internal fun Encoder.encode(obj: Person) {
    encodeNullableString(obj.id)
    encodeNullableString(obj.email)
    encodeNullableString(obj.name)
    encodeNullableString(obj.facebookId)
    encodeNullableString(obj.phoneNumber)
    encodeNullableString(obj.street)
    encodeNullableString(obj.city)
    encodeNullableString(obj.zip)
    encodeNullableString(obj.country)
    encodeNullableString(obj.birthday)
    encodeNullableString(obj.mParticleId)
    encode(obj.customData)
}

internal fun Decoder.decodePerson(): Person {
    return Person(
        id = decodeNullableString(),
        email = decodeNullableString(),
        name = decodeNullableString(),
        facebookId = decodeNullableString(),
        phoneNumber = decodeNullableString(),
        street = decodeNullableString(),
        city = decodeNullableString(),
        zip = decodeNullableString(),
        country = decodeNullableString(),
        birthday = decodeNullableString(),
        mParticleId = decodeNullableString(),
        customData = decodeCustomData()
    )
}
