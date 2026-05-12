package apptentive.com.android.feedback.platform

import apptentive.com.android.core.util.Factory
import apptentive.com.android.feedback.model.Person

internal class DefaultPersonFactory : Factory<Person> {
    override fun create(): Person = Person()
}
