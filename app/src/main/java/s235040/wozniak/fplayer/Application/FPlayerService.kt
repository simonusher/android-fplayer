package s235040.wozniak.fplayer.Application

import android.Manifest
import android.app.*
import android.content.Intent
import android.os.*
import s235040.wozniak.fplayer.Playback.MusicPlayer
import s235040.wozniak.fplayer.Utils.PermissionUtils
import android.support.v4.app.NotificationCompat
import android.support.v4.media.app.NotificationCompat.MediaStyle
import s235040.wozniak.fplayer.Activities.MainActivity
import android.os.Build
import s235040.wozniak.fplayer.Controllers.TrackUpdateListener
import s235040.wozniak.fplayer.Playback.Track
import s235040.wozniak.fplayer.R
import android.graphics.BitmapFactory


class FPlayerService: Service(), TrackUpdateListener {
    val INTENT_TYPE_UNDEFINED = -1
    val INTENT_TYPE_START_SERVICE = 0
    val INTENT_TYPE_PLAY_PAUSE_SONG = 1
    val INTENT_TYPE_PREVIOUS_SONG = 2
    val INTENT_TYPE_NEXT_SONG = 3
    val INTENT_TYPE_STOP_SERVICE = 4

    val INTENT_TYPE_NAME = "INTENT_TYPE"

    val player: MusicPlayer = MusicPlayer
    val binder: IBinder = LocalBinder()
    val CHANNEL_ID = "DEFAULT_CHANNEL_ID"
    val NOTIFICATION_ID = 123
    lateinit var notificationManager: NotificationManager
    lateinit var notificationBuilder: NotificationCompat.Builder
    lateinit var lastNotificationTrack: Track


    inner class LocalBinder : Binder() {
        internal val service: FPlayerService
            get() = this@FPlayerService
    }

    override fun onCreate() {
        acquirePermissions()
        initializeMusicPlayer()
        createNotificationChannel()
        startWithNotification()
        bindToMusicPlayer()
    }

    private fun startWithNotification() {
        val notification = createNotification(isIdle = true)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun bindToMusicPlayer() {
        MusicPlayer.addTrackUpdateListener(this)
    }

    private fun unbindFromMusicPlayer() {
        MusicPlayer.removeTrackUpdateListener(this)
    }

    private fun createNotification(track: Track? = null, isPlaying: Boolean? = null, isIdle: Boolean = false): Notification{
        val icon = if(isIdle){
            R.drawable.ic_play
        } else {
            if(isPlaying != null) {
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            } else {
                R.drawable.ic_play
            }
        }

        val btnTitle = if(isPlaying != null){
            if(isPlaying) getString(R.string.play) else getString(R.string.pause)
        } else {
            getString(R.string.play)
        }

        val trackTitle = if(isIdle){
            getString(R.string.noSongText)
        } else {
            track?.title ?: lastNotificationTrack.title
        }
        val trackAuthor = if(isIdle) {
            ""
        } else {
            track?.author ?: lastNotificationTrack.author
        }
        val (previousSongIntent, playPauseIntent, nextSongIntent) = createControlIntents()
        val activityIntent = createActivityIntent()
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        notificationBuilder
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_music)
                .addAction(R.drawable.ic_previous, getString(R.string.previous), previousSongIntent)
                .addAction(icon, btnTitle, playPauseIntent)
                .addAction(R.drawable.ic_next, getString(R.string.next), nextSongIntent)
                .setContentTitle(trackTitle)
                .setContentText(trackAuthor)
                .setStyle(MediaStyle().setShowActionsInCompactView(0, 1, 2))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(activityIntent)
                .setDeleteIntent(createDeleteIntent())

        if(track != null){
            if(track.albumArtPath != ""){
                val bmOptions = BitmapFactory.Options()
                val bitmap = BitmapFactory.decodeFile(track.albumArtPath, bmOptions)
                notificationBuilder.setLargeIcon(bitmap)
            }
        } else if(!isIdle && lastNotificationTrack != null){
            if(lastNotificationTrack.albumArtPath != ""){
                val bmOptions = BitmapFactory.Options()
                val bitmap = BitmapFactory.decodeFile(lastNotificationTrack.albumArtPath, bmOptions)
                notificationBuilder.setLargeIcon(bitmap)
            }
        }
        return notificationBuilder.build()
    }

    private fun updateNotification(isPlaying: Boolean){
        startForeground(NOTIFICATION_ID, createNotification(isPlaying = isPlaying))
        if(!isPlaying){
            stopForeground(false)
        }
    }

    private fun updateNotification(track: Track, isPlaying: Boolean){
        lastNotificationTrack = track
        startForeground(NOTIFICATION_ID, createNotification(track, isPlaying))
    }

    private fun postIdleNotification(){
        notificationManager.notify(NOTIFICATION_ID, createNotification(isIdle = true))
        stopForeground(false)
    }

    private fun createDeleteIntent(): PendingIntent{
        val intent = Intent(this, FPlayerService::class.java)
        intent.putExtra(INTENT_TYPE_NAME, INTENT_TYPE_STOP_SERVICE)
        val pendingIntent = PendingIntent.getService(this, 0,
                intent, 0)
        return pendingIntent
    }


    private fun createControlIntents(): Triple<PendingIntent, PendingIntent, PendingIntent> {
        val intentPlayPause = Intent(this, FPlayerService::class.java)
        intentPlayPause.putExtra(INTENT_TYPE_NAME, INTENT_TYPE_PLAY_PAUSE_SONG)

        val intentPreviousSong = Intent(this, FPlayerService::class.java)
        intentPreviousSong.putExtra(INTENT_TYPE_NAME, INTENT_TYPE_PREVIOUS_SONG)
        val intentNextSong = Intent(this, FPlayerService::class.java)
        intentNextSong.putExtra(INTENT_TYPE_NAME, INTENT_TYPE_NEXT_SONG)

        val pendingIntentPreviousSong =
                PendingIntent.getService(this, INTENT_TYPE_PREVIOUS_SONG, intentPreviousSong, 0)
        val pendingIntentPlayPause =
                PendingIntent.getService(this, INTENT_TYPE_PLAY_PAUSE_SONG, intentPlayPause, 0)
        val pendingIntentNextSong =
                PendingIntent.getService(this, INTENT_TYPE_NEXT_SONG, intentNextSong, 0)

        return Triple(pendingIntentPreviousSong, pendingIntentPlayPause, pendingIntentNextSong)
    }

    private fun createActivityIntent(): PendingIntent{
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)
        return pendingIntent
    }

    fun handlePlaySongButton(index: Int){
        player.handlePlaySongButton(index)
    }

    fun handlePlayButton() {
        player.handlePlayButton()
    }

    fun handleSeekbarChange(percentage: Int){
        player.jumpTo(percentage)
    }

    fun rewind() {
        player.rewind()
    }

    fun forward() {
        player.forward()
    }

    fun handleShuffleButton(){
        player.toggleRandomShuffle()
    }

    fun handleLoopingButton(){
        player.toggleLoopingType()
    }

    private fun initializeMusicPlayer() {
        if(!player.isInitialized){
            player.readTracks(this)
        }
    }



    private fun acquirePermissions(): Boolean {
        return PermissionUtils.acquirePermission(this, Manifest.permission.READ_EXTERNAL_STORAGE,
                PermissionUtils.CODE_READ_EXTERNAL_STORAGE)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val intentType = intent.getIntExtra(INTENT_TYPE_NAME, INTENT_TYPE_UNDEFINED)
        when(intentType){
            INTENT_TYPE_PLAY_PAUSE_SONG -> {
                handlePlayButton()
            }

            INTENT_TYPE_NEXT_SONG -> {
                handleNextSongButton()
            }

            INTENT_TYPE_PREVIOUS_SONG -> {
                handlePreviousSongButton()
            }

            INTENT_TYPE_STOP_SERVICE -> {
                this.stopSelf(NOTIFICATION_ID)
            }
        }
        return START_NOT_STICKY
    }

    private fun handlePreviousSongButton() {
        player.handlePreviousSongButton()
    }

    private fun handleNextSongButton() {
        player.handleNextSongButton()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        unbindFromMusicPlayer()
        player.stop()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun notifyCurrentlyPlayedSong(listIndex: Int, track: Track, isPlaying: Boolean) {
        updateNotification(track, isPlaying)
    }

    override fun notifySwitchedSong(oldListIndex: Int?, newListIndex: Int, newTrack: Track) {
        updateNotification(newTrack, true)
    }

    override fun notifyPaused(listIndex: Int, track: Track) {
        updateNotification(false)
    }

    override fun notifyResumed(listIndex: Int, track: Track) {
        updateNotification(true)
    }

    override fun notifyIdle(fromIndex: Int?) {
        postIdleNotification()
    }
}