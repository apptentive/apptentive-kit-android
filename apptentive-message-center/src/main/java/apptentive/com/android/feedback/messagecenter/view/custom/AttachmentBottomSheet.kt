package apptentive.com.android.feedback.messagecenter.view.custom

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import apptentive.com.android.feedback.messagecenter.R
import apptentive.com.android.feedback.messagecenter.view.ImagePreviewActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AttachmentBottomSheet(
    private val fileName: String?,
    private val filePath: String?,
    private val onDeleteCallback: () -> Unit
) : BottomSheetDialogFragment() {
    private lateinit var previewButton: TextViewButton
    private lateinit var deleteButton: TextViewButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.apptentive_handle_attachment_bottomsheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start expanded in landscape mode
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        previewButton = view.findViewById(R.id.apptentive_preview_attachment_button)
        deleteButton = view.findViewById(R.id.apptentive_remove_attachment_button)

        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        previewButton.setOnClickListener {
            context?.startActivity(
                Intent(context, ImagePreviewActivity::class.java).apply {
                    putExtra(APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILENAME, fileName)
                    putExtra(APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILEPATH, filePath)
                }
            )

            dismiss()
        }

        deleteButton.setOnClickListener {
            onDeleteCallback.invoke()
            dismiss()
        }
    }

    override fun onPause() {
        dismiss()
        super.onPause()
    }

    internal companion object {
        private const val APPTENTIVE_ATTACHMENT_BOTTOMSHEET = "apptentive.attachment.bottomsheet"
        internal const val APPTENTIVE_ATTACHMENT_BOTTOMSHEET_TAG = "$APPTENTIVE_ATTACHMENT_BOTTOMSHEET.tag"
        internal const val APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILENAME = "$APPTENTIVE_ATTACHMENT_BOTTOMSHEET.filename"
        internal const val APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILEPATH = "$APPTENTIVE_ATTACHMENT_BOTTOMSHEET.filepath"
    }
}
