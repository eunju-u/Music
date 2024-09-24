package com.example.music.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.Menu
import com.example.music.dto.MusicList
import com.example.music.utils.JsonUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val application: Application) : AndroidViewModel(application) {

    lateinit var nowTab: Menu

    private val _listFlow = MutableStateFlow<List<MusicList>>(emptyList())
    val listFlow: StateFlow<List<MusicList>> get() = _listFlow

    fun homeLoadData() = viewModelScope.launch {
        val context = application.applicationContext
        val list: List<MusicList> = JsonUtils.readJsonArray(context)
        _listFlow.value = list
    }
}