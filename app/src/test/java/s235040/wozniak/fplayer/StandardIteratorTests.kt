package s235040.wozniak.fplayer

import org.junit.Assert.*
import org.junit.Test
import s235040.wozniak.fplayer.Utils.StandardIterator

class StandardIteratorTests{
    @Test
    fun standard_iterator_test() {
        val list = mutableListOf(1, 2, 3, 4)
        val iterator = StandardIterator(list)
        assertFalse(iterator.hasCurrent())
        assertFalse(iterator.hasPrevious())
        assertTrue(iterator.hasNext())
        assertEquals(iterator.next(), 1)
        assertEquals(iterator.current(), 1)
        assertEquals(iterator.previous(), 1)
        assertEquals(iterator.current(), 1)
        assertEquals(iterator.previous(), 1)
        assertEquals(iterator.current(), 1)
        assertEquals(iterator.next(), 2)
        assertEquals(iterator.current(), 2)
        assertEquals(iterator.next(), 3)
        assertEquals(iterator.current(), 3)
        assertEquals(iterator.next(), 4)
        assertEquals(iterator.current(), 4)
        assertFalse(iterator.hasNext())
        assertEquals(iterator.previous(), 3)
        assertEquals(iterator.current(), 3)
        assertEquals(iterator.previous(), 2)
        assertEquals(iterator.current(), 2)
        assertEquals(iterator.previous(), 1)
        assertEquals(iterator.current(), 1)
        assertEquals(iterator.previous(), 1)
        assertEquals(iterator.current(), 1)
    }

    @Test
    fun standard_iterator_from_end_test() {
        val list = mutableListOf(1, 2, 3, 4)
        val iterator = StandardIterator.fromEnd(list)
        assertFalse(iterator.hasNext())
        assertFalse(iterator.hasPrevious())
        assertTrue(iterator.hasNext())
        assertEquals(iterator.next(), 1)
        assertEquals(iterator.current(), 1)
        assertEquals(iterator.previous(), 1)
        assertEquals(iterator.current(), 1)
        assertEquals(iterator.previous(), 1)
        assertEquals(iterator.current(), 1)
        assertEquals(iterator.next(), 2)
        assertEquals(iterator.current(), 2)
        assertEquals(iterator.next(), 3)
        assertEquals(iterator.current(), 3)
        assertEquals(iterator.next(), 4)
        assertEquals(iterator.current(), 4)
        assertFalse(iterator.hasNext())
        assertEquals(iterator.previous(), 3)
        assertEquals(iterator.current(), 3)
        assertEquals(iterator.previous(), 2)
        assertEquals(iterator.current(), 2)
        assertEquals(iterator.previous(), 1)
        assertEquals(iterator.current(), 1)
        assertEquals(iterator.previous(), 1)
        assertEquals(iterator.current(), 1)
    }
}