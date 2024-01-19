package apptentive.com.android.feedback.textmodal

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.feedback.notes.R
import apptentive.com.android.ui.overrideTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView

internal class TextModalDialogFragment : DialogFragment(), ApptentiveActivityInfo {

    private val viewModel by viewModels<TextModalViewModel>()
    private lateinit var headerImageView: ImageView
    private lateinit var dialog: Dialog

    @SuppressLint("UseGetLayoutInflater", "InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (!Apptentive.isApptentiveActivityInfoCallbackRegistered()) {
            // Calling this in onCreateDialog in case we lose the Activity reference from the
            // last Activity for whatever reason (garbage collection while app is in the background)
            Apptentive.registerApptentiveActivityInfoCallback(this)
        }

        dialog = MaterialAlertDialogBuilder(requireContext()).apply {

            val contextWrapper = ContextThemeWrapper(requireContext(), R.style.Theme_Apptentive).apply {
                overrideTheme()
            }
            val inflater = LayoutInflater.from(contextWrapper)
            val noteView = inflater.inflate(R.layout.apptentive_note, null)
            setView(noteView)

            val noteLayout = noteView.findViewById<LinearLayout>(R.id.apptentive_note_layout)

            val noteContentView = when {
                /*
                 * Material Design dialogs should always have supporting text (message).
                 * Titles are optional.
                 * https://material.io/components/dialogs
                */
                viewModel.title == null || viewModel.message == null -> {
                    val titleOrMessageView = inflater.inflate(R.layout.apptentive_note_title_or_message_only, null) as LinearLayout
                    val titleOrMessageText = titleOrMessageView.findViewById<MaterialTextView>(R.id.apptentive_note_title_or_message_only)
                    headerImageView = titleOrMessageView.findViewById(R.id.apptentive_note_title_or_message_only_image)
                    headerImageView.contentDescription = viewModel.alternateText
                    viewModel.noteHeaderBitmapStream.value?.let { bitmap ->
                        headerImageView.setImageBitmap(bitmap)
                    }
                    titleOrMessageText.text = if (viewModel.title != null) viewModel.title else viewModel.message
                    noteLayout.addView(titleOrMessageView)
                    titleOrMessageView
                }
                else -> {
                    val titleAndMessageView = inflater.inflate(R.layout.apptentive_note_title_with_message, null)
                    val titleAndMessageLayout = titleAndMessageView.findViewById<LinearLayout>(R.id.apptentive_note_title_with_message_layout)
                    val titleView = titleAndMessageLayout.findViewById<MaterialTextView>(R.id.apptentive_note_title_with_message)
                    val messageView = titleAndMessageLayout.findViewById<MaterialTextView>(R.id.apptentive_note_message)
                    headerImageView = titleAndMessageLayout.findViewById(R.id.apptentive_note_title_with_message_image)

                    headerImageView.contentDescription = viewModel.alternateText
                    viewModel.noteHeaderBitmapStream.value?.let { bitmap ->
                        headerImageView.setImageBitmap(bitmap)
                    }
                    titleView.text = viewModel.title
                    messageView.text = viewModel.message
                    noteLayout.addView(titleAndMessageView)
                    titleAndMessageView
                }
            }

            //region Actions
            val buttonLayout = noteContentView.findViewById<LinearLayout>(R.id.apptentive_note_button_layout)

            viewModel.actions.forEach { action ->
                val button = inflater.inflate(R.layout.apptentive_note_action, null) as MaterialButton
                val dismissButton = inflater.inflate(R.layout.apptentive_note_dismiss_action, null) as MaterialButton

                when (action) {
                    is TextModalViewModel.ActionModel.DismissActionModel -> {
                        dismissButton.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                        dismissButton.text = action.title
                        buttonLayout.addView(dismissButton)
                        dismissButton.setOnClickListener { action.invoke() }
                    }
                    is TextModalViewModel.ActionModel.OtherActionModel -> {
                        button.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                        button.text = action.title
                        buttonLayout.addView(button)
                        button.setOnClickListener { action.invoke() }
                    }
                }
            }
            //endregion

            viewModel.onDismiss = { this@TextModalDialogFragment.dismiss() }
        }.create()

        // Add an OnGlobalLayoutListener to the root view

        val dialogView = dialog.window?.decorView
        dialogView?.viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Remove the listener to avoid multiple calls
                dialogView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // Get the measured width and height
                val width: Int = dialogView.width
                val height: Int = dialogView.height

                if (viewModel.maxHeight != 0) {
                    val maxHeight = height * viewModel.maxHeight / 100
                    dialog.window?.setLayout(width, maxHeight)
                }
            }
        })

        return dialog.apply {
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.noteHeaderBitmapStream.observe(this) { bitmap ->
            if (this::headerImageView.isInitialized) {
                headerImageView.setImageBitmap(bitmap)
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        viewModel.onCancel()
        super.onCancel(dialog)
    }

    override fun getApptentiveActivityInfo(): Activity {
        return requireActivity()
    }

    private fun addLayoutListener(dialogView: View?) {
        dialogView?.viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Remove the listener to avoid multiple calls
                dialogView.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                // Get the measured width and height
                val width: Int = dialogView.width
                val height: Int = dialogView.height

                if (viewModel.maxHeight != 0) {
                    val maxHeight = height * viewModel.maxHeight
                    dialog.window?.setLayout(width, maxHeight)
                }
            }
        })
    }
}
