package apptentive.com.android.feedback.messagecenter.view

import android.app.Activity
import android.os.Bundle
import androidx.core.net.toUri
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.feedback.messagecenter.R
import apptentive.com.android.feedback.messagecenter.view.custom.HandleAttachmentBottomSheet.Companion.APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILENAME
import apptentive.com.android.feedback.messagecenter.view.custom.HandleAttachmentBottomSheet.Companion.APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILEPATH
import apptentive.com.android.feedback.messagecenter.view.custom.SimpleTouchImageView
import apptentive.com.android.ui.ApptentiveActivity
import com.google.android.material.appbar.MaterialToolbar
import java.io.File

class ImagePreviewActivity : ApptentiveActivity(), ApptentiveActivityInfo {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apptentive_activity_attachment_preview)

        val topAppBar: MaterialToolbar = findViewById(R.id.apptentive_attachment_preview_toolbar)
        val fileName = intent.getStringExtra(APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILENAME).orEmpty()
        topAppBar.title = fileName
        setSupportActionBar(topAppBar)
        topAppBar.setNavigationOnClickListener { onBackPressed() }

        val filepath = intent.getStringExtra(APPTENTIVE_ATTACHMENT_BOTTOMSHEET_FILEPATH).orEmpty()

        val imageView: SimpleTouchImageView = findViewById(R.id.apptentive_attachment_preview_image)
        imageView.setImageURI(File(filepath).toUri())
    }

    override fun onResume() {
        super.onResume()
        Apptentive.registerApptentiveActivityInfoCallback(this)
    }

    override fun getApptentiveActivityInfo(): Activity {
        return this
    }

    override fun onPause() {
        Apptentive.unregisterApptentiveActivityInfoCallback()
        super.onPause()
    }
}
