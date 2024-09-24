package com.example.music.ui.views

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
    private lateinit var item: MusicItem

    private var closeClickListener: (() -> Unit)? = null
    private var progressListener: ((Int, Boolean) -> Unit)? = null
    private var playClickListener: ((ImageButton?, Boolean, MusicItem) -> Unit)? = null

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
                progressListener?.invoke(progress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
        downBtn = view.findViewById(R.id.hidden_panel_down_btn)
        downBtn.setOnClickListener {
            closeClickListener?.invoke()
        }
        playBtn = view.findViewById(R.id.hidden_panel_play_btn)
        playBtn.setOnClickListener {
            isPlay = !isPlay
            playClickListener?.invoke(playBtn, isPlay, item)
        }
        addView(view)
    }

    fun setViewDate(item: MusicItem) {
        title.text = item.title
        singer.text = item.singer
        this.item = item

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

    //닫기 버튼 Listener
    fun setCloseClickListener(listener: (() -> Unit)?) {
        this.closeClickListener = listener
    }

    //progress update Listener
    fun setProgressListener(listener: ((Int, Boolean) -> Unit)?) {
        this.progressListener = listener
    }

    //재생 버튼 Listener
    fun setOnPlayButtonClickListener(listener: ((ImageButton?, Boolean, MusicItem) -> Unit)?) {
        this.playClickListener = listener
    }
}
