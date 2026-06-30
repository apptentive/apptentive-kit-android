package apptentive.com.android.feedback.messagecenter.view

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.feedback.messagecenter.R
import apptentive.com.android.feedback.messagecenter.view.custom.AttachmentBottomSheet.Companion.APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILENAME
import apptentive.com.android.feedback.messagecenter.view.custom.AttachmentBottomSheet.Companion.APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILEPATH
import apptentive.com.android.feedback.messagecenter.view.custom.SimpleTouchImageView
import apptentive.com.android.ui.ApptentiveActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView
import java.io.File

internal class ImagePreviewActivity : ApptentiveActivity(), ApptentiveActivityInfo {

    private lateinit var root: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apptentive_activity_attachment_preview)

        root = findViewById<View>(R.id.apptentive_attachment_preview_root)

        val topAppBar: MaterialToolbar = findViewById(R.id.apptentive_attachment_preview_toolbar)
        val topAppBarTitle: MaterialTextView = findViewById(R.id.apptentive_attachment_preview_title)
        val fileName = intent.getStringExtra(APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILENAME).orEmpty()
        title = fileName
        topAppBar.title = ""
        topAppBarTitle.text = fileName
        setSupportActionBar(topAppBar)
        topAppBar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val filepath = intent.getStringExtra(APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILEPATH).orEmpty()

        val imageView: SimpleTouchImageView = findViewById(R.id.apptentive_attachment_preview_image)
        imageView.setImageURI(File(filepath).toUri())
        applyWindowInsets(root)
    }

    override fun onResume() {
        super.onResume()
        Apptentive.registerApptentiveActivityInfoCallback(this)
    }

    override fun getApptentiveActivityInfo(): Activity {
        return this
    }
}
