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
import android.os.IBinder
import s235040.wozniak.fplayer.Controllers.TrackUpdateListener
import s235040.wozniak.fplayer.Playback.Track


class MainActivity : Activity(), TrackUpdateListener{
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    val musicPlayer: MusicPlayer = MusicPlayer
    val serviceConnection: ServiceConnection = MyServiceConnection()
    var isConnectedToService = false
    lateinit var service: FPlayerService

    inner class MyServiceConnection: ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName) {
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
    }

    private fun bindToMusicPlayer() {
        MusicPlayer.addTrackUpdateListener(this)
    }

    override fun onPause() {
        super.onPause()
        unbindFromService()
        unbindFromMusicPlayer()
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

    override fun notifyNowPlaying(listIndex: Int, track: Track) {
        viewAdapter.notifyItemChanged(listIndex, true)
        activity_main_btn_play.setImageResource(R.drawable.ic_pause)
        fillTrackInfo(track)
    }

    private fun fillTrackInfo(track: Track) {
        activity_main_tv_song_title.text = track.title
        activity_main_tv_song_author.text = track.author
        activity_main_tv_song_length.text = track.duration
    }

    override fun notifyPaused(listIndex: Int) {
        viewAdapter.notifyItemChanged(listIndex, false)
        activity_main_btn_play.setImageResource(R.drawable.ic_play)
    }

    override fun notifyPlaying(listIndex: Int) {
        viewAdapter.notifyItemChanged(listIndex, true)
        activity_main_btn_play.setImageResource(R.drawable.ic_pause)
    }

    override fun notifyStopped(listIndex: Int) {
        viewAdapter.notifyItemChanged(listIndex, false)
        clearTrackInfo()
        activity_main_btn_play.setImageResource(R.drawable.ic_play)
    }

    private fun clearTrackInfo() {
        activity_main_tv_song_title.text = ""
        activity_main_tv_song_author.text = ""
        activity_main_tv_song_length.text = ""
    }
}