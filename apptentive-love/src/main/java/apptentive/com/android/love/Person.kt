package apptentive.com.android.love

import java.util.*

class Person {
    val identifier: String = UUID.randomUUID().toString()
    var name: String? = null
    var email: String? = null
}
