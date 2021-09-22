package apptentive.com.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import apptentive.com.android.feedback.Apptentive

class DataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)
        findViewById<Button>(R.id.add_custom_device_data_button).setOnClickListener { addCustomDeviceData() }
        findViewById<Button>(R.id.remove_custom_device_data_button).setOnClickListener { removeCustomDeviceData() }
        findViewById<Button>(R.id.add_custom_person_data_button).setOnClickListener { addCustomPersonData() }
        findViewById<Button>(R.id.remove_custom_person_data_button).setOnClickListener { removeCustomPersonData() }
        findViewById<Button>(R.id.set_person_email_button).setOnClickListener { setPersonEmail() }
        findViewById<Button>(R.id.set_person_name_button).setOnClickListener { setPersonName() }
    }

    private fun addCustomDeviceData() {
        val keyText = findViewById<EditText>(R.id.add_custom_device_data_key)
        val valueText = findViewById<EditText>(R.id.add_custom_device_data_value)
        val key = keyText.text.toString().trim()
        val value = valueText.text.toString().trim()
        keyText.text = null
        valueText.text = null
        Apptentive.addCustomDeviceData(key, value)
    }

    private fun removeCustomDeviceData() {
        val keyText = findViewById<EditText>(R.id.remove_custom_device_data_key)
        val key = keyText.text.toString().trim()
        keyText.text = null
        Apptentive.removeCustomDeviceData(key)
    }

    private fun addCustomPersonData() {
        val keyText = findViewById<EditText>(R.id.add_custom_person_data_key)
        val valueText = findViewById<EditText>(R.id.add_custom_person_data_value)
        val key = keyText.text.toString().trim()
        val value = valueText.text.toString().trim()
        keyText.text = null
        valueText.text = null
        Apptentive.addCustomPersonData(key, value)
    }

    private fun removeCustomPersonData() {
        val keyText = findViewById<EditText>(R.id.remove_custom_person_data_key)
        val key = keyText.text.toString().trim()
        keyText.text = null
        Apptentive.removeCustomPersonData(key)
    }

    private fun setPersonEmail() {
        val emailText = findViewById<EditText>(R.id.set_person_email)
        val email = emailText.text.toString().trim()
        emailText.text = null
        Apptentive.setPersonEmail(email)
    }

    private fun setPersonName() {
        val userNameText = findViewById<EditText>(R.id.set_person_name)
        val userName = userNameText.text.toString().trim()
        userNameText.text = null
        Apptentive.setPersonName(userName)
    }
}
