package s235040.wozniak.fplayer.Utils

interface MyIterator<T> : ListIterator<T> {
    fun hasCurrent(): Boolean
    fun current(): T?

}