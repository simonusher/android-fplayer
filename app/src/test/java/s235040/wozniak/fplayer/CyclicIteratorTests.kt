package s235040.wozniak.fplayer

import org.junit.Test
import org.junit.Assert.*
import s235040.wozniak.fplayer.Utils.CyclicIterator

class CyclicIteratorTests{
    @Test
    fun cyclic_iterator_test() {
        val list = mutableListOf(1, 2, 3, 4)
        val iterator = CyclicIterator(list)
        assertFalse(iterator.hasCurrent())
        assertTrue(iterator.hasPrevious())
        assertTrue(iterator.hasNext())
        assertEquals(iterator.next(), 1)
        assertEquals(iterator.current(), 1)
        assertEquals(iterator.next(), 2)
        assertEquals(iterator.current(), 2)
        assertEquals(iterator.previous(), 1)
        assertEquals(iterator.current(), 1)
        assertEquals(iterator.next(), 2)
        assertEquals(iterator.current(), 2)
        assertEquals(iterator.next(), 3)
        assertEquals(iterator.current(), 3)
        assertEquals(iterator.next(), 4)
        assertEquals(iterator.current(), 4)
        assertEquals(iterator.next(), 1)
        assertEquals(iterator.current(), 1)
        assertEquals(iterator.previous(), 4)
        assertEquals(iterator.current(), 4)
    }

    @Test
    fun cyclic_iterator_from_end_test(){
        val list = mutableListOf(1, 2, 3, 4)
        val iterator = CyclicIterator.fromEnd(list)
        assertFalse(iterator.hasCurrent())
        assertTrue(iterator.hasPrevious())
        assertTrue(iterator.hasNext())
        assertEquals(iterator.previous(), 4)
        assertEquals(iterator.current(), 4)
        assertEquals(iterator.next(), 1)
        assertEquals(iterator.current(), 1)
        assertEquals(iterator.previous(), 4)
        assertEquals(iterator.current(), 4)
        assertEquals(iterator.previous(), 3)
        assertEquals(iterator.current(), 3)
        assertEquals(iterator.previous(), 2)
        assertEquals(iterator.current(), 2)
        assertEquals(iterator.previous(), 1)
        assertEquals(iterator.current(), 1)
        assertEquals(iterator.previous(), 4)
        assertEquals(iterator.current(), 4)
        assertEquals(iterator.previous(), 3)
        assertEquals(iterator.current(), 3)
        assertEquals(iterator.next(), 4)
        assertEquals(iterator.current(), 4)
        assertEquals(iterator.next(), 1)
        assertEquals(iterator.current(), 1)
        assertEquals(iterator.previous(), 4)
        assertEquals(iterator.current(), 4)
    }
}