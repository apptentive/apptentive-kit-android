package apptentive.com.android.feedback.test

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import apptentive.com.android.feedback.Apptentive.engage

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.login).setOnClickListener { notImplemented("Login not supported yet") }
        findViewById<View>(R.id.engage).setOnClickListener {
            engage(
                this@MainActivity,
                "love_dialog_test"
            )
        }
        findViewById<View>(R.id.message_center).setOnClickListener { notImplemented("Message center supported yet") }
    }

    private fun notImplemented(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
    }
}