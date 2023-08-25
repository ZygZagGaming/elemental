package core

import libraries.Elements
import libraries.Symbols
import kotlin.math.floor
import kotlin.math.roundToInt

val defaultStartingElements: MutableElementStack
    get() = _defaultStartingElements.toMutableDefaultedMap()
private val _defaultStartingElements = mapOf(
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

val emptyStack get() = Elements.values.associateWith { 0.0 }.toDefaultedMap(0.0)
val emptyMutableStack get() = emptyStack.toMutableMap().toMutableDefaultedMap(0.0)

data class ElementType(val name: String, val symbol: String, val isDecimal: Boolean = false, val isBasic: Boolean = false) {
    constructor(name: String, symbol: Char, isDecimal: Boolean = false, isBasic: Boolean = false): this(name, symbol.toString(), isDecimal, isBasic)
    fun withCount(n: Double): ElementStack = mapOf(this to n).toDefaultedMap(0.0)
    val capText = "<u>$symbol</u>"
}

typealias ElementStack = DefaultedMap<ElementType, Double>
typealias MutableElementStack = MutableDefaultedMap<ElementType, Double>

fun ElementStack.format(): String = filter { (_, v) ->
    v != 0.0
}.map { (k, v) ->
    "${if (v == 1.0) "" else if (k.isDecimal) v.toString(1) else floor(v).roundToInt().toString()}${k.symbol}"
}.joinToString(" + ")

val ElementType.delta get() = Elements.symbolMap["${Symbols.delta}$symbol"]!!