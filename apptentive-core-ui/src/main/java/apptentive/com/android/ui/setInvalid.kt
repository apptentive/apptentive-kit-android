package apptentive.com.android.ui

import apptentive.com.android.util.InternalUseOnly
import com.google.android.material.textfield.TextInputLayout

@InternalUseOnly
fun TextInputLayout.setInvalid(isInvalid: Boolean){
    /* the reason for doing this check is to avoid multiple error messages being displayed
    below the TextInputLayout. The TextInputLayout already supports an error message as part
    of material design and we also have an explicit error text view in the layout */
    this.error = if (isInvalid) " " else null
}