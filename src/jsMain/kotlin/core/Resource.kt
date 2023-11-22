package core

import libraries.Resources
import libraries.Symbols
import kotlin.math.floor
import kotlin.math.roundToInt

val defaultStartingElements: MutableResourceStack
    get() = _defaultStartingElements.toMutableDefaultedMap()
private val _defaultStartingElements = mapOf(
    Resources.catalyst to 1.0,
    Resources.a to 1.0
).toMutableMap().toMutableDefaultedMap(0.0)

fun MutableResourceStack.copy(): MutableResourceStack = toMutableMap().toMutableDefaultedMap(defaultValue)
fun MutableResourceStack.deduct(other: ResourceStack) {
    for ((key, value) in other) this[key] = this[key].minus(value)
}

fun MutableResourceStack.add(other: ResourceStack) {
    for ((key, value) in other) this[key] = this[key].plus(value)
}

val emptyStack get() = Resources.values.associateWith { 0.0 }.toDefaultedMap(0.0)
val emptyMutableStack get() = emptyStack.toMutableMap().toMutableDefaultedMap(0.0)

data class Resource(val name: String, val symbol: String, val isDecimal: Boolean = false, val isElement: Boolean = false, val color: String = "#fff") {
    constructor(name: String, symbol: Char, isDecimal: Boolean = false, isElement: Boolean = false, color: String = "#fff"): this(name, symbol.toString(), isDecimal, isElement, color)
    fun withCount(n: Double): ResourceStack = mapOf(this to n).toDefaultedMap(0.0)
    val capText = "<u>$symbol</u>"
}

typealias ResourceStack = DefaultedMap<Resource, Double>
typealias MutableResourceStack = MutableDefaultedMap<Resource, Double>

fun ResourceStack.format(): String = filter { (_, v) ->
    v != 0.0
}.map { (k, v) ->
    "${if (v == 1.0 || (!k.isDecimal && floor(v).roundToInt() == 1)) "" else if (k.isDecimal) v.toString(1) else floor(v).roundToInt().toString()}${k.symbol}"
}.joinToString(" + ")

fun ResourceStack.formatReactionInputs(): String {
    val list = filter { (_, v) -> v != 0.0 }
    return list.map { (k, v) ->
        val showNumber = v == 1.0 || (!k.isDecimal && floor(v).roundToInt() == 1)
        val red = v > Stats.elementAmounts[k]
        val strikethrough = v > Stats.functionalElementUpperBounds[k]
        "${if (strikethrough) "<strike>" else if (red) "<r>" else ""}${if (showNumber) "" else if (k.isDecimal) v.toString(1) else floor(v).roundToInt().toString()}${k.symbol}${if (strikethrough) "</strike>" else if (red) "</r>" else ""}"
    }.joinToString(" + ")
}

fun ResourceStack.formatReactionOutputs(): String {
    val list = filter { (_, v) -> v != 0.0 }
    return list.map { (k, v) ->
        val showNumber = v == 1.0 || (!k.isDecimal && floor(v).roundToInt() == 1)
        val red = if (k == Resources.heat) v >= Stats.functionalElementUpperBounds[k] - Stats.elementAmounts[k] else Stats.elementAmounts[k] + 1 >= Stats.functionalElementUpperBounds[k]
        "${if (red) "<r>" else ""}${if (showNumber) "" else if (k.isDecimal) v.toString(1) else floor(v).roundToInt().toString()}${k.symbol}${if (red) "</r>" else ""}"
    }.joinToString(" + ")
}

val Resource.delta get() = Resources.symbolMap["${Symbols.delta}$symbol"]!!