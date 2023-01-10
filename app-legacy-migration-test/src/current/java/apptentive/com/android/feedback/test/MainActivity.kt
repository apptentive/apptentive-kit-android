package apptentive.com.android.feedback.test

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.Apptentive.engage
import apptentive.com.android.feedback.Apptentive.showMessageCenter
import apptentive.com.android.feedback.ApptentiveActivityInfo

class MainActivity : AppCompatActivity(), ApptentiveActivityInfo {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.login).setOnClickListener { notImplemented("Login not supported yet") }
        findViewById<View>(R.id.engage).setOnClickListener {
            engage("love_dialog_test")
        }
        findViewById<View>(R.id.message_center).setOnClickListener {
            showMessageCenter()
        }
    }

    override fun onResume() {
        super.onResume()
        Apptentive.registerApptentiveActivityInfoCallback(this)
    }

    private fun notImplemented(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
    }

    override fun getApptentiveActivityInfo(): Activity {
        return this
    }
}