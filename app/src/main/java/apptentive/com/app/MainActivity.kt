package apptentive.com.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import apptentive.com.android.feedback.Apptentive
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        engage_button.setOnClickListener {
            val eventName = event_name_edit_text.text.toString().trim()
            if (eventName.isEmpty()) {
                Toast.makeText(this, "Empty event name", Toast.LENGTH_LONG).show()
            } else {
                Apptentive.engage(this, eventName)
            }
        }
    }
}
