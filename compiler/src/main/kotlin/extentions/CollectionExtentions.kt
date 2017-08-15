package extentions

import java.util.*

/**
 * Created by gilgoldzweig on 13/08/2017.
 */
private val random = Random()

fun Collection<*>?.isNullOrEmpty(): Boolean = this == null || this.isEmpty()
fun Collection<*>?.isNotNullOrEmpty() = !isNullOrEmpty()

fun Map<*, *>?.isNullOrEmpty() = this == null || this.isEmpty()
fun Map<*, *>?.isNotNullOrEmpty() = !isNullOrEmpty()

fun Set<*>?.isNullOrEmpty() = this == null || this.isEmpty()
fun Set<*>?.isNotNullOrEmpty() = !isNullOrEmpty()

fun List<*>?.isNullOrEmpty() = this == null || this.isEmpty()
fun List<*>?.isNotNullOrEmpty(): Boolean = !isNullOrEmpty()
fun <T>List<T>.random() = this[random.nextInt(size)]

operator fun ArrayList<Any>.plusAssign(obj: Any) = add(size, obj)
infix fun <T> ArrayList<T>.addIfNotExist(obj: T) = if (!contains(obj)) add(obj) else false
infix fun <T> ArrayList<T>.removeIfExist(obj: T) = if (contains(obj)) remove(obj) else false
operator fun <T>List<T>.div(amount: Int): ArrayList<List<T>> {
    val arrays = ArrayList<List<T>>()
    var startIndexOfCattedList = 0
    for (indexOfList in 0 until this.size) {
        if ((indexOfList + 1) % amount == 0) {
            if (startIndexOfCattedList == 0) {
                arrays.add(subList(startIndexOfCattedList, indexOfList + 1))
            } else {
                arrays.add(subList(startIndexOfCattedList, indexOfList))
            }
            startIndexOfCattedList = indexOfList
        }
    }
    return arrays
}