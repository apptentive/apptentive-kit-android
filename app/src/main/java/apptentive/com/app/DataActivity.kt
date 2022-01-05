package apptentive.com.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import apptentive.com.android.feedback.Apptentive
import apptentive.com.app.databinding.ActivityDataBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class DataActivity : AppCompatActivity() {
    lateinit var binding: ActivityDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val isNightMode = prefs.getBoolean(EXTRA_NIGHT_MODE, false)
        delegate.localNightMode = if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        binding = ActivityDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        setupDataTypeDropdown()
        setupCustomStringData()
        setupCustomNumberData()
        setupCustomBooleanData()
        setupRemoveData()
        setupPersonName()
        setupPersonEmail()
    }

    private fun setupDataTypeDropdown() {
        val dataTypeValues = listOf(DataTypes.PERSON, DataTypes.DEVICE)
        val dataTypesAdapter = ArrayAdapter(this, R.layout.list_item, dataTypeValues)

        binding.apply {
            personDataLayout.isVisible = dataTypeDropdown.text.toString() == DataTypes.PERSON.name

            dataTypeDropdown.setAdapter(dataTypesAdapter)
            dataTypeDropdown.addTextChangedListener {
                personDataLayout.isVisible = it.toString() == DataTypes.PERSON.name

                if (it.toString().isNotEmpty()) dataTypeLayout.isErrorEnabled = false
            }
        }
    }

    private fun setupCustomStringData() {
        binding.apply {
            addCustomStringKeyEditText.addTextChangedListener {
                if (it?.isNotEmpty() == true) addCustomStringKeyTextLayout.isErrorEnabled = false
            }

            addCustomStringButton.setOnClickListener {
                val dataType = dataTypeDropdown.text?.toString()
                val customStringKey = addCustomStringKeyEditText.text?.toString()?.trim()
                val customStringValue = addCustomStringValueEditText.text?.toString().orEmpty().trim()

                val hasError = checkHasError(
                    dataTypeLayout,
                    dataType,
                    addCustomStringKeyTextLayout,
                    customStringKey
                )

                if (!hasError) {
                    when (dataType) {
                        DataTypes.PERSON.name -> Apptentive.addCustomPersonData(
                            customStringKey,
                            customStringValue
                        )
                        DataTypes.DEVICE.name -> Apptentive.addCustomDeviceData(
                            customStringKey,
                            customStringValue
                        )
                    }
                    makeToast("Key: $customStringKey / Value: \"$customStringValue\"\nadded to $dataType")
                    clearTextFields(addCustomStringKeyEditText, addCustomStringValueEditText)
                }
            }
        }
    }

    private fun setupCustomNumberData() {
        binding.apply {

            addCustomNumberKeyEditText.addTextChangedListener {
                if (it?.isNotEmpty() == true) addCustomNumberKeyTextLayout.isErrorEnabled = false
            }

            addCustomNumberValueEditText.addTextChangedListener {
                if (it?.isNotEmpty() == true) addCustomNumberValueTextLayout.isErrorEnabled = false
            }

            addCustomNumberButton.setOnClickListener {
                val dataType = dataTypeDropdown.text?.toString()
                val customNumberKey = addCustomNumberKeyEditText.text?.toString()?.trim()
                val customNumberValue = addCustomNumberValueEditText.text?.toString()?.trim()?.toDoubleOrNull()

                val hasError = checkHasError(
                    dataTypeLayout,
                    dataType,
                    addCustomNumberKeyTextLayout,
                    customNumberKey,
                    addCustomNumberValueTextLayout,
                    customNumberValue
                )

                if (!hasError) {
                    when (dataType) {
                        DataTypes.PERSON.name -> Apptentive.addCustomPersonData(
                            customNumberKey,
                            customNumberValue
                        )
                        DataTypes.DEVICE.name -> Apptentive.addCustomDeviceData(
                            customNumberKey,
                            customNumberValue
                        )
                    }
                    makeToast("Key: $customNumberKey / Value: $customNumberValue\nadded to $dataType")
                    clearTextFields(addCustomNumberKeyEditText, addCustomNumberValueEditText)
                }
            }
        }
    }

    private fun setupCustomBooleanData() {
        val booleanValues = listOf(true, false)
        val booleanAdapter = ArrayAdapter(this, R.layout.list_item, booleanValues)
        binding.apply {
            addCustomBooleanValueDropdown.setAdapter(booleanAdapter)

            addCustomBooleanKeyEditText.addTextChangedListener {
                if (it?.isNotEmpty() == true) addCustomBooleanKeyTextLayout.isErrorEnabled = false
            }

            addCustomBooleanValueDropdown.addTextChangedListener {
                if (it?.isNotEmpty() == true) addCustomBooleanValueTextLayout.isErrorEnabled = false
            }

            addCustomBooleanButton.setOnClickListener {
                val dataType = dataTypeDropdown.text?.toString()
                val customBooleanKey = addCustomBooleanKeyEditText.text?.toString()?.trim()
                val customBooleanValue =
                    addCustomBooleanValueDropdown.text?.toString()?.trim()?.toBooleanStrictOrNull()

                val hasError = checkHasError(
                    dataTypeLayout,
                    dataType,
                    addCustomBooleanKeyTextLayout,
                    customBooleanKey,
                    addCustomBooleanValueTextLayout,
                    customBooleanValue
                )

                if (!hasError) {
                    when (dataType) {
                        DataTypes.PERSON.name -> Apptentive.addCustomPersonData(
                            customBooleanKey,
                            customBooleanValue
                        )
                        DataTypes.DEVICE.name -> Apptentive.addCustomDeviceData(
                            customBooleanKey,
                            customBooleanValue
                        )
                    }
                    makeToast("Key: $customBooleanKey / Value: $customBooleanValue\nadded to $dataType")
                    clearTextFields(addCustomBooleanKeyEditText)
                    addCustomBooleanValueDropdown.text = null
                }
            }
        }
    }

    private fun setupRemoveData() {
        binding.apply {
            removeCustomValueKeyEditText.addTextChangedListener {
                if (it?.isNotEmpty() == true) removeCustomValueKeyTextLayout.isErrorEnabled = false
            }

            removeCustomValueButton.setOnClickListener {
                val dataType = dataTypeDropdown.text?.toString()
                val removeText = removeCustomValueKeyEditText.text?.toString()?.trim()

                val hasError = checkHasError(
                    dataTypeLayout,
                    dataType,
                    removeCustomValueKeyTextLayout,
                    removeText
                )

                if (!hasError) {
                    when (dataType) {
                        DataTypes.PERSON.name -> Apptentive.removeCustomPersonData(removeText)
                        DataTypes.DEVICE.name -> Apptentive.removeCustomDeviceData(removeText)
                    }
                    makeToast("Key: $removeText\nremoved from $dataType")
                    clearTextFields(removeCustomValueKeyEditText)
                }
            }
        }
    }

    private fun setupPersonName() {
        binding.apply {
            addPersonNameButton.setOnClickListener {
                val personNameText = addPersonNameEditText.text?.toString().orEmpty().trim()

                Apptentive.setPersonName(personNameText)
                makeToast("Person name set to \"$personNameText\"")
                clearTextFields(addPersonNameEditText)
            }
        }
    }

    private fun setupPersonEmail() {
        binding.apply {
            addPersonEmailButton.setOnClickListener {
                val personEmailText = addPersonEmailEditText.text?.toString().orEmpty().trim()

                Apptentive.setPersonEmail(personEmailText)
                makeToast("Person email set to \"$personEmailText\"")
                clearTextFields(addPersonEmailEditText)
            }
        }
    }

    private fun checkHasError(
        dataTypeLayout: TextInputLayout?,
        dataType: String?,
        keyTextLayout: TextInputLayout,
        key: String?,
        valueTextLayout: TextInputLayout? = null,
        value: Any? = null
    ): Boolean {
        var hasError = false

        dataTypeLayout?.apply {
            if (dataType.isNullOrEmpty()) {
                hasError = true
                error = "Select a data type first"
            }
        }

        if (key.isNullOrEmpty()) {
            hasError = true
            keyTextLayout.error = "Field missing"
        }

        valueTextLayout?.apply {
            if (value == null) {
                hasError = true
                error = "Field missing"
            }
        }

        return hasError
    }

    private fun clearTextFields(
        keyEditText: TextInputEditText,
        valueEditText: TextInputEditText? = null
    ) {
        keyEditText.text = null
        valueEditText?.text = null
    }

    private fun makeToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    enum class DataTypes {
        PERSON, DEVICE
    }
}
