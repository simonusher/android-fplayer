package s235040.wozniak.fplayer

import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import org.junit.jupiter.api.*
import s235040.wozniak.fplayer.Playback.MusicPlayer
import s235040.wozniak.fplayer.Playback.MusicPlayer.TrackQueue.INVALID_INDEX
import s235040.wozniak.fplayer.Playback.Track

class TrackQueueTests {

    @Test
    fun isEmpty_should_return_true_for_empty_queue() {
        reset()
        val queue = MusicPlayer.TrackQueue
        queue.playbackQueue.add(mock(Track::class.java))
        assertFalse(queue.isEmpty())
    }

    @Test
    fun isEmpty_should_return_false_for_nonempty_queue() {
        val queue = MusicPlayer.TrackQueue
        assertTrue(queue.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun createPlaybackQueue_should_throw_exception_with_incorect_start_index() {
        reset()
        val player = MusicPlayer
        val queue = MusicPlayer.TrackQueue
        player.trackList.clear()
        queue.playbackQueue.clear()
        player.trackList += mock(Track::class.java)
        player.trackList += mock(Track::class.java)
        player.trackList += mock(Track::class.java)
        assertEquals(3, player.trackList.size)
        queue.createPlaybackQueue(3)
    }

    @Test(expected = IllegalArgumentException::class)
    fun createPlaybackQueue_should_throw_exception_with_incorect_start_index_with_random_shuffle() {
        reset()
        val player = MusicPlayer
        val queue = MusicPlayer.TrackQueue
        player.trackList += mock(Track::class.java)
        player.trackList += mock(Track::class.java)
        player.trackList += mock(Track::class.java)
        assertEquals(3, player.trackList.size)
        player.randomShuffle = true
        queue.createPlaybackQueue(3)
    }

    @Test
    fun createPlaybackQueue_should_work_with_corect_start_index() {
        reset()
        val player = MusicPlayer
        val queue = MusicPlayer.TrackQueue
        player.trackList += mock(Track::class.java)
        player.trackList += mock(Track::class.java)
        player.trackList += mock(Track::class.java)
        assertEquals(3, player.trackList.size)
        queue.currentlyPlayedTrackIndex = 2
        queue.createPlaybackQueue(0)
        assertEquals(3, queue.playbackQueue.size)
        assertEquals(-1, queue.currentlyPlayedTrackIndex)
    }

    @Test
    fun createPlaybackQueue_should_work_with_corect_start_index_with_random_shuffle() {
        reset()
        val player = MusicPlayer
        val queue = MusicPlayer.TrackQueue
        player.trackList += mock(Track::class.java)
        player.trackList += mock(Track::class.java)
        player.trackList += mock(Track::class.java)
        assertEquals(3, player.trackList.size)
        player.randomShuffle = true

        var track = player.trackList[0]
        queue.createPlaybackQueue(0)
        assertEquals(3, queue.playbackQueue.size)
        assertEquals(-1, queue.currentlyPlayedTrackIndex)
        assertEquals(track, queue.playbackQueue.first())

        track = player.trackList[1]
        queue.createPlaybackQueue(1)
        assertEquals(3, queue.playbackQueue.size)
        assertEquals(-1, queue.currentlyPlayedTrackIndex)
        assertEquals(track, queue.playbackQueue.first())

        track = player.trackList[2]
        queue.createPlaybackQueue(2)
        assertEquals(3, queue.playbackQueue.size)
        assertEquals(-1, queue.currentlyPlayedTrackIndex)
        assertEquals(track, queue.playbackQueue.first())
    }

    @Test
    fun getNextTrack_should_work_when_called_twice_in_a_row() {
        reset()
        val queue = MusicPlayer.TrackQueue
        var track = queue.getNextTrack()
        assertNull(track)
        assertEquals(INVALID_INDEX, queue.currentlyPlayedTrackIndex)
        track = queue.getNextTrack()
        assertNull(track)
        assertEquals(INVALID_INDEX, queue.currentlyPlayedTrackIndex)
    }

    @Test
    fun getNextTrack_should_work_correctly_when_queue_not_empty() {
        reset()
        val player = MusicPlayer
        val queue = MusicPlayer.TrackQueue

        val t1 = Track(1, "", "a", "", 1)
        val t2 = Track(2, "", "b", "", 2)
        val t3 = Track(3, "", "c", "", 3)

        player.trackList += t1
        player.trackList += t2
        player.trackList += t3
        queue.createPlaybackQueue()

        var track = queue.getNextTrack()
        assertEquals(t1, track)
        assertEquals(queue.currentlyPlayedTrackIndex, 0)

        track = queue.getNextTrack()
        assertEquals(t2, track)
        assertEquals(queue.currentlyPlayedTrackIndex, 1)

        track = queue.getNextTrack()
        assertEquals(t3, track)
        assertEquals(queue.currentlyPlayedTrackIndex, 2)

        assertNull(queue.getNextTrack())
        assertEquals(queue.currentlyPlayedTrackIndex, INVALID_INDEX)

    }

    fun reset() {
        MusicPlayer.trackList.clear()
        MusicPlayer.randomShuffle = false
        MusicPlayer.loopingType = MusicPlayer.LoopingType.NO_LOOPING
        MusicPlayer.TrackQueue.playbackQueue.clear()
    }
}