package com.zcoderemote.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupZCodeInsets(findViewById(R.id.root))

        findViewById<MaterialToolbar>(R.id.toolbar).setTitle(R.string.app_name)

        findViewById<android.view.View>(R.id.tile_zcode).setOnClickListener {
            startActivity(listIntent(LinkStore.TYPE_ZCODE))
        }
        findViewById<android.view.View>(R.id.tile_kimi).setOnClickListener {
            startActivity(listIntent(LinkStore.TYPE_KIMI))
        }

        // 冷启动直达（深链/分享）：onCreate 与 onNewIntent 两条路径都要处理
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        refreshCounts()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun listIntent(type: String, url: String? = null): Intent =
        Intent(this, LinkListActivity::class.java)
            .putExtra(LinkListActivity.EXTRA_TYPE, type)
            .putExtra(LinkListActivity.EXTRA_URL, url)

    /** 深链/分享进入：自动带入链接。 */
    private fun handleIntent(intent: Intent?) {
        val url = when (intent?.action) {
            Intent.ACTION_VIEW -> intent.data?.toString()
            Intent.ACTION_SEND ->
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { extractUrl(it) }
            else -> null
        } ?: return
        if (!LinkStore.isValid(url)) return
        val type = if (LinkStore.isZCodeUrl(url)) LinkStore.TYPE_ZCODE else LinkStore.TYPE_KIMI
        startActivity(listIntent(type, url))
    }

    private fun extractUrl(text: String): String? =
        text.split(Regex("\\s+")).firstOrNull { it.startsWith("http", ignoreCase = true) }

    private fun refreshCounts() {
        findViewById<TextView>(R.id.count_zcode).text =
            getString(R.string.saved_count, LinkStore.load(this, LinkStore.TYPE_ZCODE).size)
        findViewById<TextView>(R.id.count_kimi).text =
            getString(R.string.saved_count, LinkStore.load(this, LinkStore.TYPE_KIMI).size)
    }
}
