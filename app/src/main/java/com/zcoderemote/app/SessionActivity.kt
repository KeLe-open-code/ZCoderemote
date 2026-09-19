package com.zcoderemote.app

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.HttpAuthHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class SessionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "url"
        const val EXTRA_NAME = "name"
        const val EXTRA_USER = "user"
        const val EXTRA_PASS = "pass"
    }

    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar
    private lateinit var errorOverlay: View
    private var linkUrl: String = ""
    private var authUser: String = ""
    private var authPass: String = ""
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val fileChooser =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            filePathCallback?.onReceiveValue(uris?.takeIf { it.isNotEmpty() }?.toTypedArray())
            filePathCallback = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)
        setupZCodeInsets(findViewById(R.id.root))

        linkUrl = intent.getStringExtra(EXTRA_URL).orEmpty()
        val name = intent.getStringExtra(EXTRA_NAME) ?: getString(R.string.app_name)
        authUser = intent.getStringExtra(EXTRA_USER).orEmpty()
        authPass = intent.getStringExtra(EXTRA_PASS).orEmpty()

        // 远程操作期间保持屏幕常亮
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        webView = findViewById(R.id.webview)
        progress = findViewById(R.id.progress)
        errorOverlay = findViewById(R.id.error_overlay)

        findViewById<MaterialToolbar>(R.id.toolbar).apply {
            title = name
            setNavigationOnClickListener { finish() }
            inflateMenu(R.menu.session_menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_refresh -> {
                        hideError()
                        webView.reload()
                        true
                    }
                    R.id.action_copy -> {
                        copyLink()
                        true
                    }
                    R.id.action_browser -> {
                        openInBrowser()
                        true
                    }
                    else -> false
                }
            }
        }

        findViewById<View>(R.id.btn_retry).setOnClickListener {
            hideError()
            webView.loadUrl(linkUrl)
        }

        setupWebView()
        webView.loadUrl(linkUrl)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
        }
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val uri = request.url
                // 同一服务器（host:port 一致）的跳转留在应用内，其余交给系统浏览器
                return if (LinkStore.hostKeyOf(uri.toString()) == LinkStore.hostKeyOf(linkUrl)) {
                    false
                } else {
                    runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                    true
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                progress.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) showError()
            }

            override fun onReceivedHttpAuthRequest(
                view: WebView,
                handler: HttpAuthHandler,
                host: String,
                realm: String
            ) {
                if (authUser.isNotBlank() || authPass.isNotBlank()) {
                    handler.proceed(authUser, authPass)
                } else {
                    handler.cancel()
                    toast(R.string.auth_missing)
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (newProgress >= 100) {
                    progress.visibility = View.GONE
                } else {
                    progress.visibility = View.VISIBLE
                    progress.progress = newProgress
                }
            }

            override fun onShowFileChooser(
                view: WebView,
                callback: ValueCallback<Array<Uri>>,
                params: FileChooserParams
            ): Boolean {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = callback
                runCatching { fileChooser.launch("*/*") }
                    .onFailure {
                        callback.onReceiveValue(null)
                        filePathCallback = null
                    }
                return true
            }
        }

        webView.setDownloadListener(DownloadListener { url, _, _, _, _ ->
            runCatching {
                val request = DownloadManager.Request(Uri.parse(url))
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
                toast(R.string.downloading)
            }
        })
    }

    private fun showError() {
        progress.visibility = View.GONE
        errorOverlay.visibility = View.VISIBLE
    }

    private fun hideError() {
        errorOverlay.visibility = View.GONE
    }

    private fun copyLink() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("Agent Remote", linkUrl))
        // Android 13+ 系统自带复制提示，避免重复
        if (Build.VERSION.SDK_INT < 33) toast(R.string.link_copied)
    }

    private fun openInBrowser() {
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl))) }
            .onFailure { toast(R.string.no_browser) }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
