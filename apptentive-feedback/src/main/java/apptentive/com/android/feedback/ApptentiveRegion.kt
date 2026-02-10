package apptentive.com.android.feedback

import apptentive.com.android.util.InternalUseOnly

sealed class ApptentiveRegion(val value: String) {
    object US : ApptentiveRegion("us")
    object EU : ApptentiveRegion("eu")
    object CN : ApptentiveRegion("cn")
    object AU : ApptentiveRegion("au")
    object UNKNOWN : ApptentiveRegion("unknown")
    @InternalUseOnly
    // Used for setting staging URL
    class Custom(value: String) : ApptentiveRegion(value)
}
