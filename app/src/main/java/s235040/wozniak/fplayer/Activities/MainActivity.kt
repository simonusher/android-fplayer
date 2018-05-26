package s235040.wozniak.fplayer.Activities

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import s235040.wozniak.fplayer.Controllers.TrackAdapter
import s235040.wozniak.fplayer.Playback.MusicPlayer
import s235040.wozniak.fplayer.R
import kotlinx.android.synthetic.main.activity_main.*
import s235040.wozniak.fplayer.Application.FPlayerService
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.SeekBar
import s235040.wozniak.fplayer.Controllers.TrackUpdateListener
import s235040.wozniak.fplayer.Playback.Track
import s235040.wozniak.fplayer.Utils.StringUtils


class MainActivity : Activity(), TrackUpdateListener, SeekBar.OnSeekBarChangeListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    val musicPlayer: MusicPlayer = MusicPlayer
    val serviceConnection: ServiceConnection = MyServiceConnection()
    var isConnectedToService = false
    lateinit var service: FPlayerService

    lateinit var handler: Handler

    var interfaceUpdateDelayMs: Long = 50

    var seekbarIsClicked = false

    val interfaceUpdateThread: Runnable = object : Runnable {
        override fun run() {
            updateInterface(musicPlayer.getSongTimes())
            handler.postDelayed(this, interfaceUpdateDelayMs)
        }
    }

    private fun updateInterface(songTimes: Pair<Int, Int>) {
        val currentPosionMilis = songTimes.first
        val songDurationMulis = songTimes.second
        if(currentPosionMilis != -1){
            activity_main_tv_current_song_position.text = StringUtils.getDurationStringFromMilis(currentPosionMilis)
            val currentPositionPercent: Int = (currentPosionMilis / songDurationMulis.toDouble() * 100).toInt()
            if(!seekbarIsClicked){
                activity_main_seekbar_song_position.progress = currentPositionPercent
            }
        } else {
            activity_main_tv_current_song_position.text = "00:00"
            if(!seekbarIsClicked){
                activity_main_seekbar_song_position.progress = 0
            }
        }
    }

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
        seekbarIsClicked = true
    }

    override fun onStopTrackingTouch(seekbar: SeekBar) {
        seekbarIsClicked = false
        service.handleSeekbarChange(seekbar.progress)
    }


    inner class MyServiceConnection: ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName) {
            bindToService()
            isConnectedToService = false
        }

        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            service = (binder as FPlayerService.LocalBinder).service
            initializeTrackList()
            bindToMusicPlayer()
            isConnectedToService = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeControlButtons()
        activity_main_seekbar_song_position.setOnSeekBarChangeListener(this)
        handler = Handler(Looper.getMainLooper())
    }

    private fun initializeControlButtons() {
        initializeLoopingButton()
        initializeShuffleButton()
    }

    private fun initializeLoopingButton() {
        setLoopingButtonIcon()
        acitivty_main_btn_repeat.setOnClickListener{_ ->
            service.handleLoopingButton()
            setLoopingButtonIcon()
        }
    }

    private fun initializeShuffleButton() {
        setShuffleButtonIcon()
        activity_main_btn_shuffle.setOnClickListener{_ ->
            service.handleShuffleButton()
            setShuffleButtonIcon()
        }
    }

    private fun setShuffleButtonIcon() {
        val randomShuffle: Boolean = musicPlayer.randomShuffle
        activity_main_btn_shuffle.setImageResource(when(randomShuffle){
            true -> {
                R.drawable.ic_shuffle_on
            }
            false -> {
                R.drawable.ic_shuffle_off
            }
        })
    }

    private fun setLoopingButtonIcon() {
        val loopingType: MusicPlayer.LoopingType = musicPlayer.loopingType
        acitivty_main_btn_repeat.setImageResource(when(loopingType){
            MusicPlayer.LoopingType.NO_LOOPING -> {
                R.drawable.ic_repeat_off
            }
            MusicPlayer.LoopingType.ONE_TRACK -> {
                R.drawable.ic_repeat_one_song
            }

            MusicPlayer.LoopingType.ALL_TRACKS -> {
                R.drawable.ic_repeat_on
            }
        })
    }

    override fun onResume() {
        super.onResume()
        bindToService()
        handler.removeCallbacks(interfaceUpdateThread)
        handler.postDelayed(interfaceUpdateThread, interfaceUpdateDelayMs)
    }

    private fun bindToMusicPlayer() {
        MusicPlayer.addTrackUpdateListener(this)
    }

    override fun onPause() {
        super.onPause()
        unbindFromService()
        unbindFromMusicPlayer()
        handler.removeCallbacks(interfaceUpdateThread)
    }

    private fun unbindFromMusicPlayer() {
        MusicPlayer.removeTrackUpdateListener(this)
    }

    private fun bindToService() {
        val intent = Intent(this, FPlayerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        isConnectedToService = true
    }

    private fun unbindFromService() {
        unbindService(serviceConnection)
        isConnectedToService = false
    }

    private fun initializeTrackList() {
        viewAdapter = TrackAdapter(musicPlayer.trackList, service::handlePlaySongButton)
        viewManager = LinearLayoutManager(this)
        recyclerView = findViewById<RecyclerView>(R.id.activity_main_rv_track_list).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        activity_main_btn_play.setOnClickListener{_ -> service.handlePlayButton()}
        activity_main_btn_rewind.setOnClickListener{_ -> service.rewind()}
        activity_main_btn_forward.setOnClickListener{_ -> service.forward()}
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId){
            R.id.menu_main_item_queue -> {
                showQueue()
                true
            }

            R.id.menu_main_item_settings -> {
                showSettings()
                true
            }
            R.id.menu_main_item_about_me -> {
                showAboutMe()
                true
            }
            else ->{
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun showQueue() {
        val queueIntent = Intent(this, QueueActivity::class.java)
        startActivity(queueIntent)
    }

    private fun showSettings() {
        val settingsIntent = Intent(this, SettingsActivity::class.java)
        startActivity(settingsIntent)
    }

    private fun showAboutMe() {
        val aboutMeIntent = Intent(this, AboutMeActivity::class.java)
        startActivity(aboutMeIntent)
    }

    private fun fillTrackInfo(track: Track) {
        activity_main_tv_song_title.text = track.title
        activity_main_tv_song_author.text = track.author
        activity_main_tv_song_length.text = track.duration
    }


    private fun clearTrackInfo() {
        activity_main_tv_song_title.text = ""
        activity_main_tv_song_author.text = ""
        activity_main_tv_song_length.text = getString(R.string.songLengthPlaceholder)
    }


    override fun notifyCurrentlyPlayedSong(listIndex: Int, track: Track, isPlaying: Boolean) {
        fillTrackInfo(track)
        viewAdapter.notifyItemChanged(listIndex, isPlaying)
        if(isPlaying){
            activity_main_btn_play.setImageResource(R.drawable.ic_pause)
        } else {
            activity_main_btn_play.setImageResource(R.drawable.ic_play)
        }
    }

    override fun notifySwitchedSong(oldListIndex: Int?, newListIndex: Int, newTrack: Track) {
        if(oldListIndex != null && oldListIndex != newListIndex){
            viewAdapter.notifyItemChanged(oldListIndex, false)
        }
        viewAdapter.notifyItemChanged(newListIndex, true)
        fillTrackInfo(newTrack)
        activity_main_btn_play.setImageResource(R.drawable.ic_pause)
    }

    override fun notifyPaused(listIndex: Int, track: Track) {
        viewAdapter.notifyItemChanged(listIndex, false)
        activity_main_btn_play.setImageResource(R.drawable.ic_play)
    }

    override fun notifyResumed(listIndex: Int, track: Track) {
        viewAdapter.notifyItemChanged(listIndex, true)
        activity_main_btn_play.setImageResource(R.drawable.ic_pause)
    }

    override fun notifyIdle(fromIndex: Int?) {
        if(fromIndex != null){
            viewAdapter.notifyItemChanged(fromIndex, false)
        }
        activity_main_btn_play.setImageResource(R.drawable.ic_play)
        clearTrackInfo()
    }
}
