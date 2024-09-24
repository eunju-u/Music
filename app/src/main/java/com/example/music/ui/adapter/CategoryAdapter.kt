package com.example.music.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.dto.MusicItem

class CategoryAdapter(private val list: List<MusicItem>?, private val context: Context) :
    RecyclerView.Adapter<CategoryAdapter.CategoryHolder?>() {
    private var listener: ((View?, MusicItem?) -> Unit)? = null

    inner class CategoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var musicImage: ImageView = itemView.findViewById(R.id.item_music_image)
        var musicTitle: TextView = itemView.findViewById(R.id.item_music_title)
        var musicAuth: TextView = itemView.findViewById(R.id.item_music_singer)

        init {
            itemView.setOnClickListener {
                val position = getBindingAdapterPosition()
                listener?.run {
                    if (position != RecyclerView.NO_POSITION) {
                        list?.get(position)?.let { item -> this.invoke(itemView, item) }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): CategoryHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_home_music, viewGroup, false)
        return CategoryHolder(view)
    }

    override fun getItemCount() = list?.size ?: 0

    @SuppressLint("DiscouragedApi")
    override fun onBindViewHolder(holder: CategoryHolder, i: Int) {
        list?.run {
            val item = list[i]
            holder.musicTitle.text = item.title
            holder.musicAuth.text = item.singer

            //문자열을 R의 id 찾기
            val image = context.resources.getIdentifier(item.image, "drawable", context.packageName)
            Glide.with(context)
                .load(image)
                .centerCrop()
                .into(holder.musicImage)
        }
    }

    fun setOnClickListener(listener: ((View?, MusicItem?) -> Unit)?) {
        this.listener = listener
    }
}
