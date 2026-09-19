package com.zcoderemote.app

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LinkListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "url"
    }

    private lateinit var adapter: SavedLinksAdapter
    private lateinit var inputUrl: TextInputEditText
    private lateinit var emptyState: View
    private lateinit var root: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        root = findViewById(R.id.root)
        setupZCodeInsets(root)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = getString(R.string.tile_zcode_name)
        inputUrl = findViewById(R.id.input_url)
        emptyState = findViewById(R.id.empty_state)

        findViewById<TextView>(R.id.connect_title).text = "连接电脑 ZCode"
        findViewById<TextView>(R.id.connect_subtitle).setText(R.string.subtitle_zcode)
        findViewById<TextInputLayout>(R.id.input_layout).hint = getString(R.string.link_hint_zcode)
        findViewById<TextView>(R.id.empty_subtitle).setText(R.string.empty_subtitle_zcode)
        findViewById<View>(R.id.auth_row).visibility = View.GONE

        adapter = SavedLinksAdapter(
            onClick = { link -> openSession(link) },
            onDelete = { link -> deleteLink(link) }
        )
        findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(this@LinkListActivity)
            adapter = this@LinkListActivity.adapter
            isNestedScrollingEnabled = false
        }

        findViewById<TextInputLayout>(R.id.input_layout)
            .setEndIconOnClickListener { pasteFromClipboard() }

        findViewById<MaterialButton>(R.id.btn_connect).setOnClickListener {
            tryConnect(inputUrl.text?.toString()?.trim().orEmpty())
        }

        // 从深链/分享进入时预填并直连
        intent.getStringExtra(EXTRA_URL)?.takeIf { LinkStore.isValid(it) }?.let { url ->
            inputUrl.setText(url)
            connect(url)
        }
        refreshList()
    }

    private fun tryConnect(raw: String) {
        if (!LinkStore.isValid(raw)) {
            toast(R.string.invalid_url)
            return
        }
        if (!LinkStore.isZCodeUrl(raw)) {
            MaterialAlertDialogBuilder(this)
                .setMessage(R.string.not_zcode_link)
                .setPositiveButton(R.string.connect_anyway) { _, _ -> connect(raw) }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } else {
            connect(raw)
        }
    }

    private fun connect(url: String) {
        hideKeyboard()
        val link = LinkStore.upsert(this, LinkStore.TYPE_ZCODE, url, "", "")
        refreshList()
        openSession(link)
    }

    private fun openSession(link: SavedLink) {
        hideKeyboard()
        startActivity(
            Intent(this, SessionActivity::class.java)
                .putExtra(SessionActivity.EXTRA_URL, link.url)
                .putExtra(SessionActivity.EXTRA_NAME, link.name)
                .putExtra(SessionActivity.EXTRA_USER, link.user)
                .putExtra(SessionActivity.EXTRA_PASS, link.pass)
        )
    }

    private fun deleteLink(link: SavedLink) {
        val index = LinkStore.remove(this, link.id)
        refreshList()
        Snackbar.make(root, getString(R.string.deleted, link.name), Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) {
                if (index >= 0) LinkStore.insertAt(this, link, index)
                refreshList()
            }
            .show()
    }

    private fun pasteFromClipboard() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = cm.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()?.trim().orEmpty()
        val url = text.split(Regex("\\s+"))
            .firstOrNull { it.startsWith("http", ignoreCase = true) }
            ?: text.takeIf { LinkStore.isValid(it) }
        if (url == null) {
            toast(R.string.clipboard_empty)
        } else {
            inputUrl.setText(url)
            inputUrl.setSelection(url.length)
        }
    }

    private fun refreshList() {
        val links = LinkStore.load(this, LinkStore.TYPE_ZCODE)
        adapter.submit(links)
        findViewById<TextView>(R.id.saved_header).visibility =
            if (links.isEmpty()) View.GONE else View.VISIBLE
        emptyState.visibility = if (links.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.windowToken?.let { imm.hideSoftInputFromWindow(it, 0) }
    }
}
