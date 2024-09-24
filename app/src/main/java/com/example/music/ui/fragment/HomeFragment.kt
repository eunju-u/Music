package com.example.music.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.music.databinding.FragmentHomeBinding
import com.example.music.ui.base.BaseFragment
import com.example.music.ui.views.CategoryView
import com.example.music.R
import com.example.music.dto.MusicItem
import com.example.music.ui.MainViewModel
import kotlinx.coroutines.launch

/** 홈 화면 **/
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    private var mainParent: LinearLayout? = null
    private var listener: ((View?, MusicItem?) -> Unit)? = null

    override fun getLayout(): Int {
        return R.layout.fragment_home
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.listFlow.collect { dataList ->
                context?.run {
                    for (item in dataList) {
                        val categoryView = CategoryView(this)
                        categoryView.setOnClickListItemLister(object :
                            CategoryView.OnClickListItemListener {
                            override fun onItemSelected(view: View?, item: MusicItem?) {
                                listener?.invoke(view, item)
                            }
                        })
                        categoryView.setViewDate(item.category, item.description, item.items)
                        mainParent?.addView(categoryView)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        mainParent = view.findViewById(R.id.home_parent_view)
        return view
    }

    fun setOnItemClickListener(listener: ((View?, MusicItem?) -> Unit)?) {
        this.listener = listener
    }

    companion object {
        private val TAG: String = "music play ${HomeFragment::class.java.simpleName}"
    }
}