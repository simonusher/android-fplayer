package s235040.wozniak.fplayer.Application

import android.Manifest
import android.app.Service
import android.content.Intent
import android.os.*
import android.widget.Toast
import s235040.wozniak.fplayer.Playback.MusicPlayer
import s235040.wozniak.fplayer.Utils.PermissionUtils
import android.support.v4.app.NotificationCompat
import android.app.PendingIntent
import s235040.wozniak.fplayer.Activities.MainActivity
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import s235040.wozniak.fplayer.R


class FPlayerService: Service() {
    val player: MusicPlayer = MusicPlayer
    val binder: IBinder = LocalBinder()
    val CHANNEL_ID = "DEFAULT_CHANNEL_ID"

    inner class LocalBinder : Binder() {
        internal val service: FPlayerService
            get() = this@FPlayerService
    }

    override fun onCreate() {
        acquirePermissions()
        initializeMusicPlayer()
        createNotificationChannel()
        createNotification()
    }

    private fun createNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = (Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)

        val mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle("FPlayer notification")
                .setContentText("ABC")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notification = mBuilder.build()

        startForeground(1337, notification)
    }

    fun playTrack(index: Int){
        player.playTrack(index)
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
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }
}