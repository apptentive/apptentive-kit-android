package apptentive.com.android.feedback.textmodal

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
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
    private lateinit var alternateTextView: MaterialTextView
    private lateinit var dialog: Dialog
    private lateinit var noteView: View
    private lateinit var buttonLayout: LinearLayout
    private var isImageHeightSet: Boolean = false

    @SuppressLint("UseGetLayoutInflater", "InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (!Apptentive.isApptentiveActivityInfoCallbackRegistered()) {
            // Calling this in onCreateDialog in case we lose the Activity reference from the
            // last Activity for whatever reason (garbage collection while app is in the background)
            Apptentive.registerApptentiveActivityInfoCallback(this)
        }

        isImageHeightSet = false
        dialog = MaterialAlertDialogBuilder(requireContext()).apply {
            val contextWrapper = ContextThemeWrapper(requireContext(), R.style.Theme_Apptentive).apply {
                overrideTheme()
            }
            val inflater = LayoutInflater.from(contextWrapper)
            val resource = if (viewModel.isRichNote()) {
                R.layout.apptentive_rich_note
            } else {
                R.layout.apptentive_note
            }
            noteView = inflater.inflate(resource, null)
            setView(noteView)
            val noteLayout = noteView.findViewById<LinearLayout>(R.id.apptentive_note_layout)

            when {
                viewModel.isRichNote() -> {
                    val scrollView = noteLayout.findViewById<ScrollView>(R.id.apptentive_note_scroll_view)
                    val contentLayout = inflater.inflate(R.layout.apptentive_rich_note_content, null) as LinearLayout
                    val titleView = contentLayout.findViewById<MaterialTextView>(R.id.apptentive_note_title_with_message)
                    val messageView = contentLayout.findViewById<MaterialTextView>(R.id.apptentive_note_message)
                    alternateTextView = contentLayout.findViewById(R.id.apptentive_note_alternate_text)
                    headerImageView = contentLayout.findViewById(R.id.apptentive_note_title_with_message_image)

                    scrollView.addView(contentLayout)
                    alternateTextView.text = viewModel.alternateText
                    alternateTextView.gravity = viewModel.getAlternateTextGravity()
                    headerImageView.contentDescription = viewModel.alternateText

                    if (viewModel.title == null) {
                        titleView.visibility = View.GONE
                    } else {
                        titleView.text = viewModel.title
                    }

                    if (viewModel.message == null) {
                        messageView.visibility = View.GONE
                    } else {
                        messageView.text = viewModel.message
                    }
                }

                /*
                * Material Design dialogs should always have supporting text (message).
                * Titles are optional.
                * https://material.io/components/dialogs
                */

                viewModel.title == null || viewModel.message == null -> {
                    val titleLayout = inflater.inflate(
                        R.layout.apptentive_note_title_or_message_only,
                        null
                    ) as LinearLayout
                    val titleView =
                        titleLayout.findViewById<MaterialTextView>(R.id.apptentive_note_title_or_message_only)
                    titleView.text =
                        if (viewModel.title != null) viewModel.title else viewModel.message
                    noteLayout.addView(titleLayout)
                }

                else -> {
                    val titleLayout = inflater.inflate(
                        R.layout.apptentive_note_title_with_message,
                        null
                    ) as LinearLayout
                    val titleView =
                        titleLayout.findViewById<MaterialTextView>(R.id.apptentive_note_title_with_message)
                    titleView.text = viewModel.title
                    noteLayout.addView(titleLayout)

                    val messageView = inflater.inflate(
                        R.layout.apptentive_note_message,
                        null
                    ) as MaterialTextView
                    messageView.text = viewModel.message
                    noteLayout.addView(messageView)
                }
            }

            //region Actions
            buttonLayout = inflater.inflate(R.layout.apptentive_note_actions, null) as LinearLayout

            noteLayout.addView(buttonLayout)

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
        viewModel.noteHeaderBitmapStream.value?.let { bitmap ->
            setupImage(bitmap)
        }

        return dialog.apply {
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.noteHeaderBitmapStream.observe(this) { bitmap ->
            if (this::headerImageView.isInitialized) {
                setupImage(bitmap)
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

    private fun setupImage(bitmap: Bitmap) {
        if (this::headerImageView.isInitialized) {
            alternateTextView.visibility = View.GONE
            headerImageView.visibility = View.VISIBLE
            headerImageView.setImageBitmap(bitmap)
            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            headerImageView.scaleType = viewModel.getImageScaleType()
            if (this::dialog.isInitialized && dialog.isShowing) {
                dialog.window?.decorView?.let {
                    val imageHeight = (it.width / aspectRatio).toInt()
                    val layoutParams = headerImageView.layoutParams as LinearLayout.LayoutParams
                    headerImageView.layoutParams = viewModel.getLayoutParams(layoutParams, imageHeight)
                    isImageHeightSet = true
                }
            }
            headerImageView.requestLayout()
            val padding = viewModel.getPadding(
                resources.getDimension(R.dimen.apptentive_dialog_text_horizontal_padding)
            )
            headerImageView.setPadding(padding, padding, padding, 0)

            // Resize the dialog to max height after the image is loaded and positioned
            addLayoutListener(aspectRatio)
        }
    }

    private fun addLayoutListener(aspectRatio: Float) {
        if (this::dialog.isInitialized) {
            dialog.window?.decorView?.let { dialogView ->
                dialogView.viewTreeObserver?.addOnGlobalLayoutListener(object :
                        OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            dialog.window?.decorView?.let {
                                if (!isImageHeightSet) {
                                    val imageHeight = (it.width / aspectRatio).toInt()
                                    val layoutParams =
                                        headerImageView.layoutParams as LinearLayout.LayoutParams
                                    headerImageView.layoutParams =
                                        viewModel.getLayoutParams(layoutParams, imageHeight)
                                }
                            }

                            // Remove the listener to avoid multiple calls
                            dialogView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                            val maxModalHeight = requireContext().resources.displayMetrics.heightPixels
                            val maxHeight = viewModel.getModalHeight(maxModalHeight, dialogView.height)

                            // Set the new height of the dialog
                            dialog.window?.setLayout(dialogView.width, maxHeight)
                        }
                    })
            }
        }
    }
}
