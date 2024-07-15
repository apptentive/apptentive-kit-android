package apptentive.com.android.feedback.link.view

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import apptentive.com.android.feedback.link.R
import apptentive.com.android.ui.hideSoftKeyboard
import com.google.android.material.appbar.MaterialToolbar

internal class NavigateTolinkActivity : BaseNavigateToLinkActivity() {
    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apptentive_activity_navigate_to_link)

        supportActionBar?.hide()

        val topAppBar = findViewById<MaterialToolbar>(R.id.apptentive_top_app_bar)
        topAppBar.setNavigationOnClickListener {
            it.hideSoftKeyboard()
            finish()
        }

        webView = findViewById<WebView>(R.id.apptentive_webview_navigate_to_link)
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.javaScriptCanOpenWindowsAutomatically = true
        webView.webChromeClient = WebChromeClient()

        val url = intent.getStringExtra("linkUrl")
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else
            url?.let { webView.loadUrl(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }
}
