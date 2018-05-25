package s235040.wozniak.fplayer.Application

import android.app.Application
import android.content.Intent

class FPlayerApplication: Application() {
    override fun onCreate() {
        startMusicPlayerService()
        super.onCreate()
    }

    private fun startMusicPlayerService() {
        val serviceIntent = Intent(this, FPlayerService::class.java)
        startService(serviceIntent)
    }
}