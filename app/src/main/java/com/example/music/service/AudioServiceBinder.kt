package com.example.music.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.media.MediaPlayer.OnSeekCompleteListener
import android.os.Binder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.example.music.dto.MusicItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

//Activity와 연결을 위한 클래스
//바인딩을 통해 컴포넌트와 상호작용, 바인딩은 연관시키는 과정/구속/값이 정해진다.
class AudioServiceBinder : Binder(), OnPreparedListener, OnSeekCompleteListener {
    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> get() = _progress

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> get() = _duration

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying

    private var mediaPlayer: MediaPlayer? = null
    private var audioUrl: String? = null

    private var progressJob: Job? = null
    private lateinit var mediaSession: MediaSessionCompat

    private var musicTitle = ""
    private var musicSinger = ""

    private fun initMediaPlayer() {
        _isPlaying.value = false

        if (this.mediaPlayer == null) {
            //초기화 해준다.
            mediaPlayer = MediaPlayer().apply {
                setOnPreparedListener(this@AudioServiceBinder)
                setAudioAttributes(
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(
                        AudioAttributes.CONTENT_TYPE_MUSIC
                    ).build()
                )
            }
        }
    }

    fun setMediaPlayer(context: Context?, item: MusicItem) {
        musicTitle = item.title ?: ""
        musicSinger = item.singer ?: ""
        val url = item.uri ?: ""

        Log.d(TAG, "setMediaPlayer : $url")

        Log.i(TAG, "URL : $url")
        if (url.trim { it <= ' ' }.isNotEmpty()) {
            if (this.audioUrl == null) { //처음에 url은 없는 상태 첫 재생시 컨트롤 위해 따로 빼줌
                //첫 재생시
                this.audioUrl = url //현재 받은 url이 된다.
                initMediaPlayer()
                //곡이 실행되고 있지 않은 상태
                startMusic(context, url)
            } else {
                //이전 곡에 대한 정보가 있을 시 (연속적으로 재생을 실행시키기 위해)
                if (this.audioUrl == url) {
                    //현재 재생 중인 곡 지속 재생
                    startMusic()

                } else {
                    //현재 재생 중인 곡 중지, 새로운 곡 재생
                    destroyMusic()
                    this.audioUrl = url
                    initMediaPlayer()
                    startMusic(context, url)

                }
            }
        }
    }

    //새로(처음) 재생 되는 곡 start
    private fun startMusic(context: Context?, url: String) {
        Log.d(TAG, "startMusic : $url")
        try {
            if (url.startsWith("asset:///")) {
                // "asset:///" 부분을 제외한 파일 이름 추출
                val fileName = url.removePrefix("asset:///")
                context?.run {
                    val afd = this.assets.openFd(fileName)

                    mediaPlayer?.apply {
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        prepareAsync() //비동기화로 정보를 받아서 제공함
                    }
                }

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //재생 곡 pause 후 다시 재생 할 경우 플레이어 start
    private fun startMusic() {
        _isPlaying.value = true
        updateProgress()
        mediaPlayer?.start()
    }

    fun stopMusic() {
        mediaPlayer?.apply {
            stop()
            destroyMusic()
        }
    }

    fun pauseMusic() {
        Log.d(TAG, "pauseMusic")
        mediaPlayer?.pause()
    }

    private fun startAndPause() {
        if (isPlaying.value) {
            pauseMusic()
        } else {
            startMusic()
        }
    }

    private fun destroyMusic() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
            mediaPlayer = null
        }
    }

    override fun onPrepared(mp: MediaPlayer) {
        Log.d(TAG, "isPrepared Ok")
        _isPlaying.value = true
        mp.start()
        updateProgress()
        setMediaMetadata()
    }

    private fun updateProgress() {
        progressJob?.cancel()
        progressJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                mediaPlayer?.let { mp ->
                    withContext(Dispatchers.Main) {
                        _progress.value =
                            if (mp.duration > 0 || mp.isPlaying) mp.currentPosition else 0
                        _duration.value = mp.duration
                        _isPlaying.value = mp.isPlaying
                        updatePlaybackState()
                    }
                }
                delay(1000)
            }
        }
    }

    fun seekTo(position: Int) {
        mediaPlayer?.apply {
            seekTo(position)
        }
    }

    override fun onSeekComplete(mp: MediaPlayer) {
        _isPlaying.value = false
    }

    // 미디어 세션 초기화
    fun initMediaSession(context: Context) {
        mediaSession = MediaSessionCompat(context, "MusicService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    super.onPlay()
                    startAndPause()
                }

                override fun onPause() {
                    super.onPause()
                    startAndPause()
                }

                override fun onStop() {
                    super.onStop()
                    stopMusic()
                }

                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    seekTo(pos.toInt())
                }
            })
            isActive = true
        }
        updatePlaybackState()
    }

    // 미디어 세션 데이터 설정
    private fun setMediaMetadata() {
        val mediaMetadata: MediaMetadataCompat.Builder = MediaMetadataCompat.Builder()

        mediaMetadata.apply {
            putString(MediaMetadataCompat.METADATA_KEY_TITLE, musicTitle) // 제목 이름
            putString(MediaMetadataCompat.METADATA_KEY_ARTIST, musicSinger) // 가수 이름
            putLong(MediaMetadata.METADATA_KEY_DURATION, mediaPlayer?.duration?.toLong() ?: 0) // 재생 바 노출
        }

        mediaSession.setMetadata(mediaMetadata.build())
    }

    // 미디어 세션 재생 상태 위치 설정
    private fun updatePlaybackState() {
        val state = if (mediaPlayer?.isPlaying == true) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(state, mediaPlayer?.currentPosition?.toLong() ?: 0, 1.0f)  // 재생 상태와 위치 설정
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_SEEK_TO
                ) // 재생, 일시정지, 위치 이동 동작
                .build()
        )
    }

    fun releaseMediaSession() {
        mediaSession.isActive = false
        mediaSession.release()
    }

    companion object {
        private val TAG: String = "music play ${AudioServiceBinder::class.java.simpleName}"
    }
}


