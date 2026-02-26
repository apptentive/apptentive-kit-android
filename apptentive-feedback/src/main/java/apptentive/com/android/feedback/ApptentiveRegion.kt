package apptentive.com.android.feedback

import apptentive.com.android.util.InternalUseOnly

sealed class ApptentiveRegion(val value: String) {
    object US : ApptentiveRegion("us")
    object EU : ApptentiveRegion("eu")
    object CN : ApptentiveRegion("cn")
    object AU : ApptentiveRegion("au")
    object UNKNOWN : ApptentiveRegion("unknown")
    @InternalUseOnly
    object STAGING0 : ApptentiveRegion("stage0")
    @InternalUseOnly
    object STAGING1 : ApptentiveRegion("stage1")
    @InternalUseOnly
    object STAGING2 : ApptentiveRegion("stage2")
    @InternalUseOnly
    // Used for setting staging URL
    class Custom(value: String) : ApptentiveRegion(value)
}
