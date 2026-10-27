package com.zcoderemote.app

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar

class KimiAuthDialog(context: Context) : Dialog(context) {

    val webView = WebView(context)

    init {
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        val toolbar = MaterialToolbar(context).apply {
            title = context.getString(R.string.kimi_authorization)
            navigationIcon = AppCompatResources.getDrawable(context, R.drawable.ic_back)
            navigationContentDescription = context.getString(R.string.close_authorization)
            setTitleTextColor(ContextCompat.getColor(context, R.color.zr_text))
            setNavigationOnClickListener { dismiss() }
        }
        content.addView(
            toolbar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (52 * context.resources.displayMetrics.density).toInt()
            )
        )
        content.addView(
            webView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        setContentView(content)
        window?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, R.color.zr_bg)))
        setupWebView()
        setOnDismissListener {
            webView.stopLoading()
            webView.destroy()
        }
    }

    override fun onStart() {
        super.onStart()
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
        }
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webView.webViewClient = WebViewClient()
    }
}
