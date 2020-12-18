package apptentive.com.android.core

open class BehaviorSubject<T>(value: T) : Observable<T>(value) {
    override var value: T
        get() = super.value
        public set(value) {
            super.value = value
        }
}