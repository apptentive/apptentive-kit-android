package apptentive.com.app

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.app.databinding.ActivityDevFunctionsBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class DevFunctionsActivity : AppCompatActivity(), ApptentiveActivityInfo {
    lateinit var binding: ActivityDevFunctionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val isNightMode = prefs.getBoolean(EXTRA_NIGHT_MODE, false)
        delegate.localNightMode = if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        binding = ActivityDevFunctionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        setupFunctionTypeDropdown()

        // Custom Data
        setupDataTypeDropdown()
        setupCustomStringData()
        setupCustomNumberData()
        setupCustomBooleanData()
        setupRemoveData()
        setupPersonName()
        setupPersonEmail()

        // Message Center
        setupMessageCenterHiddenText()
        setupMessageCenterHiddenTextFile()
        setupSendAttachmentFile()
        setupFilePicker()
        setupPhotoPicker()
    }

    //region Data
    private fun setupFunctionTypeDropdown() {
        val DATA = "DATA"
        val MESSAGE_CENTER = "MESSAGE CENTER"

        val functionTypeValues = listOf(DATA, MESSAGE_CENTER)
        val functionTypesAdapter = ArrayAdapter(this, R.layout.list_item, functionTypeValues)

        binding.apply {
            dataTypesLayout.isVisible = functionsTypeDropdown.text.toString() == DATA
            messageCenterLayout.isVisible = functionsTypeDropdown.text.toString() == MESSAGE_CENTER

            functionsTypeDropdown.setAdapter(functionTypesAdapter)

            functionsTypeDropdown.addTextChangedListener {
                dataTypesLayout.isVisible = it.toString() == DATA
                messageCenterLayout.isVisible = it.toString() == MESSAGE_CENTER
            }

            functionsTypeDropdown.setText(MESSAGE_CENTER, false)
        }
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
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    enum class DataTypes {
        PERSON, DEVICE
    }
    //endregion

    //region Message Center
    private fun setupMessageCenterHiddenText() {
        binding.apply {
            hiddenMessageCenterTextButton.setOnClickListener {
                val hiddenText = hiddenMessageCenterTextEditText.text?.toString()?.trim()
                if (!hiddenText.isNullOrEmpty()) {
                    Apptentive.sendAttachmentText(hiddenText)
                    hiddenMessageCenterTextLayout.isErrorEnabled = false
                    hiddenMessageCenterTextLayout.error = ""
                    hiddenMessageCenterTextEditText.setText("")

                    makeToast("Hidden text sent: $hiddenText")
                } else {
                    hiddenMessageCenterTextLayout.isErrorEnabled = true
                    hiddenMessageCenterTextLayout.error = "No text entered"
                }
            }
        }
    }

    private fun setupMessageCenterHiddenTextFile() {
        binding.apply {
            hiddenMessageCenterTextFileButton.setOnClickListener {
                val text = hiddenMessageCenterTextFileEditText.text?.toString()
                val bytes = text?.toByteArray()
                Apptentive.sendAttachmentFile(bytes, "text/plain")
                hiddenMessageCenterTextFileEditText.setText("")

                makeToast("Hidden text file sent.\nText: $text\nBytes: $bytes")
            }
        }
    }

    private fun setupSendAttachmentFile() {
        val PPTX = "test.pptx"
        val DOCX = "test.docx"
        val PDF = "test.pdf"

        val attachmentTypeValues = listOf(PPTX, DOCX, PDF)
        val attachmentTypesAdapter = ArrayAdapter(this, R.layout.list_item, attachmentTypeValues)

        binding.apply {
            attachmentFileDropdown.setAdapter(attachmentTypesAdapter)

            sendAttachmentFileButton.setOnClickListener {
                if (!attachmentFileDropdown.text.isNullOrEmpty()) {
                    val fileName = attachmentFileDropdown.text.toString()
                    val uri = createFileAssetUriString(fileName)
                    val extension = MimeTypeMap.getFileExtensionFromUrl(uri)
                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                    val fis = openFileAsset(this@DevFunctionsActivity, fileName)
                    Apptentive.sendAttachmentFile(fis, mimeType)

                    attachmentFileLayout.isErrorEnabled = false
                    attachmentFileLayout.error = ""

                    makeToast(
                        "Attachment by stream sent.\nFile name: $fileName\n" +
                            "URI: $uri\nExtension: $extension\nMime Type: $mimeType"
                    )
                } else {
                    attachmentFileLayout.isErrorEnabled = true
                    attachmentFileLayout.error = "No file selected"
                }
            }
        }
    }

    private fun setupFilePicker() {
        binding.apply {
            filePickerButton.setOnClickListener {
                selectFile.launch("*/*")
            }

            sendFileButton.setOnClickListener {
                if (!filePickerEditText.text.isNullOrEmpty()) {
                    Apptentive.sendAttachmentFile(fileUri.toString())
                    filePickerTextLayout.isErrorEnabled = false
                    filePickerTextLayout.error = ""

                    makeToast("Attachment by URI sent.\n URI: $fileUri")
                } else {
                    filePickerTextLayout.isErrorEnabled = true
                    filePickerTextLayout.error = "No file selected"
                }
            }
        }
    }

    private var fileUri: Uri? = null
    private val selectFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { returnUri ->
            returnUri?.let { uri ->
                fileUri = uri
                binding.filePickerEditText.setText(getFileName(contentResolver, uri))
            } ?: makeToast("Picker cancelled")
        }

    private fun setupPhotoPicker() {
        binding.apply {
            photoPickerButton.setOnClickListener {
                launchCamera()
            }

            sendPhotoButton.setOnClickListener {
                if (!photoPickerEditText.text.isNullOrEmpty()) {
                    Apptentive.sendAttachmentFile(photoUri.toString())
                    photoPickerTextLayout.isErrorEnabled = false
                    photoPickerTextLayout.error = ""

                    makeToast("Photo by URI sent.\n URI: $photoUri")
                } else {
                    photoPickerTextLayout.isErrorEnabled = true
                    photoPickerTextLayout.error = "No photo taken"
                }
            }
        }
    }

    private val requestCameraPermissionAndTakePic =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { cameraPermissionGranted ->
            when {
                cameraPermissionGranted -> makePhotoFileAndTakePicture()
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> makeToast("Camera permission is required to take a photo")
                else -> openPermissionNeededDialog(this, "Camera", "take a photo")
            }
        }

    private var photoUri: Uri? = null
    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) binding.photoPickerEditText.setText(
                getFileName(
                    contentResolver,
                    photoUri!!
                )
            )
            else makeToast("Camera cancelled")
        }

    private fun makePhotoFileAndTakePicture() {
        createImageFile(this@DevFunctionsActivity)?.let {
            photoUri = FileProvider.getUriForFile(
                this@DevFunctionsActivity,
                "${BuildConfig.APPLICATION_ID}.provider",
                it
            )
        }

        photoUri?.apply { takePhoto.launch(this) } ?: makeToast("Error occurred while creating image file")
    }

    private fun launchCamera() {
        requestCameraPermissionAndTakePic.launch(Manifest.permission.CAMERA)
    }

    override fun onResume() {
        super.onResume()
        Apptentive.registerApptentiveActivityInfoCallback(this)
    }

    override fun getApptentiveActivityInfo(): Activity {
        return this
    }

    override fun onPause() {
        Apptentive.unregisterApptentiveActivityInfoCallback()
        super.onPause()
    }

    //endregion
}
