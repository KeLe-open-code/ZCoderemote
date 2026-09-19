package com.zcoderemote.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SavedLinksAdapter(
    private val onClick: (SavedLink) -> Unit,
    private val onDelete: (SavedLink) -> Unit
) : RecyclerView.Adapter<SavedLinksAdapter.ViewHolder>() {

    private val items = mutableListOf<SavedLink>()
    private val dateFormat = SimpleDateFormat("yyyy/M/d HH:mm", Locale.getDefault())

    fun submit(list: List<SavedLink>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: View = view.findViewById(R.id.card)
        val name: TextView = view.findViewById(R.id.name)
        val urlHint: TextView = view.findViewById(R.id.url_hint)
        val meta: TextView = view.findViewById(R.id.meta)
        val delete: View = view.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_link, parent, false)
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.urlHint.text = item.url
        val parts = mutableListOf<String>()
        if (item.user.isNotBlank()) parts += "已存凭据"
        parts += dateFormat.format(Date(item.savedAt))
        holder.meta.text = parts.joinToString(" · ")
        holder.card.setOnClickListener { onClick(item) }
        holder.delete.setOnClickListener { onDelete(item) }
    }
}
