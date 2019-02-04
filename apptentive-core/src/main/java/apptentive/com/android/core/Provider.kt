package apptentive.com.android.core

interface Providable

object Provider {
    fun <T : Providable> register(providable: T) {
        TODO("register")
    }

    fun clear() {
        TODO("clear")
    }

    fun <T : Providable> of(): T? {
        TODO("of")
    }
}
