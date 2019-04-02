package apptentive.com.android.movies

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProviders.of(this, factory).get(ProfileViewModel::class.java)

        userIdTextView.setText(viewModel.person.identifier)
        usernameEditText.setText(viewModel.person.name ?: "")
        emailEditText.setText(viewModel.person.email ?: "")

        setAutocomplete(usernameEditText, viewModel.usernames)
        setAutocomplete(emailEditText, viewModel.emails)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.buttonSave) {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            viewModel.person.name = username
            viewModel.person.email = email
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setAutocomplete(textView: AutoCompleteTextView, suggestions: Array<String>) {
        textView.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions))
    }
}
