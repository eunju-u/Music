package com.example.music.ui.views

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.music.R
import com.example.music.dto.MusicItem

class StickyPanelView(context: Context) : LinearLayout(context) {
    private lateinit var playBtn: ImageButton
    private lateinit var title: TextView
    private lateinit var singer: TextView
    private var isPlay = false
    private var audiouri: String? = null
    private var listener: OnPlayButtonClickListener? = null

    init {
        setView(context)
    }

    //이니셜 라이즈
    private fun setView(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.sticky_panel_layout, this, false)
        playBtn = view.findViewById(R.id.sticky_panel_play_btn)
        playBtn.setOnClickListener {
            isPlay = !isPlay
            listener?.OnClicked(playBtn, isPlay, audiouri)
        }
        title = view.findViewById(R.id.sticky_panel_title)
        singer = view.findViewById(R.id.sticky_panel_singer)
        addView(view)
    }

    fun setViewData(item: MusicItem) {
        title.text = item.title
        singer.text = item.singer
        audiouri = item.uri
    }

    fun setImage(id: Int) {
        playBtn.setImageDrawable(ContextCompat.getDrawable(context, id))
    }

    fun setOnPlayButtonClickListener(listener: OnPlayButtonClickListener?) {
        this.listener = listener
    }

    interface OnPlayButtonClickListener {
        fun OnClicked(view: ImageButton?, isPlay: Boolean, audiori: String?) //현재 뷰 넣고
    }

    companion object {
        private val TAG: String = "music play ${StickyPanelView::class.java.simpleName}"
    }
}
