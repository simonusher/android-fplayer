package s235040.wozniak.fplayer.Controllers

import s235040.wozniak.fplayer.Playback.Track

interface TrackUpdateListener {
    fun notifyCurrentlyPlayedSong(listIndex: Int, track: Track, isPlaying: Boolean)
    fun notifySwitchedSong(oldListIndex: Int?, newListIndex: Int, newTrack: Track)
    fun notifyPaused(listIndex: Int, track: Track)
    fun notifyResumed(listIndex: Int, track: Track)
    fun notifyIdle(fromIndex: Int? = null)
}