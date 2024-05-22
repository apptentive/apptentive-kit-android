package apptentive.com.android.feedback.link.view

import android.os.Bundle
import android.webkit.WebView
import apptentive.com.android.feedback.link.R
import apptentive.com.android.ui.hideSoftKeyboard
import com.google.android.material.appbar.MaterialToolbar
import android.webkit.WebSettings

internal class NavigateTolinkActivity : BaseNavigateToLinkActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apptentive_activity_navigate_to_link)

        supportActionBar?.hide()

        val topAppBar = findViewById<MaterialToolbar>(R.id.apptentive_top_app_bar)
        topAppBar.setNavigationOnClickListener {
            it.hideSoftKeyboard()
            finish()
        }

        val webView = findViewById<WebView>(R.id.apptentive_webview_navigate_to_link)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        val url = intent.getStringExtra("linkUrl")
        if (url != null) {
            webView.loadUrl(url)
        }
    }
}
