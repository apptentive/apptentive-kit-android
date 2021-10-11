package com.apptentive.example

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apptentive.apptentive_example.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loveDialogButton = findViewById<Button>(R.id.loveDialogButton)
        val noteButton = findViewById<Button>(R.id.noteButton)
        val surveyButton = findViewById<Button>(R.id.surveyButton)

        /**
         * For each interaction you'd like to test out...
         *
         * TODO Step 5: Uncomment the code below for the interactions you'd like to demo and import
         *  There are buttons set up for Love Dialog, Notes, and Survey
         *
         * TODO Step 6: Uncomment the handleResult function and import
         *
         * TODO Step 7: Create Interactions on your Apptentive Dashboard.
         *  Use the events below in your WHERE targeting (e.g. "love_dialog") or create
         *  your own event and change the corresponding included event below.
         *  https://learn.apptentive.com/knowledge-base/how-to-use-targeting/#where-targeting
         */

//        loveDialogButton.setOnClickListener { engage("love_dialog") { handleResult(it } }
//        noteButton.setOnClickListener { engage("note") { handleResult(it } }
//        surveyButton.setOnClickListener { engage("survey") { handleResult(it } }
    }

    /**
     * Every Apptentive engage call will have a callback which will have some helpful
     * info which tells if an interaction succeeded or not and why
     *
     * Success & Failure are typical results. They should be seen with every engage.
     * Success   - All criteria was met (set on backend) and the interaction shows
     * Failure   - Not all criteria was met and the interaction did not show
     *
     * Error & Exception are unusual results. They should be investigated.
     * Error     - There is some discrepancy between what was expected and what was attempted
     * Exception - There was a breaking error
     *
     * @see apptentive.com.android.feedback.EngagementResult
     */
//    fun handleResult(result: EngagementResult) {
//        if (result !is EngagementResult.Success) {
//            Toast.makeText(this, "Not engaged: $result", Toast.LENGTH_LONG).show()
//        }
//    }
}