package apptentive.com.android.feedback.model

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
    val birthday: String? = null, // FIXME: make it Date
    val mParticleId: String? = null,
    val customData: CustomData = CustomData()
)
