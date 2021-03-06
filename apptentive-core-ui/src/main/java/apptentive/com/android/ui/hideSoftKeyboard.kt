package apptentive.com.android.ui

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
fun View.hideSoftKeyboard() {
    val manager = context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    manager.hideSoftInputFromWindow(windowToken, 0)
}
