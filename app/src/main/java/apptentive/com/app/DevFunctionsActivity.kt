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

    private val customData: MutableMap<String, Any?> = mutableMapOf()

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
        setupEngageEvent()
        setupMessageCenterButton()
        setupCustomStringData()
        setupCustomNumberData()
        setupCustomBooleanData()
        setupRemoveData()
        setupPersonName()
        setupPersonEmail()
        clearCustomData()

        // Message Center
        setupMessageCenterHiddenText()
        setupMessageCenterHiddenTextFile()
        setupSendAttachmentFile()
        setupFilePicker()
        setupPhotoPicker()
    }

    private fun isPersonOrDevice(): Boolean {
        return binding.dataTypeDropdown.text.toString() in listOf(DataTypes.PERSON.name, DataTypes.DEVICE.name)
    }

    private fun addToCustomData(key: String, value: Any?) {
        customData[key] = value

        val customDataListText = buildString {
            append("Custom Data:\n")
            customData.forEach { (k, v) ->
                append("Key: $k")
                append(" / ")
                append("Value: $v\n")
            }
        }

        binding.customDataList.text = customDataListText
    }

    private fun clearCustomData() {
        customData.clear()
        binding.customDataList.text = "No Custom Data"
    }

    //region Custom Data
    private fun setupFunctionTypeDropdown() {
        val CUSTOM_DATA = "CUSTOM DATA"
        val HIDDEN_MESSAGES = "HIDDEN MESSAGES"

        val functionTypeValues = listOf(CUSTOM_DATA, HIDDEN_MESSAGES)
        val functionTypesAdapter = ArrayAdapter(this, R.layout.list_item, functionTypeValues)

        binding.apply {
            dataTypesLayout.isVisible = functionsTypeDropdown.text.toString() == CUSTOM_DATA
            hiddenMessageLayout.isVisible = functionsTypeDropdown.text.toString() == HIDDEN_MESSAGES
            customDataList.isVisible = !isPersonOrDevice()

            functionsTypeDropdown.setAdapter(functionTypesAdapter)

            functionsTypeDropdown.addTextChangedListener {
                dataTypesLayout.isVisible = it.toString() == CUSTOM_DATA
                hiddenMessageLayout.isVisible = it.toString() == HIDDEN_MESSAGES
            }
        }
    }

    private fun setupDataTypeDropdown() {
        val dataTypeValues = listOf(
            DataTypes.EVENT.name, DataTypes.MESSAGE_CENTER.name,
            DataTypes.PERSON.name, DataTypes.DEVICE.name
        )

        val dataTypesAdapter = ArrayAdapter(this, R.layout.list_item, dataTypeValues)

        binding.apply {
            dataTypeDropdown.setAdapter(dataTypesAdapter)

            dataTypeDropdown.addTextChangedListener {
                // Remove only for Person or Device (unsure if possible for Events or MC)
                removeCustomValueKeyTextLayout.isVisible = isPersonOrDevice()
                removeCustomValueButton.isVisible = isPersonOrDevice()

                // Name and email only visible for Person custom data
                personDataLayout.isVisible = it.toString() == DataTypes.PERSON.name

                // Event sending only for event custom data
                eventTextLayout.isVisible = dataTypeDropdown.text.toString() == DataTypes.EVENT.name
                engageEventButton.isVisible = dataTypeDropdown.text.toString() == DataTypes.EVENT.name

                // MC button only for Message Center custom data
                messageCenterButton.isVisible = dataTypeDropdown.text.toString() == DataTypes.MESSAGE_CENTER.name

                // Custom Data list only used for EVENT or MC
                customDataList.isVisible = !isPersonOrDevice()

                if (it.toString().isNotEmpty()) dataTypeLayout.isErrorEnabled = false
            }
        }
    }

    private fun setupEngageEvent() {
        binding.engageEventButton.setOnClickListener {
            val engageEvent = binding.eventTextEditText.text?.toString()?.trim()
            if (!engageEvent.isNullOrEmpty()) {
                Apptentive.engage(engageEvent, if (customData.isEmpty()) null else customData) {
                    makeToast("Event \'$engageEvent\' engaged with custom data: $customData")
                    clearCustomData()
                }
                binding.eventTextLayout.isErrorEnabled = false
                binding.eventTextLayout.error = ""
                binding.eventTextEditText.setText("")
            } else {
                binding.eventTextLayout.isErrorEnabled = true
                binding.eventTextLayout.error = "No event entered"
            }
        }
    }

    private fun setupMessageCenterButton() {
        binding.messageCenterButton.setOnClickListener {
            Apptentive.showMessageCenter(if (customData.isEmpty()) null else customData) {
                makeToast("Message Center engaged with custom data: $customData")
                clearCustomData()
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
                        else -> addToCustomData(customStringKey!!, customStringValue)
                    }

                    if (isPersonOrDevice()) {
                        makeToast("Key: $customStringKey / Value: \"$customStringValue\"\nadded to $dataType")
                    } else {
                        makeToast("Key: $customStringKey / Value: \"$customStringValue\" added to customData list: $customData")
                    }

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
                        else -> addToCustomData(customNumberKey!!, customNumberValue)
                    }

                    if (isPersonOrDevice()) {
                        makeToast("Key: $customNumberKey / Value: $customNumberValue\nadded to $dataType")
                    } else {
                        makeToast("Key: $customNumberKey / Value: $customNumberValue added to customData list: $customData")
                    }

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
                        else -> addToCustomData(customBooleanKey!!, customBooleanValue)
                    }
                    if (isPersonOrDevice()) {
                        makeToast("Key: $customBooleanKey / Value: \"$customBooleanValue\"\nadded to $dataType")
                    } else {
                        makeToast("Key: $customBooleanKey / Value: \"$customBooleanValue\" added to customData list: $customData")
                    }

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

                binding.personNameText.text = "Person name: ${Apptentive.getPersonName()}"
            }

            personNameText.text = "Person name: ${Apptentive.getPersonName()}"
        }
    }

    private fun setupPersonEmail() {
        binding.apply {
            addPersonEmailButton.setOnClickListener {
                val personEmailText = addPersonEmailEditText.text?.toString().orEmpty().trim()

                Apptentive.setPersonEmail(personEmailText)
                makeToast("Person email set to \"$personEmailText\"")
                clearTextFields(addPersonEmailEditText)
                binding.personEmailText.text = "Person email ${Apptentive.getPersonEmail()}"
            }

            personEmailText.text = "Person email ${Apptentive.getPersonEmail()}"
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

    enum class DataTypes(name: String) {
        PERSON("PERSON"),
        DEVICE("DEVICE"),
        EVENT("EVENT"),
        MESSAGE_CENTER("MESSAGE CENTER")
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
