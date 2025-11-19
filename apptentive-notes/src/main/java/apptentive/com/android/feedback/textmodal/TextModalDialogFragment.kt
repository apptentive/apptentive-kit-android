package apptentive.com.android.feedback.textmodal

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.text.method.LinkMovementMethod
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
import apptentive.com.android.feedback.utils.containsLinks
import apptentive.com.android.ui.overrideTheme
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INTERACTIONS
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView

internal class TextModalDialogFragment : DialogFragment(), ApptentiveActivityInfo {

    private val viewModel by viewModels<TextModalViewModel>()
    private lateinit var headerImageView: ImageView
    private lateinit var alternateTextView: MaterialTextView
    private lateinit var dialog: Dialog
    private lateinit var noteLayout: LinearLayout
    private lateinit var buttonLayout: LinearLayout
    private var isImageHeightSet: Boolean = false

    @SuppressLint("UseGetLayoutInflater", "InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return try {
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
                val noteView = inflater.inflate(R.layout.apptentive_note, null)
                setView(noteView)
                noteLayout = noteView.findViewById(R.id.apptentive_note_layout)
                val scrollView = noteLayout.findViewById<ScrollView>(R.id.apptentive_note_scroll_view)
                val contentLayout: LinearLayout
                when {
                    /*
                    * Material Design dialogs should always have supporting text (message).
                    * Titles are optional.
                    * https://material.io/components/dialogs
                    */
                    viewModel.title == null || viewModel.message == null -> {
                        contentLayout = inflater.inflate(
                            R.layout.apptentive_note_title_or_message_only,
                            null
                        ) as LinearLayout
                        val titleView =
                            contentLayout.findViewById<MaterialTextView>(R.id.apptentive_note_title_or_message_only)
                        titleView.text =
                            if (viewModel.title != null) viewModel.title else viewModel.message
                        if (containsLinks(titleView.text.toString())) {
                            titleView.movementMethod = LinkMovementMethod.getInstance()
                        }
                        if (viewModel.title == null && viewModel.message == null) titleView.visibility = View.GONE
                        scrollView.addView(contentLayout)
                    } else -> {
                    contentLayout = inflater.inflate(
                        R.layout.apptentive_note_title_with_message,
                        null
                    ) as LinearLayout
                    val titleView =
                        contentLayout.findViewById<MaterialTextView>(R.id.apptentive_note_title_with_message)
                    val messageView = contentLayout.findViewById<MaterialTextView>(R.id.apptentive_note_message)
                    titleView.text = viewModel.title
                    if (containsLinks(titleView.text.toString()))
                        titleView.movementMethod = LinkMovementMethod.getInstance()
                    messageView.text = viewModel.message
                    if (containsLinks(viewModel.message.toString()))
                        messageView.movementMethod = LinkMovementMethod.getInstance()
                    scrollView.addView(contentLayout)
                }
                }
                alternateTextView = contentLayout.findViewById(R.id.apptentive_note_alternate_text)
                headerImageView = contentLayout.findViewById(R.id.apptentive_note_title_with_message_image)

                if (viewModel.alternateText.isNullOrEmpty()) {
                    alternateTextView.visibility = View.GONE
                } else {
                    alternateTextView.text = viewModel.alternateText
                    alternateTextView.gravity = viewModel.getAlternateTextGravity()
                    headerImageView.contentDescription = viewModel.alternateText
                    // Hide the image view until the image is loaded
                    headerImageView.visibility = View.GONE
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

                viewModel.onDismiss = {
                    this@TextModalDialogFragment.dismiss()
                    finishActivity(arguments)
                }
            }.create()

            dialog.apply {
                setOnShowListener {
                    window?.decorView?.post {
                        viewModel.noteHeaderBitmapStream.value?.let { bitmap ->
                            setupImage(bitmap)
                        }
                        // Set the dialog to be non-cancelable on touch outside
                        setCanceledOnTouchOutside(false)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(INTERACTIONS, "Error creating TextModalDialogFragment", e)
            Dialog(requireContext()).apply {
                setOnShowListener {
                    dismiss() // silently close itself
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.dismissInteraction.observe(this) {
            dismiss()
        }
        viewModel.noteHeaderBitmapStream.observe(this) { bitmap ->
            setupImage(bitmap)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        viewModel.onCancel()
        super.onCancel(dialog)
        finishActivity(arguments)
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
                    if (it.width != 0) {
                        val imageHeight = (it.width / aspectRatio).toInt()
                        val layoutParams = headerImageView.layoutParams as LinearLayout.LayoutParams
                        headerImageView.layoutParams =
                            viewModel.getLayoutParams(layoutParams, imageHeight)
                        isImageHeightSet = true
                    }
                }
            }
            val padding = viewModel.getPadding(
                resources.getDimension(R.dimen.apptentive_dialog_text_horizontal_padding)
            )
            headerImageView.setPadding(padding, padding, padding, resources.getDimension(R.dimen.apptentive_dialog_text_horizontal_padding).toInt())
            // Readjust the padding for the note layout if image is present
            noteLayout.setPadding(0, 0, 0, 0)

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

                        if (viewModel.maxHeight < 100) {
                            val maxModalHeight =
                                requireContext().resources.displayMetrics.heightPixels
                            val maxHeight =
                                viewModel.getModalHeight(maxModalHeight, dialogView.height)

                            // Set the new height of the dialog
                            dialog.window?.setLayout(dialogView.width, maxHeight)
                        }
                    }
                })
            }
        }
    }

    private fun finishActivity(arguments: Bundle?) {
        if (arguments?.getBoolean("IS_SDK_HOST_ACTIVITY") == true && requireActivity().isFinishing.not()) {
            requireActivity().finish()
        }
    }
}
