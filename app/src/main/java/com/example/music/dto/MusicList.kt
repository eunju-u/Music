package com.example.music.dto

data class MusicList(
    var category: String? = "",
    var description: String? = "",
    var items: List<MusicItem>? = emptyList()
)