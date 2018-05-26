package s235040.wozniak.fplayer.Application

import android.Manifest
import android.app.Service
import android.content.Intent
import android.os.*
import android.widget.Toast
import s235040.wozniak.fplayer.Playback.MusicPlayer
import s235040.wozniak.fplayer.Utils.PermissionUtils
import android.support.v4.app.NotificationCompat
import android.support.v4.media.app.NotificationCompat.MediaStyle
import android.app.PendingIntent
import s235040.wozniak.fplayer.Activities.MainActivity
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.support.v4.app.NotificationManagerCompat
import s235040.wozniak.fplayer.Controllers.TrackUpdateListener
import s235040.wozniak.fplayer.Playback.Track
import s235040.wozniak.fplayer.R


class FPlayerService: Service(), TrackUpdateListener {
    val INTENT_TYPE_UNDEFINED = -1
    val INTENT_TYPE_START_SERVICE = 0
    val INTENT_TYPE_PLAY_PAUSE_SONG = 1
    val INTENT_TYPE_PREVIOUS_SONG = 2
    val INTENT_TYPE_NEXT_SONG = 3

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
        createNotification()
        bindToMusicPlayer()
    }

    private fun bindToMusicPlayer() {
        MusicPlayer.addTrackUpdateListener(this)
    }

    private fun updateNotification(isPlaying: Boolean){
        val (previousSongIntent, playPauseIntent, nextSongIntent) = createControlIntents()
        val icon = if(isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val btnTitle = if(isPlaying) "Pause" else "Play" //FIXME
        val activityIntent = createActivityIntent()
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        notificationBuilder
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_music)
                .addAction(R.drawable.ic_previous, "Previous", previousSongIntent)
                .addAction(icon, btnTitle, playPauseIntent)
                .addAction(R.drawable.ic_next, "Next", nextSongIntent)
                .setContentTitle(lastNotificationTrack.title)
                .setContentText(lastNotificationTrack.author)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setStyle(MediaStyle())
                .setContentIntent(activityIntent)

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

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


    private fun updateNotification(track: Track, isPlaying: Boolean){
        val (previousSongIntent, playPauseIntent, nextSongIntent) = createControlIntents()
        val icon = if(isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val btnTitle = if(isPlaying) "Pause" else "Play" //FIXME
        val activityIntent = createActivityIntent()
        lastNotificationTrack = track
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        notificationBuilder
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_music)
                .addAction(R.drawable.ic_previous, "Previous", previousSongIntent)
                .addAction(icon, btnTitle, playPauseIntent)
                .addAction(R.drawable.ic_next, "Next", nextSongIntent)
                .setContentTitle(track.title)
                .setContentText(track.author)
                .setStyle(MediaStyle())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(activityIntent)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)

        val mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle("FPlayer notification")
                .setContentText("ABC")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)

        val notification = mBuilder.build()

        startForeground(NOTIFICATION_ID, notification)
    }

    fun handlePlaySongButton(index: Int){
        player.handlePlaySongButton(index)
    }

    fun handlePlayButton() {
        player.handlePlayButton()
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


    override fun notifyNowPlaying(listIndex: Int, track: Track) {
        updateNotification(track, true)
    }

    override fun notifyPaused(listIndex: Int) {
        updateNotification(false)
    }

    override fun notifyPlaying(listIndex: Int) {
        updateNotification(true)
    }

    override fun notifyStopped(listIndex: Int) {
        updateNotification(false)
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
        }
        return START_NOT_STICKY
    }

    private fun handlePreviousSongButton() {
        player.playPreviousSong()
    }

    private fun handleNextSongButton() {
        player.playNextSong()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
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
}