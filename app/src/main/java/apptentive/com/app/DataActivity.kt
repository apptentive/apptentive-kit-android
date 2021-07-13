package apptentive.com.app

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import apptentive.com.android.feedback.Apptentive

class DataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)
    }

    fun addCustomDeviceData(view: View?) {
        val keyText = findViewById<EditText>(R.id.add_custom_device_data_key)
        val valueText = findViewById<EditText>(R.id.add_custom_device_data_value)
        val key = keyText.text.toString().trim()
        val value = valueText.text.toString().trim()
        keyText.text = null
        valueText.text = null
        Apptentive.addCustomDeviceData(key, value)
    }

    fun removeCustomDeviceData(view: View?) {
        val keyText = findViewById<EditText>(R.id.remove_custom_device_data_key)
        val key = keyText.text.toString().trim()
        keyText.text = null
        Apptentive.removeCustomDeviceData(key)
    }

    fun addCustomPersonData(view: View?) {
        val keyText = findViewById<EditText>(R.id.add_custom_person_data_key)
        val valueText = findViewById<EditText>(R.id.add_custom_person_data_value)
        val key = keyText.text.toString().trim()
        val value = valueText.text.toString().trim()
        keyText.text = null
        valueText.text = null
        Apptentive.addCustomPersonData(key, value)
    }

    fun removeCustomPersonData(view: View?) {
        val keyText = findViewById<EditText>(R.id.remove_custom_person_data_key)
        val key = keyText.text.toString().trim()
        keyText.text = null
        Apptentive.removeCustomPersonData(key)
    }

    fun setPersonEmail(view: View?) {
        val emailText = findViewById<EditText>(R.id.set_person_email)
        val email = emailText.text.toString().trim()
        emailText.text = null
        Apptentive.setPersonEmail(email)
    }

    fun setPersonName(view: View?) {
        val userNameText = findViewById<EditText>(R.id.set_person_name)
        val userName = userNameText.text.toString().trim()
        userNameText.text = null
        Apptentive.setPersonName(userName)
    }
}
