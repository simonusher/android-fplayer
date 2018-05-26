package s235040.wozniak.fplayer.Utils

interface MyIterator<T> : ListIterator<T> {
    var dirty: Boolean
    fun hasCurrent(): Boolean
    fun current(): T?
}