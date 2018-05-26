package s235040.wozniak.fplayer.Utils

class CyclicIterator<T>(val list:List<T>, var currentIndex: Int = -1): MyIterator<T>{
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
        return !list.isEmpty()
    }

    override fun hasPrevious(): Boolean {
        return !list.isEmpty()
    }

    override fun next(): T {
        currentIndex += 1
        if(currentIndex >= list.size){
            currentIndex = 0
        }
        return list[currentIndex]
    }

    override fun nextIndex(): Int {
        return if(currentIndex + 1 >= list.size) 0 else currentIndex + 1
    }

    override fun previous(): T {
        currentIndex -= 1
        if(currentIndex < 0){
            currentIndex = list.size - 1
        }
        return list[currentIndex]
    }

    override fun previousIndex(): Int {
        return if(currentIndex - 1 < 0) list.size -1 else currentIndex - 1
    }
    companion object {
        fun<T> fromEnd(list: MutableList<T>): CyclicIterator<T>{
            return CyclicIterator(list, list.size)
        }
    }
}