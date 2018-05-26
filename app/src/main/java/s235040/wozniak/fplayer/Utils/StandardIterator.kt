package s235040.wozniak.fplayer.Utils

class StandardIterator<T>(val list:List<T>, var currentIndex: Int = -1): MyIterator<T>{
    override fun hasCurrent(): Boolean {
        return currentIndex >= 0 && currentIndex < list.size
    }

    override fun current(): T?{
        return if(hasCurrent()){
            list[currentIndex]
        } else {
            null
        }
    }

    override fun hasNext(): Boolean {
        val newIndex = nextIndex()
        return newIndex < list.size
    }

    override fun hasPrevious(): Boolean {
        return currentIndex >= 0
    }

    override fun next(): T {
        currentIndex += 1
        return list[currentIndex]
    }

    override fun nextIndex(): Int {
        return currentIndex + 1
    }

    override fun previous(): T {
        currentIndex -= 1
        if(currentIndex < 0){
            currentIndex = 0
        }
        return list[currentIndex]
    }

    override fun previousIndex(): Int {
        return currentIndex - 1
    }

    companion object {
        fun<T> fromEnd(list: MutableList<T>): StandardIterator<T>{
            return StandardIterator(list, list.size)
        }
    }
}