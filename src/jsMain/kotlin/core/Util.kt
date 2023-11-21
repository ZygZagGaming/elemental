package core

import kotlinx.browser.document
import kotlinx.browser.window
import libraries.Resources
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

interface MutableStatMap<K, V>: StatMap<K, V>, MutableIndexable<K, V> {
    fun addListener(key: K, name: String, listener: (V?, V) -> Unit)
    fun removeListener(key: K, name: String)
}
open class BasicMutableStatMap<K, V>(backingMap: Map<K, V>, override val defaultValue: V): MutableStatMap<K, V> {
    private val backingMap = backingMap.toMutableMap()
    private val changedSet = mutableSetOf<K>()
    private val listeners = mutableMapOf<K, MutableMap<String, (V?, V) -> Unit>>()
    val asMap get() = backingMap.toMap()

    override fun get(key: K): V = backingMap[key] ?: defaultValue
    override fun set(key: K, value: V) {
        if (this[key] != value) {
            val old = backingMap[key]
            backingMap[key] = value
            changedSet.add(key)
            //if (this == core.Stats.elementAmounts) console.log("value of $key changed from $old to $value")
            listeners[key]?.values?.forEach { it(old, value) }
        }
    }

    override fun addListener(key: K, name: String, listener: (V?, V) -> Unit) {
        if (key in listeners) listeners[key]!![name] = listener
        else listeners[key] = mutableMapOf(name to listener)
    }

    override fun removeListener(key: K, name: String) {
        listeners[key]?.remove(name)
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

    fun setValues(newValues: Map<K, V>) {
        clear()
        for (key in newValues.keys) set(key, newValues[key]!!)
    }
}

fun <K, V: Comparable<V>> MutableStatMap<K, V>.addThresholdListener(key: K, name: String, threshold: V, listener: (V) -> Unit) {
    addListener(key, name) { old, new ->
        if (old == null || (old > threshold) != (new > threshold)) listener(new)
    }
}

class ProductStatMap<K, V1, V2, V3>(val a: StatMap<K, V1>, val b: StatMap<K, V2>, val multiplyFunction: (V1, V2) -> V3):
    StatMap<K, V3> {
    private operator fun V1.times(other: V2): V3 = multiplyFunction(this, other)
    override fun get(key: K): V3 = a[key] * b[key]

    override val defaultValue: V3 get() = a.defaultValue * b.defaultValue


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
    val element = Resources.map[elementId]
    if (element != null) Stats.elementAmounts[element] = amount
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun setElementBaseCap(elementId: String, amount: Double) {
    val element = Resources.map[elementId]
    if (element != null) Stats.baseElementUpperBounds[element] = amount
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
    private val listeners = mutableMapOf<K, MutableMap<String, (V?, V) -> Unit>>()

    fun addListener(key: K, name: String, listener: (V?, V) -> Unit) {
        if (key in listeners) listeners[key]!![name] = listener
        else listeners[key] = mutableMapOf(name to listener)
    }

    fun removeListener(key: K, name: String) {
        listeners[key]?.remove(name)
    }

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
        listeners[key]?.values?.forEach { it(backingMap[key], value) }
        return backingMap.put(key, value) ?: defaultValue
    }

    fun unmutable(): DefaultedMap<K, V> = toMap().toDefaultedMap(defaultValue)
}

fun <K, V> Map<K, V>.toDefaultedMap(value: V) = DefaultedMap(this, value)
fun <K, V> MutableMap<K, V>.toMutableDefaultedMap(value: V) = MutableDefaultedMap(this, value)
fun <K, V> DefaultedMap<K, V>.toMutableDefaultedMap() = MutableDefaultedMap(toMutableMap(), defaultValue)
fun Double.roundToOneDecimalPlace() = (this * 10).roundToInt() / 10.0
fun Double.roundTo(places: Int) = (this * 10.0.pow(places)).roundToInt() / 10.0.pow(places)
fun Double.toString(places: Int): String {
    val power = 10.0.pow(-places)
    if (places == 0) return roundToInt().toString()
    return if (places < 0) (roundToInt() - (roundToInt() % power.roundToInt())).toString() else {
        val k = (power * (this / power).roundToInt()).toString().split('.')
        val second = if (k.size >= 2) k[1].substring(0, places) else ""
        val k2 = k[0] + '.' + second
        k2 + "0".repeat(places - second.length)
    }
}
fun <K, V> defaultedMapOf(defaultValue: V, vararg values: Pair<K, V>) = mapOf(*values).toDefaultedMap(defaultValue)
fun <K, V> mutableDefaultedMapOf(defaultValue: V, vararg values: Pair<K, V>) = mutableMapOf(*values).toMutableDefaultedMap(defaultValue)
fun elementStackOf(vararg values: Pair<Resource, Double>, defaultValue: Double = 0.0): ResourceStack =
    defaultedMapOf(defaultValue, *values)
operator fun ResourceStack.plus(other: ResourceStack): ResourceStack = entries.associate { (k, v) -> k to (v + other[k]) }.toDefaultedMap(0.0)
fun Map<Resource, Double>.toElementStack(): ResourceStack = toDefaultedMap(0.0)
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

fun interpolationFunction(f: (Double) -> Double): (Double) -> Double {
    val zero = f(0.0)
    val normalization = 1.0 / (f(1.0) - zero)
    return { t -> (f(t) - zero) * normalization }
}

fun interpolationFunctionForwardsBackwards(f: (Double) -> Double): (Double) -> Double {
    val zero = f(0.0)
    val normalization = 1.0 / (f(1.0) - zero)
    return { t -> if (t <= 0.5) (f(2 * t) - zero) * normalization else 1 - (f(2 * t - 1) - zero) * normalization }
}

fun <T> List<T>.firstTwo(): Pair<T, T> = this[0] to this[1]

fun Boolean.toStringOnOff(on: String = "On", off: String = "Off") = if (this) on else off