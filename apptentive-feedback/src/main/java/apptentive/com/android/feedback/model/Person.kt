package apptentive.com.android.feedback.model

import org.rekotlin.StateType

data class Person(
    val localIdentifier: String,
    val identifier: String? = null,
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val facebookId: String? = null,
    val phoneNumber: String? = null,
    val street: String? = null,
    val city: String? = null,
    val zip: String? = null,
    val country: String? = null,
    val birthday: String? = null

) : StateType {
    // private val customData: CustomData? = null
}