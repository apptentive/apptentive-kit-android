package apptentive.com.android.feedback.messagecenter.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import apptentive.com.android.feedback.messagecenter.R
import com.google.android.material.appbar.MaterialToolbar

class MessageCenterActivity : BaseMessageCenterActivity() {
    private lateinit var messageText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_center)

        val topAppBar = findViewById<MaterialToolbar>(R.id.apptentive_toolbar)
        topAppBar.title = viewModel.title

        // SupportActionBar should be set before setting NavigationOnClickListener
        setSupportActionBar(topAppBar)

        topAppBar.setNavigationOnClickListener {
            viewModel.exitMessageCenter()
        }

        messageText = findViewById(R.id.apptentive_composer_text)

        val sendButton = findViewById<ImageView>(R.id.apptentive_send_image)
        sendButton.setOnClickListener {
            viewModel.sendMessage(messageText.text.toString())
        }

        findViewById<TextView>(R.id.apptentive_message_center_greeting).text = viewModel.greeting
        findViewById<TextView>(R.id.apptentive_message_center_greeting_body).text = viewModel.greetingBody
        if (viewModel.isFirstLaunch)
            findViewById<Group>(R.id.apptentive_message_center_greeting_group).visibility = View.VISIBLE

        addObservers()
    }

    private fun addObservers() {
        viewModel.exitStream.observe(this) { exit ->
            if (exit) finish()
        }

        viewModel.clearMessageStream.observe(this) { clearMessage ->
            if (clearMessage) messageText.text.clear()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.message_center_action, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_profile)
            Toast.makeText(this, "Profile is selected", Toast.LENGTH_SHORT).show()
        return true
    }
}
