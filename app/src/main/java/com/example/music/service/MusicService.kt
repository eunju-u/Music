package com.example.music.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MusicService : Service() {
    private val binder: AudioServiceBinder = AudioServiceBinder()

    //binder 객체를 반환해야한다.
    //서비스가 클라이언트와 통신할 떄 사용한다.
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        binder.initMediaSession(this) // MediaSession 초기화
    }

    override fun onDestroy() {
        super.onDestroy()
        binder.releaseMediaSession() // MediaSession 해제
    }
}
