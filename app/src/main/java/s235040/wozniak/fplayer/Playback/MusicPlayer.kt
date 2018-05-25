package s235040.wozniak.fplayer.Playback

import android.content.Context
import android.media.MediaPlayer
import android.util.Log


/**
 * Created by Szymon on 24.05.2018.
 */
object MusicPlayer {
    fun MutableList<Track>.swap(index1: Int, index2: Int) {
        if(index1 != index2){
            val tmp = this[index1]
            this[index1] = this[index2]
            this[index2] = tmp
        }
    }
    enum class PlaybackState {
        IDLE,
        PLAYING,
        PAUSED
    }
    enum class LoopingType {
        NO_LOOPING,
        ONE_TRACK,
        ALL_TRACKS
    }

    val DEFAULT_FORWARD_TIME_SECONDS = 10
    val DEFAULT_REWIND_TIME_SECONDS = 3

    var isInitialized = false

    var randomShuffle: Boolean = false
    var loopingType: LoopingType = LoopingType.NO_LOOPING
    var playbackState: PlaybackState = PlaybackState.IDLE
    val trackList: MutableList<Track> = mutableListOf()
    var mediaPlayer: MediaPlayer = MediaPlayer()
    val queue = TrackQueue()
    init {
        mediaPlayer.isLooping = false
    }


    fun toggleRandomShuffle(){
        randomShuffle = !randomShuffle
        when(playbackState){
            PlaybackState.PLAYING, PlaybackState.PAUSED -> {
                val track = queue.getCurrentTrack()
                queue.createPlaybackQueue(trackList.indexOf(track), false)
            }
        }
    }

    fun toggleLoopingType() {
        loopingType = when(loopingType){
            LoopingType.NO_LOOPING -> LoopingType.ALL_TRACKS
            LoopingType.ALL_TRACKS -> LoopingType.ONE_TRACK
            LoopingType.ONE_TRACK -> LoopingType.NO_LOOPING
        }
        when(playbackState){
            PlaybackState.PLAYING, PlaybackState.PAUSED -> {
                val track = queue.getCurrentTrack()
                queue.createPlaybackQueue(trackList.indexOf(track), false)
            }
        }
    }

    fun handlePlayButton() {
        when(playbackState){
            PlaybackState.PLAYING -> {
                mediaPlayer.pause()
                playbackState = PlaybackState.PAUSED
            }
            PlaybackState.PAUSED -> {
                mediaPlayer.start()
                playbackState = PlaybackState.PLAYING
            }
            PlaybackState.IDLE -> {
                playTrack(0)
            }
        }
    }

    fun forward() {
        forward(DEFAULT_FORWARD_TIME_SECONDS)
    }

    fun forward(seconds: Int) {
        if(playbackState == PlaybackState.PLAYING || playbackState == PlaybackState.PAUSED){
            val newPosition = mediaPlayer.currentPosition + seconds * 1000
            if(newPosition > mediaPlayer.duration){
                playNextSong()
            }
            else {
                mediaPlayer.seekTo(newPosition)
            }
        }
    }

    fun rewind() {
        rewind(DEFAULT_REWIND_TIME_SECONDS)
    }

    fun rewind(seconds: Int) {
        if(playbackState == PlaybackState.PLAYING || playbackState == PlaybackState.PAUSED){
            val newPosition = mediaPlayer.currentPosition - seconds * 1000
            if(newPosition < 0){
                playPreviousSong()
            }
            else {
                mediaPlayer.seekTo(newPosition)
            }
        }
    }


    fun playTrack(index: Int) {
        queue.createPlaybackQueue(index)
        playNextSong()
    }

    fun playNextSong() {
        val track = queue.getNextTrack()
        Log.d("track", track.toString())
        playbackState = if(track != null){
            startMediaPlayer(track)
            PlaybackState.PLAYING
        } else {
            releaseMediaPlayer()
            PlaybackState.IDLE
        }
    }

    fun playPreviousSong() {
        val track = queue.getPreviousTrack()
        playbackState = if(track != null){
            startMediaPlayer(track)
            PlaybackState.PLAYING
        } else {
            releaseMediaPlayer()
            PlaybackState.IDLE
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer.release()
    }

    private fun startMediaPlayer(track: Track) {
        prepareMediaPlayer(track)
        mediaPlayer.start()
    }

    private fun prepareMediaPlayer(track: Track) {
        releaseMediaPlayer()
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(track.path)
        mediaPlayer.setOnCompletionListener { playNextSong() }
        mediaPlayer.prepare()
    }

    fun readTracks(context: Context){
        val contentResolver = context.contentResolver
        val uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor == null) {
            println("QUERY FAILED")
        } else if (!cursor.moveToFirst()) {
            println("NO MEDIA ON THE DEVICE")
        } else {
            val titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
            val authorColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DURATION)
            val pathColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA)
            do {
                val thisTitle = cursor.getString(titleColumn)
                val thisAuthor = cursor.getString(authorColumn)
                val thisDuration = cursor.getInt(durationColumn)
                val songPath = cursor.getString(pathColumn)
                trackList += Track(songPath, thisTitle, thisAuthor, thisDuration)
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    fun isPlaying(): Boolean {
        return this.playbackState == PlaybackState.PLAYING
    }

    fun stop() {
        if(isPlaying()){
            mediaPlayer.stop()
            releaseMediaPlayer()
        }
    }

    //TRACK QUEUE OBJECT
    class TrackQueue {
        val INVALID_INDEX = -2
        var playbackQueue: MutableList<Track> = mutableListOf()
        var currentlyPlayedTrackIndex = -1

        fun getCurrentTrack(): Track? {
            return if(currentlyPlayedTrackIndex != INVALID_INDEX && !isEmpty()){
                playbackQueue[currentlyPlayedTrackIndex]
            } else{
                null
            }
        }

        fun getNextTrack(): Track? {
            if(currentlyPlayedTrackIndex == INVALID_INDEX){
                currentlyPlayedTrackIndex = -1
            }
            when(loopingType){
                LoopingType.ALL_TRACKS, LoopingType.NO_LOOPING -> {
                    currentlyPlayedTrackIndex += 1
                    if(currentlyPlayedTrackIndex >= playbackQueue.size){
                        currentlyPlayedTrackIndex = if(loopingType == LoopingType.ALL_TRACKS){
                            0
                        } else {
                            INVALID_INDEX
                        }
                    }
                }
            }
            return if(currentlyPlayedTrackIndex != INVALID_INDEX && !isEmpty())
                playbackQueue[currentlyPlayedTrackIndex] else null
        }

        fun getPreviousTrack(): Track? {
            when(loopingType){
                LoopingType.ALL_TRACKS -> {
                    currentlyPlayedTrackIndex -= 1
                    if (currentlyPlayedTrackIndex < 0){
                        currentlyPlayedTrackIndex = playbackQueue.size - 1
                    }
                }
                LoopingType.NO_LOOPING -> {
                    currentlyPlayedTrackIndex -= 1
                    if (currentlyPlayedTrackIndex < 0){
                        currentlyPlayedTrackIndex = 0
                    }
                }
            }
            return if(!isEmpty()) playbackQueue[currentlyPlayedTrackIndex] else null
        }

        fun createPlaybackQueue(startIndex: Int, resetIndex: Boolean = true) {
            if(randomShuffle){
                shuffleQueue(startIndex)
                if(resetIndex){
                    resetTrackIndex()
                }
            } else{
                createNormalQueue(startIndex, resetIndex)
            }
            Log.d("playbackQueue: ", playbackQueue.toString())
        }

        private fun createNormalQueue(startIndex: Int, resetIndex: Boolean = true) {
            if(startIndex >=0 && startIndex < trackList.size){
                playbackQueue = trackList.toMutableList()
                if(resetIndex){
                    currentlyPlayedTrackIndex = startIndex -1
                }
            } else {
                throw IllegalArgumentException("Incorrect start index")
            }
        }

        private fun resetTrackIndex() {
            currentlyPlayedTrackIndex = -1
        }

        private fun shuffleQueue() {
            playbackQueue = trackList.shuffled() as MutableList<Track>
        }

        private fun shuffleQueue(startIndex: Int) {
            if(startIndex >=0 && startIndex < trackList.size){
                val firstTrack = trackList[startIndex]
                val tempList = trackList.shuffled() as MutableList<Track>
                tempList.swap(0, tempList.indexOf(firstTrack))
                playbackQueue = tempList
            } else {
                throw IllegalArgumentException("Incorrect start index")
            }
        }

        fun isEmpty(): Boolean = playbackQueue.isEmpty()
    }
}