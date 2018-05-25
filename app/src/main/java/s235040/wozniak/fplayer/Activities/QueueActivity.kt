package s235040.wozniak.fplayer.Activities

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import s235040.wozniak.fplayer.Controllers.QueueAdapter
import s235040.wozniak.fplayer.Playback.MusicPlayer
import s235040.wozniak.fplayer.R

class QueueActivity : Activity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue)
        initQueue()
    }

    private fun initQueue() {
        viewAdapter = QueueAdapter(MusicPlayer.TrackQueue.playbackQueue)
        viewManager = LinearLayoutManager(this)
        recyclerView = findViewById<RecyclerView>(R.id.activity_queue_rv_queue).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }
}
