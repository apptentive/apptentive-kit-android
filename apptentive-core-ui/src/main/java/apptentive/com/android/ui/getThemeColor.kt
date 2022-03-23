package apptentive.com.android.ui

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes


fun Context.getThemeColor(@AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}