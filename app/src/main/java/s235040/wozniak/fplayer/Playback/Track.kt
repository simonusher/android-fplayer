package s235040.wozniak.fplayer.Playback


import s235040.wozniak.fplayer.Utils.StringUtils

/**
 * Created by Szymon on 24.05.2018.
 */
open class Track(val path: String, val title: String, val author: String, val albumArtPath: String, durationMilis: Int){
    val duration: String = getDurationStringFromMilis(durationMilis)
    companion object {
        fun getDurationStringFromMilis(durationMilis: Int): String{
            return StringUtils.getDurationStringFromMilis(durationMilis)
        }
    }

    override fun toString(): String {
        return title
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Track

        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }


}