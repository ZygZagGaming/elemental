val defaultElements: MutableElementStack
    get() = mapOf(
    Elements.catalyst to 1.0,
    Elements.a to 1.0
).toMutableMap().toMutableDefaultedMap(0.0)

fun MutableElementStack.copy(): MutableElementStack = toMutableMap().toMutableDefaultedMap(defaultValue)
fun MutableElementStack.deduct(other: ElementStack) {
    for ((key, value) in other) this[key] = this[key].minus(value)
}

fun MutableElementStack.add(other: ElementStack) {
    for ((key, value) in other) this[key] = this[key].plus(value)
}

val defaultStack get() = Elements.values.associateWith { 0.0 }.toDefaultedMap(0.0)
val defaultMutableStack get() = defaultStack.toMutableMap().toMutableDefaultedMap(0.0)

data class ElementType(val name: String, val symbol: Char) {
    fun withCount(n: Double): ElementStack = mapOf(this to n).toDefaultedMap(0.0)
}

typealias ElementStack = DefaultedMap<ElementType, Double>
typealias MutableElementStack = MutableDefaultedMap<ElementType, Double>

fun ElementStack.format(): String = filter { (k, v) -> v != 0.0 }.map { (k, v) -> "${if (v == 1.0) "" else v.toString1DecPlace()}${k.symbol}" }.joinToString(" + ")