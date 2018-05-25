package s235040.wozniak.fplayer.Utils

/**
 * Created by Szymon on 24.05.2018.
 */
object StringUtils{
    fun getLengthString(minutes: Int, seconds: Int): String{
        return getTimeWithZero(minutes) + ":" + getTimeWithZero(seconds)
    }

    fun getTimeWithZero(number: Int): String{
        return if (number < 10) "0$number" else number.toString()
    }

    fun getDurationStringFromMilis(milis: Int): String{
        val minutes = (milis / (1000 * 60) % 60)
        val seconds = (milis / 1000)  % 60
        return getLengthString(minutes, seconds)
    }
}