package s235040.wozniak.fplayer.Utils

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ServiceCompat
import android.support.v4.content.ContextCompat

/**
 * Created by Szymon on 24.05.2018.
 */
object PermissionUtils {
    val CODE_READ_EXTERNAL_STORAGE: Int = 123
    fun acquirePermission(context: Context, permission: String, requestCode: Int): Boolean {
        if (!hasPermission(context, permission)) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(permission), requestCode)
            return hasPermission(context, permission)
        }
        return true
    }

    fun hasPermission(context: Context, permission: String): Boolean{
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}