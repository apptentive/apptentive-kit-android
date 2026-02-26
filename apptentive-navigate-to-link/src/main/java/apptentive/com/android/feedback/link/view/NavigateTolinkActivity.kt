package apptentive.com.android.feedback.link.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import apptentive.com.android.feedback.link.R
import apptentive.com.android.ui.hideSoftKeyboard
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags
import com.google.android.material.appbar.MaterialToolbar

internal class NavigateTolinkActivity : BaseNavigateToLinkActivity() {
    private lateinit var webView: WebView
    private lateinit var root: View
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private lateinit var fileChooserLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apptentive_activity_navigate_to_link)

        root = findViewById<View>(R.id.navigate_to_link_root)

        // Register the ActivityResultLauncher
        fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (uploadMessage == null) return@registerForActivityResult
            uploadMessage?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data))
            uploadMessage = null
        }

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
        settings.allowFileAccess = false
        settings.allowContentAccess = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = true
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            settings.allowFileAccessFromFileURLs = false
            @Suppress("DEPRECATION")
            settings.allowUniversalAccessFromFileURLs = false
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                if (uploadMessage != null) {
                    uploadMessage?.onReceiveValue(null)
                    uploadMessage = null
                }

                Log.i(LogTags.SURVEY, "Detected file upload using alchemer survey")

                uploadMessage = filePathCallback
                val intent = fileChooserParams?.createIntent()
                try {
                    fileChooserLauncher.launch(intent)
                } catch (e: Exception) {
                    Log.e(LogTags.SURVEY, "Error launching file chooser", e)
                    uploadMessage = null
                    return false
                }
                return true
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                title?.let { topAppBar.title = it }
            }
        }

        val url = intent.getStringExtra("linkUrl")
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else
            url?.let { webView.loadUrl(it) }

        applyWindowInsets(root)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.apply {
            clearHistory()
            clearCache(true)
            onPause()
            removeAllViews()
            destroy()
        }
    }
}
