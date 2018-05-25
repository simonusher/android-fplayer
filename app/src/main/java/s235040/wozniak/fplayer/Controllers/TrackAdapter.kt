package s235040.wozniak.fplayer.Controllers

import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import s235040.wozniak.fplayer.Playback.Track
import s235040.wozniak.fplayer.R
import kotlinx.android.synthetic.main.item_track.view.*
import s235040.wozniak.fplayer.Playback.MusicPlayer

/**
 * Created by Szymon on 14.05.2018.
 */
class TrackAdapter(trackList: List<Track>, val playCallback:(Int) -> Unit) : RecyclerView.Adapter<TrackAdapter.ViewHolder>() {
    private val tracks: List<Track> = trackList
    private val lastIndex = -1
    class ViewHolder(val rowLayout: ConstraintLayout) : RecyclerView.ViewHolder(rowLayout) {
        val titleTextView: TextView = rowLayout.item_track_tv_song_title
        val authorTextView: TextView = rowLayout.item_track_tv_song_author
        val lengthTextView: TextView = rowLayout.item_track_tv_song_length
        val playButton: ImageButton = rowLayout.item_track_btn_play
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackAdapter.ViewHolder {
        val rowLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_track, parent, false) as ConstraintLayout
        return ViewHolder(rowLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if(lastIndex >= 0 && lastIndex < tracks.size){
            notifyItemChanged(lastIndex, false)
        }
        if(!payloads.isEmpty()){
            val playing = payloads[0]
            if(playing is Boolean){
                Log.d("TAG", "onBindViewHolder $position $holder $playing")
                setItemBackgroundAndIcon(holder, playing)
            }
        }
        else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        val currentlyPlayedTrackIndex = MusicPlayer.getCurrentlyPlayedSongIndex()
        if(currentlyPlayedTrackIndex == holder.adapterPosition && MusicPlayer.isPlaying()){
            setItemBackgroundAndIcon(holder, true)
        } else {
            setItemBackgroundAndIcon(holder, false)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = tracks[position]
        holder.titleTextView.text = track.title
        holder.authorTextView.text = track.author
        holder.lengthTextView.text = track.duration
        holder.playButton.setOnClickListener { _ ->
            playCallback(position)
        }
    }

    fun setItemBackgroundAndIcon(holder: ViewHolder, isPlaying: Boolean) {
        if(isPlaying){
            holder.playButton.setImageResource(R.drawable.ic_pause)
        } else {
            holder.playButton.setImageResource(R.drawable.ic_play)
        }
    }

    override fun getItemCount() = tracks.size
}