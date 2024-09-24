package com.example.music

import android.app.Application
import android.content.Context

class MusicApplication : Application() {
    companion object {
        lateinit var instance: MusicApplication

        fun context() : Context {
            return instance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}