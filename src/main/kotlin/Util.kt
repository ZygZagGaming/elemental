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