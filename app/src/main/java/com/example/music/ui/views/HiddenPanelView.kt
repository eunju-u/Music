package com.example.music.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.music.R
import com.example.music.dto.MusicItem

class HiddenPanelView(context: Context) : LinearLayout(context) {
    private lateinit var mainImage: ImageView
    private lateinit var title: TextView
    private lateinit var singer: TextView
    private lateinit var downBtn: ImageView
    private lateinit var seekBar: SeekBar
    private lateinit var playBtn: ImageButton

    private var isPlay = false
    private var audiouri: String? = null

    private var hiddenViewListener: OnHiddenViewClickListener? = null

    init {
        setView(context)
    }

    private fun setView(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.hidden_panel_layout, this, false)
        mainImage = view.findViewById(R.id.hidden_panel_background_image)

        title = view.findViewById(R.id.hidden_panel_title)
        singer = view.findViewById(R.id.hidden_panel_singer)
        seekBar = view.findViewById(R.id.hidden_panel_seek_bar)
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                hiddenViewListener?.progress(seekBar, progress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
        downBtn = view.findViewById(R.id.hidden_panel_down_btn)
        downBtn.setOnClickListener {
            hiddenViewListener?.clickCloseButton()
        }
        playBtn = view.findViewById(R.id.hidden_panel_play_btn)
        playBtn.setOnClickListener {
            isPlay = !isPlay
            hiddenViewListener?.clickPlayButton(playBtn, isPlay, audiouri)
        }
        addView(view)
    }

    @SuppressLint("DiscouragedApi")
    fun setViewDate(item: MusicItem) {
        title.text = item.title
        singer.text = item.singer
        audiouri = item.uri

        //문자열을 R의 id 찾기
        val image = context.resources.getIdentifier(item.image, "drawable", context.packageName)
        Glide.with(context)
            .load(image)
            .centerCrop()
            .into(mainImage)
            .onLoadFailed(ContextCompat.getDrawable(context, R.drawable.img_no_image))
    }

    var seekProgress: Int
        get() = seekBar.progress
        set(progress) {
            seekBar.progress = progress
        }

    fun setMaxDuration(max: Int) {
        if (seekBar.max != max) {
            seekBar.max = max
        }
    }

    fun setImage(id: Int) {
        playBtn.setImageDrawable(ContextCompat.getDrawable(context, id))
    }

    interface OnHiddenViewClickListener {
        fun progress(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
        fun clickPlayButton(view: ImageButton, isPlay: Boolean, url: String?)
        fun clickCloseButton()
    }

    fun setHiddenViewClickListener(listener: OnHiddenViewClickListener) {
        this.hiddenViewListener = listener
    }
}
