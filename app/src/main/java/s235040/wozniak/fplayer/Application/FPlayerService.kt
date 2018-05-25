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
import android.os.Build
import android.support.v4.app.NotificationManagerCompat
import s235040.wozniak.fplayer.Controllers.TrackUpdateListener
import s235040.wozniak.fplayer.Playback.Track
import s235040.wozniak.fplayer.R


class FPlayerService: Service(), TrackUpdateListener {
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
        val intent1 = Intent(this, MainActivity::class.java)
        intent1.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val intent2 = Intent(this, MainActivity::class.java)
        intent2.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val intent3 = Intent(this, MainActivity::class.java)
        intent3.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent1 = PendingIntent.getActivity(this, 0,
                intent1, 0)
        val pendingIntent2 = PendingIntent.getActivity(this, 1,
                intent2, 0)
        val pendingIntent3 = PendingIntent.getActivity(this, 2,
                intent3, 0)
        val icon = if(isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val btnTitle = if(isPlaying) "Pause" else "Play" //FIXME
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        notificationBuilder
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_music)
                .addAction(R.drawable.ic_previous, "Previous", pendingIntent1)
                .addAction(icon, btnTitle, pendingIntent2)
                .addAction(R.drawable.ic_next, "Next", pendingIntent3)
                .setContentTitle(lastNotificationTrack.title)
                .setContentText(lastNotificationTrack.author)
                .setStyle(MediaStyle())

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun updateNotification(track: Track, isPlaying: Boolean){
        val intent1 = Intent(this, MainActivity::class.java)
        intent1.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val intent2 = Intent(this, MainActivity::class.java)
        intent2.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val intent3 = Intent(this, MainActivity::class.java)
        intent3.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent1 = PendingIntent.getActivity(this, 0,
                intent1, 0)
        val pendingIntent2 = PendingIntent.getActivity(this, 1,
                intent2, 0)
        val pendingIntent3 = PendingIntent.getActivity(this, 2,
                intent3, 0)
        val icon = if(isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val btnTitle = if(isPlaying) "Pause" else "Play" //FIXME
        lastNotificationTrack = track
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        notificationBuilder
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_music)
                .addAction(R.drawable.ic_previous, "Previous", pendingIntent1)
                .addAction(icon, btnTitle, pendingIntent2)
                .addAction(R.drawable.ic_next, "Next", pendingIntent3)
                .setContentTitle(track.title)
                .setContentText(track.author)
                .setStyle(MediaStyle())
                .build()
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
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()

        return START_NOT_STICKY
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