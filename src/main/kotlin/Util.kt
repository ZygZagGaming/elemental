import kotlin.math.abs
import kotlin.math.sqrt

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

typealias BiMap<K1, K2, V> = Map<Pair<K1, K2>, V>

fun <K1, K2, V> Map<K1, Map<K2, V>>.toBiMap(): BiMap<K1, K2, V> = flatMap { (k1, v1) -> v1.map { (k2, v) -> (k1 to k2) to v } }.toMap()

fun <K1, K2, V> BiMap<K1, K2, V>.switchKeys(): BiMap<K2, K1, V> = mapKeys { (k, v) -> k.switch() }

fun <K1, K2, V> BiMap<K1, K2, V>.toNestedMap(): Map<K1, Map<K2, V>> = entries.groupBy { it.key.first }.mapValues { (k, l) -> l.map { (p, v) -> p.second to v }.toMap() }

fun <A, B> Pair<A, B>.switch() = second to first