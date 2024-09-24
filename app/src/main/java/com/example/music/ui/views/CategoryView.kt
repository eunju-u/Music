package com.example.music.ui.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.dto.MusicItem
import com.example.music.ui.adapter.CategoryAdapter

class CategoryView(private val context: Context) : LinearLayout(context) {
    private lateinit var categoryTitle: TextView
    private lateinit var categoryDescription: TextView
    private lateinit var recycleListView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private var listener: OnClickListItemListener? = null

    fun setOnClickListItemLister(listener: OnClickListItemListener?) {
        this.listener = listener
    }

    interface OnClickListItemListener {
        fun onItemSelected(view: View?, item: MusicItem?)
    }

    init {
        setView(context)
    }

    private fun setView(context: Context) {
        val view: View = LayoutInflater.from(context).inflate(
            R.layout.home_category_view,
            this, false
        )
        categoryTitle = view.findViewById(R.id.category_title)
        categoryDescription = view.findViewById(R.id.category_description)

        recycleListView = view.findViewById(R.id.category_list)
        recycleListView.setLayoutManager(
            LinearLayoutManager(
                context,
                RecyclerView.HORIZONTAL,
                false
            )
        )
        //false 는 고정값이다. 리스트가 고정값이고 자제 메모리에 고정값을 준다. 고정값아니면 속도가 느리다.
        recycleListView.setHasFixedSize(true)

        this.addView(view)
    }

    fun setViewDate(title: String?, description: String?, list: List<MusicItem>?) {
        categoryTitle.text = title
        categoryDescription.text = description
        adapter = CategoryAdapter(list, context)
        adapter.setOnClickListener { view, item ->
            if (listener != null) {
                listener!!.onItemSelected(view, item)
            }
        }
        recycleListView.setAdapter(adapter)
    }
}
