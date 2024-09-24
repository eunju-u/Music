package com.example.music.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.lifecycle.lifecycleScope
import com.example.music.Menu
import com.example.music.R
import com.example.music.databinding.ActivityMainBinding
import com.example.music.ui.base.BaseActivity
import com.example.music.ui.base.BaseFragment
import com.example.music.ui.fragment.HomeFragment
import com.example.music.service.AudioServiceBinder
import com.example.music.service.MusicService
import com.example.music.ui.views.StickyPanelView
import com.example.music.ui.views.HiddenPanelView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    private val homeFragment by lazy { HomeFragment() }
    private val tabView: BottomNavigationView by lazy { findViewById(R.id.main_tabs) }

    private val upPanelLayout: SlidingUpPanelLayout by lazy { findViewById(R.id.main_up_panel) }
    private val panelLayout: RelativeLayout by lazy { findViewById(R.id.main_panel_layout) }
    private val stickyPanelView: StickyPanelView by lazy { StickyPanelView(applicationContext) } //아래 다운에 노출되는 뷰
    private val hiddenPanelView: HiddenPanelView by lazy { HiddenPanelView(applicationContext) } //위로 뜰때 보여지는 뷰

    var binder: AudioServiceBinder? = null
    private var isMusicPlay = false

    //connection할 때 binder를 넘겨준다.
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "service connected")
            binder = service as AudioServiceBinder
            initServiceCoroutine()
        }

        override fun onServiceDisconnected(name: ComponentName) {
        }
    }

    override fun getLayoutRes(): Int {
        return R.layout.activity_main
    }

    override fun getVmClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onStart() {
        super.onStart()
        //select 되는 메뉴를 고른다. Home 으로만 셋팅해도됨.
        vm.nowTab = Menu.HOME
    }

    private fun init() {
        Log.d(TAG, "init")
        setEvent()

        panelLayout.addView(stickyPanelView)
        panelLayout.addView(hiddenPanelView)

        //정보가 없을때는 안 보인다. 아이템 클릭했을 때 다시 넣겠다.
        upPanelLayout.setPanelState(PanelState.HIDDEN)

        bindMusicService()
        vm.homeLoadData()
    }

    private fun setEvent() {
        binding.mainTabs.setOnItemSelectedListener { menuItem ->
            var result = true
            val transactionFragment: BaseFragment<*>? = when (menuItem.itemId) {
                R.id.menu_home -> homeFragment
                else -> null
            }
            if (transactionFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frame, transactionFragment).commit()
            } else {
                result = false
            }
            result
        }

        //listener 대신 가독성 높고 직관적인 람다로 처리하기
        homeFragment.setOnItemClickListener { view, item ->
            item?.run {
                stickyPanelView.setViewData(item)
                hiddenPanelView.setViewDate(item)
                upPanelLayout.panelState = PanelState.COLLAPSED

                if (isMusicPlay) {
                    startMusic(item.uri)
                }
            }
        }

        stickyPanelView.setOnPlayButtonClickListener { _, isPlay, audiourl ->
            isMusicPlay = isPlay
            setPlayingImage(isPlay)
            if (isPlay) {
                startMusic(audiourl)
            } else {
                binder?.pauseMusic()
            }
        }

        upPanelLayout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            //slideOffset는 뷰에는 알파값을 조정해서 투명도랑 보이게 하는걸 조절한다.
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                hiddenPanelView.alpha = slideOffset
                stickyPanelView.setAlpha(1 - slideOffset)
                tabView.setAlpha(1 - slideOffset)
            }

            override fun onPanelStateChanged(
                panel: View,
                previousState: PanelState,
                newState: PanelState
            ) {
                if (newState == PanelState.EXPANDED) { //다 펼쳐졌다면 탭뷰 사라진다.
                    tabView.visibility = View.GONE
                } else if (newState == PanelState.COLLAPSED) { // 아래로 내려갔다면
                    tabView.visibility = View.VISIBLE
                }
            }
        })

        hiddenPanelView.setCloseClickListener {
            upPanelLayout.panelState = PanelState.COLLAPSED
        }
        hiddenPanelView.setProgressListener { progress, fromUser ->
            if (fromUser) {
                /*현재 프로그래스를 넘겨준다. */
                binder?.seekTo(progress)
            }
        }
        hiddenPanelView.setOnPlayButtonClickListener { _, isPlay, audiourl ->
            isMusicPlay = isPlay
            setPlayingImage(isPlay)
            if (isPlay) {
                startMusic(audiourl)
            } else {
                binder?.pauseMusic()
            }
        }
    }

    private fun startMusic(url: String?) {
        if (url.isNullOrEmpty()) return
        //service 실행
        binder?.setMediaPlayer(applicationContext, url) //자동적으로 실행해준다.
    }

    //bind 연결 후 호출
    private fun initServiceCoroutine() {
        lifecycleScope.launch {
            launch {
                binder?.progress?.collect { progress ->
                    /*한칸 한칸 움직일때 현재 위치 거리*/
                    hiddenPanelView.seekProgress = progress
                }
            }
            launch {
                binder?.duration?.collect { duration ->
                    //setMaxDuration 시크바의 전체 듀레이션 처음부터 끝 까지 int값으로 max라고 하면 처음부터 끝까지
                    hiddenPanelView.setMaxDuration(duration)
                }
            }
            launch {
                binder?.isPlaying?.collect { isPlaying ->
                    setPlayingImage(isPlaying)
                }
            }
        }
    }

    private fun setPlayingImage(isPlaying: Boolean) {
        if (isPlaying) {
            val pauseImgId = R.drawable.ic_pause
            stickyPanelView.setImage(pauseImgId)
            hiddenPanelView.setImage(pauseImgId)
        } else {
            val playImgId = R.drawable.ic_play_arrow
            stickyPanelView.setImage(playImgId)
            hiddenPanelView.setImage(playImgId)
        }
    }


    private fun bindMusicService() {
        val intent = Intent(this@MainActivity, MusicService::class.java)
        bindService(intent, connection, BIND_AUTO_CREATE)
    }


    private fun unBindMusicService() {
        binder?.run {
            unbindService(connection)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unBindMusicService()
    }
}