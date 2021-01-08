package apptentive.com.app.test

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.rules.ActivityScenarioRule
import apptentive.com.app.test.rules.DisableAnimationsRule
import org.junit.Rule

abstract class AbstractActivityTest {
    @get:Rule
    var disableAnimations = DisableAnimationsRule()

    @get:Rule
    var activityRule: ActivityScenarioRule<out Activity>

    constructor(activityClass: Class<out Activity>) {
        activityRule = ActivityScenarioRule(activityClass)
    }

    constructor(intent: Intent) {
        activityRule = ActivityScenarioRule(intent)
    }
}