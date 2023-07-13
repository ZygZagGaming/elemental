import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Storage
import kotlin.math.*

fun sign(n: Int) = if (n < 0) -1 else if (n > 0) 1 else 0
data class Vec2(val x: Double, val y: Double) {
    operator fun plus(other: Vec2) = Vec2(x + other.x, y + other.y)
    operator fun minus(other: Vec2) = Vec2(x - other.x, y - other.y)
    operator fun times(other: Double) = Vec2(x * other, y * other)
    operator fun div(other: Double) = Vec2(x / other, y / other)
    val transpose get() = Vec2(y, x)
    val magnitude get() = sqrt(x * x + y * y)
    val normalized get() = this / magnitude
}

fun Double.toString1DecPlace(): String {
    if (abs(this % 1.0) <= 1e-7) return toString().split('.')[0]
    val str = (this * 10).toString()
    return str.substring(0 until str.length - 1) + '.' + str.last()
}

data class Indexed<T>(val t: T, val index: Int)

fun <K, V> Map<K, V>.mutate(function: (MutableMap<K, V>) -> Unit): Map<K, V> {
    val mutable = toMutableMap()
    function(mutable)
    return mutable.toMap()
}

fun <K, V> DefaultedMap<K, V>.mutateDefaulted(function: (MutableDefaultedMap<K, V>) -> Unit): DefaultedMap<K, V> {
    val mutable = toMutableDefaultedMap()
    function(mutable)
    return mutable.unmutable()
}

typealias BiMap<K1, K2, V> = Map<Pair<K1, K2>, V>

fun <K1, K2, V> Map<K1, Map<K2, V>>.toBiMap(): BiMap<K1, K2, V> = flatMap { (k1, v1) -> v1.map { (k2, v) -> (k1 to k2) to v } }.toMap()

fun <K1, K2, V> BiMap<K1, K2, V>.switchKeys(): BiMap<K2, K1, V> = mapKeys { (k, v) -> k.switch() }

fun <K1, K2, V> BiMap<K1, K2, V>.toNestedMap(): Map<K1, Map<K2, V>> = entries.groupBy { it.key.first }.mapValues { (_, l) -> l.associate { (p, v) -> p.second to v } }

fun <A, B> Pair<A, B>.switch() = second to first
fun Double.squared() = this * this
fun Int.squared() = this * this
interface StatMap<K, V>: Indexable<K, V> {
    val defaultValue: V
    fun changed(): Boolean
    fun changed(key: K): Boolean
    fun clearChanged(key: K)
    fun clearChanged()
    fun clear()
}

interface MutableStatMap<K, V>: StatMap<K, V>, MutableIndexable<K, V>
open class BasicMutableStatMap<K, V>(backingMap: Map<K, V>, override val defaultValue: V): MutableStatMap<K, V> {
    private val backingMap = backingMap.toMutableMap()
    private val changedSet = mutableSetOf<K>()
    val asMap get() = backingMap.toMap()

    override fun get(key: K): V = backingMap[key] ?: defaultValue
    override fun set(key: K, value: V) {
        if (this[key] != value) {
            backingMap[key] = value
            changedSet.add(key)
        }
    }

    override fun clearChanged() {
        changedSet.clear()
    }

    override fun clearChanged(key: K) {
        changedSet.remove(key)
    }

    override fun clear() {
        backingMap.keys.forEach { backingMap[it] = defaultValue }
        changedSet.addAll(backingMap.keys)
    }

    override fun changed(): Boolean = changedSet.isNotEmpty()

    override fun changed(key: K): Boolean = key in changedSet
}

class ProductStatMap<K, V>(val a: StatMap<K, V>, val b: StatMap<K, V>, val multiplyFunction: (V, V) -> V):
    StatMap<K, V> {
    private operator fun V.times(other: V) = multiplyFunction(this, other)
    override fun get(key: K): V = a[key] * b[key]

    override val defaultValue: V get() = a.defaultValue * b.defaultValue


    override fun changed(): Boolean = a.changed() || b.changed()

    override fun clearChanged() {
        a.clearChanged()
        b.clearChanged()
    }

    override fun clear() {
        a.clear()
        b.clear()
    }

    override fun clearChanged(key: K) {
        a.clearChanged(key)
        b.clearChanged(key)
    }

    override fun changed(key: K): Boolean = a.changed(key) || b.changed(key)
}

data class SimpleIndexable<K, V>(private val getter: (K) -> V): Indexable<K, V> {
    override fun get(key: K): V = getter(key)
}

data class SimpleMutableIndexable<K, V>(private val getter: (K) -> V, private val setter: (K, V) -> Unit):
    MutableIndexable<K, V> {
    override fun get(key: K): V = getter(key)
    override fun set(key: K, value: V) {
        setter(key, value)
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun setElementAmount(elementId: String, amount: Double) {
    val element = Elements.map[elementId]
    if (element != null) Stats.elementAmounts[element] = amount
}

open class DefaultedMap<K, V>(private val backingMap: Map<K, V>, val defaultValue: V): Map<K, V> {
    override val entries: Set<Map.Entry<K, V>> get() = backingMap.entries
    override val keys: Set<K> get() = backingMap.keys
    override val size: Int get() = backingMap.size
    override val values: Collection<V> get() = backingMap.values
    override fun isEmpty(): Boolean = backingMap.isEmpty()

    override operator fun get(key: K): V = backingMap[key] ?: defaultValue

    override fun containsValue(value: V): Boolean = backingMap.containsValue(value) || value == defaultValue

    override fun containsKey(key: K): Boolean = backingMap.containsKey(key)
}

class MutableDefaultedMap<K, V>(private val backingMap: MutableMap<K, V>, defaultValue: V): DefaultedMap<K, V>(backingMap, defaultValue), MutableMap<K, V> {
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = backingMap.entries
    override val keys: MutableSet<K> get() = backingMap.keys
    override val values: MutableCollection<V> get() = backingMap.values

    override fun clear() {
        backingMap.clear()
    }

    override fun remove(key: K): V {
        val value = this[key]
        this[key] = defaultValue
        return value
    }

    override fun putAll(from: Map<out K, V>) {
        for (key in from.keys) this[key] = from[key] ?: defaultValue
    }

    override fun put(key: K, value: V): V {
        return backingMap.put(key, value) ?: defaultValue
    }

    fun unmutable(): DefaultedMap<K, V> = toMap().toDefaultedMap(defaultValue)
}

fun <K, V> Map<K, V>.toDefaultedMap(value: V) = DefaultedMap(this, value)
fun <K, V> MutableMap<K, V>.toMutableDefaultedMap(value: V) = MutableDefaultedMap(this, value)
fun <K, V> DefaultedMap<K, V>.toMutableDefaultedMap() = MutableDefaultedMap(toMutableMap(), defaultValue)
fun Double.roundToOneDecimalPlace() = (this * 10).roundToInt() / 10.0
fun Double.roundTo(places: Int) = (this * 10.0.pow(places)).roundToInt() / 10.0.pow(places)
fun <K, V> defaultedMapOf(defaultValue: V, vararg values: Pair<K, V>) = mapOf(*values).toDefaultedMap(defaultValue)
fun <K, V> mutableDefaultedMapOf(defaultValue: V, vararg values: Pair<K, V>) = mutableMapOf(*values).toMutableDefaultedMap(defaultValue)
fun elementStackOf(vararg values: Pair<ElementType, Double>, defaultValue: Double = 0.0): ElementStack =
    defaultedMapOf(defaultValue, *values)
operator fun ElementStack.plus(other: ElementStack): ElementStack = entries.associate { (k, v) -> k to (v + other[k]) }.toDefaultedMap(0.0)
fun Map<ElementType, Double>.toElementStack(): ElementStack = toDefaultedMap(0.0)
fun vh(percent: Double): Double {
    val h = max(document.documentElement!!.clientHeight, window.innerHeight)
    return (percent * h) / 100.0
}

fun vw(percent: Double): Double {
    val w = max(document.documentElement!!.clientWidth, window.innerWidth)
    return (percent * w) / 100.0
}

fun vmin(percent: Double): Double {
    return min(vh(percent), vw(percent))
}

fun vmax(percent: Double): Double {
    return max(vh(percent), vw(percent))
}

operator fun Storage.set(name: String, value: String) {
    setItem(name, value)
}

operator fun Storage.get(name: String): String {
    return getItem(name) ?: ""
}

enum class SaveMode {
    LOCAL_STORAGE
}

data class Page(val name: String)