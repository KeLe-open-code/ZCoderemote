package com.zcoderemote.app

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SavedLink(
    val id: String,
    val url: String,
    val name: String,
    val user: String,
    val pass: String,
    val savedAt: Long,
    val type: String
)

/**
 * 已保存的连接（含可选 HTTP Basic Auth 凭据），JSON 存于 SharedPreferences。
 * 按 host:port 判重：同一服务器重复粘贴时更新而非新增。
 */
object LinkStore {

    const val TYPE_ZCODE = "zcode"
    const val TYPE_KIMI = "kimi"

    private const val PREFS = "zcode_remote"
    private const val KEY = "links"

    fun load(context: Context, type: String? = null): MutableList<SavedLink> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return mutableListOf()
        val out = mutableListOf<SavedLink>()
        runCatching {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val link = SavedLink(
                    id = o.getString("id"),
                    url = o.getString("url"),
                    name = o.getString("name"),
                    user = o.optString("user"),
                    pass = o.optString("pass"),
                    savedAt = o.optLong("savedAt", 0L),
                    type = o.optString("type", TYPE_ZCODE)
                )
                if (type == null || link.type == type) out += link
            }
        }
        return out
    }

    private fun persist(context: Context, links: List<SavedLink>) {
        val arr = JSONArray()
        links.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("url", it.url)
                    .put("name", it.name)
                    .put("user", it.user)
                    .put("pass", it.pass)
                    .put("savedAt", it.savedAt)
                    .put("type", it.type)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }

    /**
     * 新增或更新（按 type + host:port 判重），新条目置顶。
     * 用户名/密码留空时保留旧条目里已保存的凭据。
     */
    fun upsert(context: Context, type: String, url: String, user: String, pass: String): SavedLink {
        val links = load(context)
        val key = hostKeyOf(url)
        val index = links.indexOfFirst { it.type == type && hostKeyOf(it.url) == key }
        val old = if (index >= 0) links[index] else null
        val link = SavedLink(
            id = old?.id ?: UUID.randomUUID().toString(),
            url = url,
            name = nameOf(url, type),
            user = user.ifBlank { old?.user ?: "" },
            pass = pass.ifBlank { old?.pass ?: "" },
            savedAt = System.currentTimeMillis(),
            type = type
        )
        if (index >= 0) links[index] = link else links.add(0, link)
        persist(context, links)
        return link
    }

    /** 删除并返回原下标（-1 表示不存在），供撤销恢复。 */
    fun remove(context: Context, id: String): Int {
        val links = load(context)
        val index = links.indexOfFirst { it.id == id }
        if (index >= 0) {
            links.removeAt(index)
            persist(context, links)
        }
        return index
    }

    fun insertAt(context: Context, link: SavedLink, index: Int) {
        val links = load(context)
        links.add(index.coerceIn(0, links.size), link)
        persist(context, links)
    }

    fun isValid(url: String): Boolean {
        if (url.isBlank()) return false
        return runCatching {
            val uri = Uri.parse(url)
            (uri.scheme == "https" || uri.scheme == "http") && !uri.host.isNullOrEmpty()
        }.getOrDefault(false)
    }

    fun isZCodeUrl(url: String): Boolean =
        runCatching { Uri.parse(url).host?.endsWith("zcode.z.ai") == true }.getOrDefault(false)

    /** 形如 host 或 host:port 的判重键。 */
    fun hostKeyOf(url: String): String = runCatching {
        val uri = Uri.parse(url)
        val port = if (uri.port > 0) ":${uri.port}" else ""
        "${uri.host}$port"
    }.getOrDefault(url)

    /** 展示名：优先取链接 name 参数（电脑端生成的名字），否则 host。 */
    fun nameOf(url: String, type: String): String = runCatching {
        if (type == TYPE_ZCODE) {
            val n = Uri.parse(url).getQueryParameter("name")
            if (!n.isNullOrBlank()) return n
        }
        val uri = Uri.parse(url)
        val port = if (uri.port > 0) ":${uri.port}" else ""
        "${uri.host}$port"
    }.getOrDefault("远程连接")
}
