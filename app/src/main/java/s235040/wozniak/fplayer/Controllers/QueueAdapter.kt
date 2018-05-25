package s235040.wozniak.fplayer.Controllers

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import s235040.wozniak.fplayer.Playback.Track
import s235040.wozniak.fplayer.R
import kotlinx.android.synthetic.main.item_track.view.*

/**
 * Created by Szymon on 14.05.2018.
 */
class QueueAdapter(trackList: List<Track>) : RecyclerView.Adapter<QueueAdapter.ViewHolder>() {
    private val tracks: List<Track> = trackList
    class ViewHolder(rowLayout: ConstraintLayout) : RecyclerView.ViewHolder(rowLayout) {
        val titleTextView: TextView = rowLayout.item_track_tv_song_title
        val authorTextView: TextView = rowLayout.item_track_tv_song_author
        val lengthTextView: TextView = rowLayout.item_track_tv_song_length
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueAdapter.ViewHolder {
        val rowLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_track_queue, parent, false) as ConstraintLayout
        return ViewHolder(rowLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = tracks[position]
        holder.titleTextView.text = track.title
        holder.authorTextView.text = track.author
        holder.lengthTextView.text = track.duration
    }

    override fun getItemCount() = tracks.size
}