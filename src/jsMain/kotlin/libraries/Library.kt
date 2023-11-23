@file:Suppress("unused")

package libraries
import kotlin.collections.set

open class Library<T> {
    val map get() = backingMap.toMap()
    val values get() = map.values
    private val backingMap = mutableMapOf<String, T>()
    protected fun <K: T> register(id: String, elem: K): K {
        if (id !in backingMap) backingMap[id] = elem
        else throw Exception("Duplicate values in registry")
        return elem
    }

    fun id(elem: T): String? {
        return map.entries.firstOrNull { it.value == elem }?.key
    }
}