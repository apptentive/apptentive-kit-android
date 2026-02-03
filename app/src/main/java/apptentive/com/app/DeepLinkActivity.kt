package apptentive.com.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import apptentive.com.app.databinding.ActivityDeeplinkBinding

class DeepLinkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDeeplinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
