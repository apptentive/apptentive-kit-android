package com.apptentive.example

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.apptentive.apptentive_example.R

/**
 * TODO Step 6: Extend the ApptentiveActivityInfo interface.
 *  This (and registering the callback) is so the Apptentive SDK has a callback
 *  to the Activity and can display Apptentive Interactions on the device screen.
 *
 *  @see ApptentiveActivityInfo
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * TODO Step 8: Uncomment the line below.
         *  This registers the Activity callback with the Apptentive SDK so it can
         *  display Apptentive Interactions.
         */
//        Apptentive.registerApptentiveActivityInfoCallback(this)

        val loveDialogButton = findViewById<Button>(R.id.loveDialogButton)
        val noteButton = findViewById<Button>(R.id.noteButton)
        val surveyButton = findViewById<Button>(R.id.surveyButton)
        val ratingButton = findViewById<Button>(R.id.ratingButton)

        /**
         * TODO Step 9: Uncomment the on click listeners below and import
         *  There are buttons set up for the Love Dialog, Notes, Survey, and Rating Interactions
         *
         *  NOTE ON THE RATING INTERACTION:
         *      Rating Interaction defaults to Google In-App Review. Google In-App Review is not
         *      demo-able unless the app is in a Internal Test Track or in Production on the Play Store.
         *      To demo the Apptentive Rating Dialog, set the customAppStoreURL in your ApptentiveConfiguration
         *
         * TODO Step 11: Create Interactions on your Apptentive Dashboard.
         *  Use the events below in your WHERE targeting (e.g. "love_dialog") or create
         *  your own event and change the corresponding event provided below.
         *  https://learn.apptentive.com/knowledge-base/how-to-use-targeting/#where-targeting
         */
//        loveDialogButton.setOnClickListener { Apptentive.engage("love_dialog") { handleResult(it) } }
//        noteButton.setOnClickListener { Apptentive.engage("note") { handleResult(it) } }
//        surveyButton.setOnClickListener { Apptentive.engage("survey") { handleResult(it) } }
//        ratingButton.setOnClickListener { Apptentive.engage("rating") { handleResult(it) } }
    }

    // TODO Step 7: Uncomment the overridden function for the ApptentiveActivityInfo interface.
/*
    override fun getApptentiveActivityInfo(): Activity {
        return this
    }
*/

    /**
     * TODO Step 10: Uncomment the helper function below and import
     *
     * Every Apptentive engage call will have an optional callback which will have some
     * helpful info which tells if an interaction succeeded or not and why.
     *
     * InteractionShown & InteractionNotShown are typical results. They should be seen with every engage.
     * InteractionShown    - All criteria was met (set on the dashboard) and the interaction shows
     * InteractionNotShown - Not all criteria was met and the interaction did not show
     *
     * Error & Exception are not typical results. They should be investigated.
     * Error     - There is some discrepancy between what was expected and what was attempted
     * Exception - There was a breaking error
     *
     * @see apptentive.com.android.feedback.EngagementResult
     */
/*
    private fun handleResult(result: EngagementResult) {
        when(result) {
            is EngagementResult.InteractionShown -> {
                Toast.makeText(this, "Interaction Shown", Toast.LENGTH_LONG).show()
            }
            is EngagementResult.InteractionNotShown -> {
                Toast.makeText(this, "Interaction Not Shown: ${result.description}", Toast.LENGTH_LONG).show()
            }
            is EngagementResult.Error -> {
                Toast.makeText(this, "Engage Error: ${result.message}", Toast.LENGTH_LONG).show()
            }
            is EngagementResult.Exception -> {
                Toast.makeText(this, "Engage Exception: ${result.error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
*/
}