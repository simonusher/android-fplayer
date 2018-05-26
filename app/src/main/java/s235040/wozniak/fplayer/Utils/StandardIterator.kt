package s235040.wozniak.fplayer.Utils

class StandardIterator<T>(val list:List<T>, var currentIndex: Int = 0): MyIterator<T>{
    override var dirty: Boolean = true
    override fun hasCurrent(): Boolean {
        return currentIndex >= 0 && currentIndex < list.size
    }

    override fun current(): T?{
        return if(hasCurrent() && !dirty){
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
        val newIndex = previousIndex()
        return newIndex >= 0
    }

    override fun next(): T {
        if(dirty){
            dirty = false
            return current() as T
        }
        else if(hasNext()){
            currentIndex += 1
            return current() as T
        } else {
            throw IllegalStateException("No objects left to take")
        }
    }

    override fun nextIndex(): Int {
        return currentIndex + 1
    }

    override fun previous(): T {
        if(dirty){
            dirty = false
            return current() as T
        }
        else if(hasPrevious()){
            currentIndex += 1
            return current() as T
        } else {
            throw IllegalStateException("No objects left to take")
        }
    }

    override fun previousIndex(): Int {
        return currentIndex - 1
    }
}