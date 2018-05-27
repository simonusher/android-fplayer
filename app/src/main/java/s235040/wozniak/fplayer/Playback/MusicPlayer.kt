package s235040.wozniak.fplayer.Playback

import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.os.ParcelFileDescriptor
import android.util.Log
import s235040.wozniak.fplayer.Controllers.TrackUpdateListener
import s235040.wozniak.fplayer.Utils.CyclicIterator
import s235040.wozniak.fplayer.Utils.MyIterator
import s235040.wozniak.fplayer.Utils.StandardIterator
import java.io.FileDescriptor
import android.provider.MediaStore




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

    var equalizerPreset: Short? = null
    var equalizer: Equalizer = Equalizer(0, mediaPlayer.audioSessionId)

    fun getSongTimes(): Pair<Int, Int> {
        return if(playbackState == PlaybackState.PLAYING || playbackState == PlaybackState.PAUSED){
            Pair(mediaPlayer.currentPosition, mediaPlayer.duration)
        } else {
            Pair(-1, -1)
        }
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
                    notifyAllPaused(songIndex, currentTrack)
                    mediaPlayer.pause()
                    PlaybackState.PAUSED
                }
                else {
                    notifyAllResumed(songIndex, currentTrack)
                    mediaPlayer.start()
                    PlaybackState.PLAYING
                }
            }
            else{
                startPlaybackFrom(index)
            }
        } else {
            startPlaybackFrom(index)
        }
    }

    private fun startPlaybackFrom(index: Int) {
        var currentSong: Track? = null
        if(playbackState == PlaybackState.PLAYING || playbackState == PlaybackState.PAUSED){
            currentSong = TrackQueue.getCurrentTrack()
        }
        TrackQueue.createPlaybackQueue(index)
        val firstTrack = TrackQueue.getCurrentTrack()
        val firstTrackIndex = trackList.indexOf(firstTrack)
        playbackState = if(firstTrack != null){
            Log.d("FIRST TRACK", firstTrack.toString())
            if(currentSong != null){
                val oldListIndex = trackList.indexOf(currentSong)
                notifyAllSwitchedSong(oldListIndex, firstTrackIndex, firstTrack)
            } else {
                notifyAllSwitchedSong(null, firstTrackIndex, firstTrack)
            }
            startMediaPlayer(firstTrack)
            PlaybackState.PLAYING
        } else {
            if (currentSong != null) {
                val currentSongIndex = trackList.indexOf(currentSong)
                notifyAllIdle(currentSongIndex)
            } else {
                notifyAllIdle()
            }
            releaseMediaPlayer()
            PlaybackState.IDLE
        }
    }

    fun handlePlayButton() {
        when (playbackState) {
            PlaybackState.PLAYING -> {
                val track = TrackQueue.getCurrentTrack()
                if(track != null){
                    val trackIndex = trackList.indexOf(track)
                    notifyAllPaused(trackIndex, track)
                }
                mediaPlayer.pause()
                playbackState = PlaybackState.PAUSED
            }
            PlaybackState.PAUSED -> {
                val track = TrackQueue.getCurrentTrack()
                if(track != null){
                    val trackIndex = trackList.indexOf(track)
                    notifyAllResumed(trackIndex, track)
                }
                mediaPlayer.start()
                playbackState = PlaybackState.PLAYING
            }
            PlaybackState.IDLE -> {
                startPlaybackFrom(0)
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

    fun playNextSong() {
        val currentTrack = TrackQueue.getCurrentTrack()
        val newTrack = TrackQueue.getNextTrack()
        Log.d("track", newTrack.toString())
        playbackState = if (newTrack != null) {
            val newTrackIndex = trackList.indexOf(newTrack)
            if (currentTrack != null) {
                val currentTrackIndex = trackList.indexOf(currentTrack)
                notifyAllSwitchedSong(currentTrackIndex, newTrackIndex, newTrack)
            } else {
                notifyAllSwitchedSong(null, newTrackIndex, newTrack)
            }
            startMediaPlayer(newTrack)
            PlaybackState.PLAYING
        } else {
            if(playbackState == PlaybackState.IDLE){
                startPlaybackFrom(0)
                PlaybackState.PLAYING
            } else{
                if (currentTrack != null) {
                    val currentTrackIndex = trackList.indexOf(currentTrack)
                    notifyAllIdle(currentTrackIndex)
                } else {
                    notifyAllIdle()
                }
                releaseMediaPlayer()
                PlaybackState.IDLE
            }

        }
    }

    fun playPreviousSong() {
        val previousTrack = TrackQueue.getCurrentTrack()
        val newTrack = TrackQueue.getPreviousTrack()
        playbackState = if(newTrack != null){
            val newTrackIndex = trackList.indexOf(newTrack)
            if(previousTrack != null){
                val previousTrackIndex = trackList.indexOf(previousTrack)
                notifyAllSwitchedSong(previousTrackIndex, newTrackIndex, newTrack)
            } else{
                notifyAllSwitchedSong(null, newTrackIndex, newTrack)
            }
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
        applyEqualizer()
    }

    private fun prepareMediaPlayer(track: Track) {
        releaseMediaPlayer()
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(track.path)
        mediaPlayer.setOnCompletionListener { playNextSong() }
        mediaPlayer.prepare()
    }

    fun updateEqualizer(presetIndex: Short){
        if(equalizerPreset != presetIndex){
            this.equalizerPreset = presetIndex
            applyEqualizer()
        }
    }

    private fun applyEqualizer() {
        equalizer.enabled = false
        equalizer.release()
        equalizer = Equalizer(0, mediaPlayer.audioSessionId)
        if (equalizerPreset != null) {
            equalizer.usePreset(equalizerPreset as Short)
        }

        equalizer.enabled = true
    }

    fun getEqualizerPresetIndex(): Short?{
        return equalizerPreset
    }

    fun getMediaPlayerSessionId(): Int? {
        return if(playbackState != PlaybackState.IDLE){
            mediaPlayer.audioSessionId
        } else {
            null
        }
    }

    fun readTracks(context: Context){
        val contentResolver = context.contentResolver
        val uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(uri, null, null, null, android.provider.MediaStore.Audio.Media.TITLE + " ASC")

        if (cursor == null) {
            println("QUERY FAILED")
        } else if (!cursor.moveToFirst()) {
            println("NO MEDIA ON THE DEVICE")
        } else {
            val titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
            val authorColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DURATION)
            val pathColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA)
            val albumIdColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM_ID)
            do {
                val thisTitle = cursor.getString(titleColumn)
                val thisAuthor = cursor.getString(authorColumn)
                val thisDuration = cursor.getInt(durationColumn)
                val songPath = cursor.getString(pathColumn)
                val albumId = cursor.getString(albumIdColumn)
                val projection = arrayOf(android.provider.MediaStore.Audio.Albums._ID, android.provider.MediaStore.Audio.Albums.ALBUM_ART)
                val selection = arrayOf(albumId.toString())
                val cursorAlbums = contentResolver.query(android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        projection,
                        android.provider.MediaStore.Audio.Albums._ID+ "=?",
                        selection,
                        null)
                var path = ""
                if (cursorAlbums.moveToFirst()) {
                    val columnIndex = cursorAlbums.getColumnIndex(android.provider.MediaStore.Audio.Albums.ALBUM_ART)
                    path = cursorAlbums.getString(columnIndex) ?: ""
                    Log.d("ALBUM ART PATH", path)
                }
                cursorAlbums.close()

                trackList += Track(songPath, thisTitle, thisAuthor, path, thisDuration)
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
            playbackState = PlaybackState.IDLE
            val index = getCurrentlyPlayedSongIndex()
            if(index != -1){
                notifyAllIdle(index)
            }
            else {
                notifyAllIdle()
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


    fun addTrackUpdateListener(listener: TrackUpdateListener){
        myListeners.add(listener)
        if(playbackState == PlaybackState.PLAYING || playbackState == PlaybackState.PAUSED){
            val currentTrack = TrackQueue.getCurrentTrack()
            if(currentTrack != null){
                val index = trackList.indexOf(currentTrack)
                if(playbackState == PlaybackState.PLAYING){
                    listener.notifyCurrentlyPlayedSong(index, currentTrack, true)
                } else {
                    listener.notifyCurrentlyPlayedSong(index, currentTrack, false)
                }
                Log.d("REMOVED", listener.toString())
            }
        } else {
            listener.notifyIdle()
        }
    }

    fun removeTrackUpdateListener(listener: TrackUpdateListener){
        myListeners.remove(listener)
        Log.d("REMOVED", listener.toString())
    }

    fun notifyAll(action: (TrackUpdateListener) -> Unit){
        myListeners.forEach(action)
    }



    fun notifyAllSwitchedSong(oldListIndex: Int? = null, newListIndex: Int, newTrack: Track){
        if(oldListIndex != null){
            notifyAll { listener -> listener.notifySwitchedSong(oldListIndex, newListIndex, newTrack) }
        } else {
            notifyAll { listener -> listener.notifySwitchedSong(null, newListIndex, newTrack) }
        }
    }


    fun notifyAllPaused(listIndex: Int, track: Track){
        notifyAll { listener -> listener.notifyPaused(listIndex, track) }
    }


    fun notifyAllResumed(listIndex: Int, track: Track){
        notifyAll { listener -> listener.notifyResumed(listIndex, track) }
    }

    fun notifyAllIdle(fromIndex: Int? = null){
        if(fromIndex != null){
            notifyAll { listener -> listener.notifyIdle(fromIndex) }
        } else {
            notifyAll { listener -> listener.notifyIdle() }
        }
    }

    fun jumpTo(percentage: Int) {
        if(playbackState == PlaybackState.PLAYING || playbackState == PlaybackState.PAUSED){
            val newPosition = (percentage * mediaPlayer.duration) / 100
            if(newPosition >= 0 && newPosition < mediaPlayer.duration){
                mediaPlayer.seekTo(newPosition)
            }
        }
    }

    fun handlePreviousSongButton() {
        if(playbackState == PlaybackState.IDLE){
            startPlaybackFrom(0)
        } else {
            playPreviousSong()
        }
    }

    fun handleNextSongButton() {
        if(playbackState == PlaybackState.IDLE){
            startPlaybackFrom(0)
        } else {
            playNextSong()
        }
    }

    //TRACK QUEUE OBJECT
    object TrackQueue {
        fun MutableList<Track>.cyclicIterator(): CyclicIterator<Track>{
            return CyclicIterator(this)
        }

        fun MutableList<Track>.cyclicIterator(startIndex: Int): CyclicIterator<Track>{
            return CyclicIterator(this, startIndex)
        }

        fun MutableList<Track>.standardIterator(): StandardIterator<Track>{
            return StandardIterator(this)
        }

        fun MutableList<Track>.standardIterator(startIndex: Int): StandardIterator<Track>{
            return StandardIterator(this, startIndex)
        }

        var playbackQueue: MutableList<Track> = mutableListOf()
        lateinit var iterator: MyIterator<Track>

        fun getCurrentTrack(): Track? {
            if(this::iterator.isInitialized){
                return iterator.current()
            }
            else return null
        }

        fun getNextTrack(): Track? {
            return if(iterator.hasNext()){
                iterator.next()
            }
            else{
                null
            }
        }

        fun getPreviousTrack(): Track? {
            return if(iterator.hasPrevious()){
                iterator.previous()
            } else {
                null
            }
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
                throw IllegalArgumentException("Incorrect start index $startIndex")
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
                throw IllegalArgumentException("Incorrect start index $startIndex")
            }
        }

        private fun createIterator(index: Int = 0) {
            iterator = when(loopingType){
                LoopingType.ALL_TRACKS, LoopingType.ONE_TRACK -> {
                    playbackQueue.cyclicIterator(index)
                }
                LoopingType.NO_LOOPING-> {
                    playbackQueue.standardIterator(index)
                }
            }
        }
    }
}