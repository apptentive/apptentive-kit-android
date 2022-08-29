package apptentive.com.android.feedback.messagecenter.view.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import com.google.android.material.textview.MaterialTextView

/**
 * Basic custom view that lets Talkback announce a TextView as a button.
 */
class TextViewButton(context: Context, attrs: AttributeSet) : MaterialTextView(context, attrs) {

    init {
        isClickable = true
        isFocusable = true
    }

    override fun getAccessibilityClassName(): CharSequence = Button::class.java.name
}
