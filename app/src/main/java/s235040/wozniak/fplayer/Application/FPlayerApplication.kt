package s235040.wozniak.fplayer.Application

import android.app.Application
import android.app.Service
import android.content.Intent

class FPlayerApplication: Application() {
    lateinit var musicPlayerService: Service

    override fun onCreate() {
        startMusicPlayerService()
        super.onCreate()
    }

    private fun startMusicPlayerService() {
        val serviceIntent = Intent(this, FPlayerService::class.java)
        startService(serviceIntent)
    }

    override fun onTerminate() {
//        this::musicPlayerService.isInitialized.run {
//            musicPlayerService.stopSelf()
//        }
        super.onTerminate()
    }
}