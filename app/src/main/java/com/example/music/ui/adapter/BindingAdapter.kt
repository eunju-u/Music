package com.example.music.ui.adapter

import androidx.databinding.BindingAdapter
import com.example.music.Menu
import com.example.music.R
import com.google.android.material.bottomnavigation.BottomNavigationView

@BindingAdapter("selectedItemId")
fun setSelectedItemId(view: BottomNavigationView, menu: Menu) {
    val item = when (menu) {
        Menu.HOME -> R.id.menu_home
    }
    view.selectedItemId = item
}