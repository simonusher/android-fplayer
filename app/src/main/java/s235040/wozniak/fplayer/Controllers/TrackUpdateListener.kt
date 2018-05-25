package s235040.wozniak.fplayer.Controllers

import s235040.wozniak.fplayer.Playback.Track

interface TrackUpdateListener {
    fun notifyNowPlaying(listIndex: Int, track: Track)
    fun notifyPaused(listIndex: Int)
    fun notifyPlaying(listIndex: Int)
    fun notifyStopped(listIndex: Int)
}