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
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.android.feedback.messagecenter.R
import com.google.android.material.appbar.MaterialToolbar

class MessageCenterActivity : BaseMessageCenterActivity() {
    private lateinit var messageText: EditText
    private lateinit var messageListAdapter: MessageListAdapter
    private lateinit var greetingGroup: Group
    private lateinit var messageList: RecyclerView
    private lateinit var topAppBar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_center)

        topAppBar = findViewById(R.id.apptentive_toolbar)
        messageText = findViewById(R.id.apptentive_composer_text)
        messageList = findViewById(R.id.apptentive_message_list)
        greetingGroup = findViewById(R.id.apptentive_message_center_greeting_group)
        messageListAdapter = MessageListAdapter()

        topAppBar.title = viewModel.title
        messageText.hint = viewModel.composerHint
        findViewById<TextView>(R.id.apptentive_message_center_greeting).text = viewModel.greeting
        findViewById<TextView>(R.id.apptentive_message_center_greeting_body).text = viewModel.greetingBody

        messageList.adapter = messageListAdapter
        messageListAdapter.submitList(viewModel.messages) {
            val lastItem = messageListAdapter.currentList.size - 1
            if (lastItem >= 0) messageList.scrollToPosition(lastItem) // TODO Scroll to first unread
        }

        if (viewModel.isFirstLaunch && viewModel.messages.isEmpty()) greetingGroup.visibility = View.VISIBLE
        else messageList.visibility = View.VISIBLE

        // SupportActionBar should be set before setting NavigationOnClickListener
        setSupportActionBar(topAppBar)

        addObservers()
        setListeners()
    }

    private fun addObservers() {
        viewModel.exitStream.observe(this) { exit ->
            if (exit) finish()
        }

        viewModel.clearMessageStream.observe(this) { clearMessage ->
            if (clearMessage) messageText.text.clear()
        }

        viewModel.newMessages.observe(this) { newMessages ->
            greetingGroup.visibility = View.GONE
            messageList.visibility = View.VISIBLE
            messageListAdapter.submitList(newMessages) {
                val lastItem = messageListAdapter.currentList.size - 1
                if (lastItem >= 0) messageList.smoothScrollToPosition(lastItem) // TODO Scroll to first unread
            }
        }
    }

    private fun setListeners() {
        topAppBar.setNavigationOnClickListener {
            viewModel.exitMessageCenter()
        }
        val sendButton = findViewById<ImageView>(R.id.apptentive_send_image)
        sendButton.setOnClickListener {
            viewModel.sendMessage(messageText.text.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onMessageViewStatusChanged(true)
    }

    override fun onStop() {
        viewModel.onMessageViewStatusChanged(false)
        super.onStop()
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
