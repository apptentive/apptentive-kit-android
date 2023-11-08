package apptentive.com.android.feedback.messagecenter.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import apptentive.com.android.feedback.messagecenter.R
import apptentive.com.android.R as CoreR
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

internal class ProfileView(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    private var profileName: String = ""
    private var profileEmail: String = ""
    private var nameEditText: TextInputEditText
    private var emailEditText: TextInputEditText
    private var nameInputLayout: TextInputLayout
    private var emailInputLayout: TextInputLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.apptentive_message_profile, this, true)
        nameEditText = findViewById(R.id.apptentive_profile_name_text)
        emailEditText = findViewById(R.id.apptentive_profile_email_text)
        nameInputLayout = findViewById(R.id.apptentive_profile_name_layout)
        emailInputLayout = findViewById(R.id.apptentive_profile_email_layout)
        nameEditText.addTextChangedListener {
            profileName = it.toString()
            nameInputLayout.error = null
        }
        emailEditText.addTextChangedListener {
            profileEmail = it.toString()
            emailInputLayout.error = null
        }
    }

    fun getName() = profileName

    fun getEmail() = profileEmail

    fun updateName(name: String) {
        nameEditText.setText(name)
        profileName = name
    }

    fun updateEmail(email: String) {
        emailEditText.setText(email)
        profileEmail = email
    }

    fun setNameHint(hint: String) {
        nameInputLayout.hint = hint
    }

    fun setEmailHint(hint: String) {
        emailInputLayout.hint = hint
    }

    fun setEmailError(value: Boolean) {
        if (value) emailInputLayout.error = context.getString(CoreR.string.apptentive_email_validation_error)
        else emailInputLayout.error = null
    }

    fun setNameError(value: Boolean) {
        if (value) nameInputLayout.error = context.getString(CoreR.string.apptentive_email_validation_error)
        else nameInputLayout.error = null
    }
}
