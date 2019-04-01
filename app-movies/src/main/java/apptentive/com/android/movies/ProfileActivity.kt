package apptentive.com.android.movies

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val factory = ViewModelFactory.getInstance(this)
        val viewModel = ViewModelProviders.of(this, factory).get(ProfileViewModel::class.java)

        setAutocomplete(username, viewModel.usernames)
        setAutocomplete(email, viewModel.emails)
    }

    private fun setAutocomplete(textView: AutoCompleteTextView, suggestions: Array<String>) {
        textView.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions))
    }
}
