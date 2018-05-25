package s235040.wozniak.fplayer.Playback

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import s235040.wozniak.fplayer.Controllers.TrackUpdateListener


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

    val myListeners: MutableList<TrackUpdateListener> = mutableListOf()
    val DEFAULT_FORWARD_TIME_SECONDS = 10
    val DEFAULT_REWIND_TIME_SECONDS = 3

    var isInitialized = false

    var randomShuffle: Boolean = false
    var loopingType: LoopingType = LoopingType.NO_LOOPING
    var playbackState: PlaybackState = PlaybackState.IDLE
    val trackList: MutableList<Track> = mutableListOf()
    var mediaPlayer: MediaPlayer = MediaPlayer()
    init {
        mediaPlayer.isLooping = false
    }


    fun toggleRandomShuffle(){
        randomShuffle = !randomShuffle
        when(playbackState){
            PlaybackState.PLAYING, PlaybackState.PAUSED -> {
                val track = TrackQueue.getCurrentTrack()
                TrackQueue.createPlaybackQueue(trackList.indexOf(track))
            }
        }
        Log.d("currentTrack", TrackQueue.getCurrentTrack().toString())
    }

    fun toggleLoopingType() {
        loopingType = when(loopingType){
            LoopingType.NO_LOOPING -> LoopingType.ALL_TRACKS
            LoopingType.ALL_TRACKS -> LoopingType.ONE_TRACK
            LoopingType.ONE_TRACK -> LoopingType.NO_LOOPING
        }
        if(loopingType == LoopingType.ONE_TRACK){
            val track = TrackQueue.getCurrentTrack()
            if (track != null){
                TrackQueue.createPlaybackQueue(trackList.indexOf(track))
            }
        }
        when(playbackState){
            PlaybackState.PLAYING, PlaybackState.PAUSED -> {
                val track = TrackQueue.getCurrentTrack()
                TrackQueue.createPlaybackQueue(trackList.indexOf(track))
            }
        }
        Log.d("currentTrack", TrackQueue.getCurrentTrack().toString())
    }

    fun handlePlaySongButton(index: Int) {
        val currentTrack = TrackQueue.getCurrentTrack()
        if(currentTrack != null){
            val songIndex = trackList.indexOf(currentTrack)
            if(songIndex == index){
                playbackState = if(playbackState == PlaybackState.PLAYING){
                    notifyAllPaused(songIndex)
                    mediaPlayer.pause()
                    PlaybackState.PAUSED
                }
                else {
                    notifyAllNowPlaying(songIndex, currentTrack)
                    mediaPlayer.start()
                    PlaybackState.PLAYING
                }
            }
            else{
                playTrack(index)
            }
        } else {
            playTrack(index)
        }
    }

    fun handlePlayButton() {
        when (playbackState) {
            PlaybackState.PLAYING -> {
                val track = TrackQueue.getCurrentTrack()
                if(track != null){
                    notifyAllPaused(trackList.indexOf(track))
                }
                mediaPlayer.pause()
                playbackState = PlaybackState.PAUSED
            }
            PlaybackState.PAUSED -> {
                val track = TrackQueue.getCurrentTrack()
                if(track != null){
                    notifyAllNowPlaying(trackList.indexOf(track), track)
                }
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
        TrackQueue.createPlaybackQueue(index)
        playNextSong()
    }

    fun playNextSong() {
        val previousTrack = TrackQueue.getCurrentTrack()
        if(previousTrack != null){
            notifyStopped(trackList.indexOf(previousTrack))
        }
        val newTrack = TrackQueue.getNextTrack()
        Log.d("track", newTrack.toString())
        playbackState = if(newTrack != null){
            notifyAllNowPlaying(trackList.indexOf(newTrack), newTrack)
            startMediaPlayer(newTrack)
            PlaybackState.PLAYING
        } else {
            releaseMediaPlayer()
            PlaybackState.IDLE
        }
    }

    fun playPreviousSong() {
        val previousTrack = TrackQueue.getCurrentTrack()
        if(previousTrack != null){
            notifyStopped(trackList.indexOf(previousTrack))
        }
        val newTrack = TrackQueue.getPreviousTrack()
        playbackState = if(newTrack != null){
            notifyAllNowPlaying(trackList.indexOf(newTrack), newTrack)
            startMediaPlayer(newTrack)
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

    fun addTrackUpdateListener(listener: TrackUpdateListener){
        myListeners.add(listener)
        if(playbackState == PlaybackState.PLAYING){
            val currentTrack = TrackQueue.getCurrentTrack()
            if(currentTrack != null){
                val index = trackList.indexOf(currentTrack)
                listener.notifyNowPlaying(index, currentTrack)
                Log.d("REMOVED", listener.toString())
            }
        }
    }

    fun getCurrentlyPlayedSongIndex(): Int {
        val track = TrackQueue.getCurrentTrack()
        if(track != null){
            val index = trackList.indexOf(track)
            return index
        }
        return -1
    }

    fun removeTrackUpdateListener(listener: TrackUpdateListener){
        myListeners.remove(listener)
        Log.d("REMOVED", listener.toString())
    }

    fun notifyAll(action: (TrackUpdateListener) -> Unit){
        myListeners.forEach(action)
    }

    fun notifyAllNowPlaying(listIndex: Int, track: Track){
        notifyAll{ listener -> listener.notifyNowPlaying(listIndex, track) }
    }

    fun notifyAllPaused(listIndex: Int){
        notifyAll { listener -> listener.notifyPaused(listIndex) }
    }

    fun notifyStopped(listIndex: Int){
        notifyAll { listener -> listener.notifyStopped(listIndex) }
    }

    //TRACK QUEUE OBJECT
    object TrackQueue {
        var playbackQueue: MutableList<Track> = mutableListOf()
        var iterator: ListIterator<Track> = playbackQueue.listIterator()
        private var currentTrack: Track? = null

        fun getCurrentTrack(): Track? {
            return currentTrack
        }

        fun getNextTrack(): Track? {
            currentTrack = if(iterator.hasNext()){
                iterator.next()
            }
            else{
                if(loopingType == LoopingType.ALL_TRACKS){
                    createIterator()
                    if(iterator.hasNext()){
                        iterator.next()
                    }
                    else{
                        null
                    }
                }
                else{
                    null
                }
            }
            return currentTrack
        }

        fun getPreviousTrack(): Track? {
            currentTrack = if(iterator.hasPrevious()){
                iterator.previous()
            } else {
                if(loopingType == LoopingType.ALL_TRACKS){
                    createIterator(playbackQueue.size)
                    iterator.previous()
                }
                else{
                    if(iterator.hasNext()){
                        iterator.next()
                    }
                    else{
                        null
                    }
                }
            }
            return currentTrack
        }

        fun createPlaybackQueue(startIndex: Int) {
            if(loopingType == LoopingType.ONE_TRACK){
                createOneTrackQueue(startIndex)
                createIterator()
            }
            else if(randomShuffle){
                shuffleQueue(startIndex)
            } else{
                createNormalQueue(startIndex)
            }
            Log.d("playbackQueue: ", playbackQueue.toString())
        }

        private fun createOneTrackQueue(startIndex: Int) {
            if(startIndex >=0 && startIndex < trackList.size){
                playbackQueue = mutableListOf(trackList[startIndex])
            } else if(!trackList.isEmpty()) {
                throw IllegalArgumentException("Incorrect start index")
            }
        }

        private fun createNormalQueue(startIndex: Int) {
            if(startIndex >=0 && startIndex < trackList.size){
                playbackQueue = trackList.toMutableList()
                createIterator(startIndex)
            } else if(!trackList.isEmpty()) {
                throw IllegalArgumentException("Incorrect start index")
            }
        }

        private fun shuffleQueue() {
            playbackQueue = trackList.shuffled() as MutableList<Track>
            createIterator()
        }

        private fun shuffleQueue(startIndex: Int) {
            if(startIndex >=0 && startIndex < trackList.size){
                val firstTrack = trackList[startIndex]
                val tempList = trackList.shuffled() as MutableList<Track>
                tempList.swap(0, tempList.indexOf(firstTrack))
                playbackQueue = tempList
                createIterator()
            } else if(!trackList.isEmpty()) {
                throw IllegalArgumentException("Incorrect start index")
            }
        }

        private fun createIterator(index: Int = 0) {
            iterator = playbackQueue.listIterator(index)
        }
    }
}