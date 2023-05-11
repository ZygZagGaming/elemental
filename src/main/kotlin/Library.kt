@file:Suppress("unused")

import org.w3c.dom.ElementContentEditable
import kotlin.math.floor
import kotlin.math.roundToInt

val elements = listOf(
    ElementType("Catalyst", Symbols.catalyst),
    ElementType("Element A", Symbols.a),
    ElementType("Element B", Symbols.b),
    ElementType("Element C", Symbols.c),
    ElementType("Element D", Symbols.d),
    ElementType("Element E", Symbols.e),
    ElementType("Element F", Symbols.f),
    ElementType("Element G", Symbols.g),
    ElementType("Heat", Symbols.heat)
).associateBy { it.symbol }



open class Library<T> {
    val map get() = backingMap.toMap()
    val values get() = map.values
    private val backingMap = mutableMapOf<String, T>()
    protected fun register(id: String, elem: T): T {
        if (id !in backingMap) backingMap[id] = elem
        else throw Exception("Duplicate values in registry")
        return elem
    }
}

object SpecialReactions: Library<SpecialReaction>() {
    val clockStarted = register("clock_started",
        SpecialReaction(
            "Clock Started",
            {
                elementStackOf(
                    Elements.heat to 10.0,
                    Elements.b to 40.0
                )
            },
            effects = {
                Options.gameSpeed = 1.0
            },
            stringEffects = {
                "Game speed 0 → 1"
            }
        )
    )
    val clockwork = register("clockwork",
        SpecialReaction(
            "Clockwork",
            {
                elementStackOf(
                    Elements.a to 60.0 + 100.0 * it
                )
            },
            effects = {
                gameState.automationTickers["a_to_b"]!!.rateHertz = 2.0 * it
            },
            stringEffects = {
                "Automate \"A to B\" at a rate of ${2 * (it - 1)} → ${2 * it} per second"
            },
            usageCap = 100
        )
    )
    val massiveClock = register("massive_clock",
        SpecialReaction(
            "Massive Clock",
            {
                elementStackOf(
                    Elements.d to 4.0 * it
                )
            },
            effects = {
                GameTimer.registerTicker { dt ->
                    val multiplier = 0.1 * it
                    val catalysts = dt * Options.gameSpeed * multiplier * gameState.elementAmounts[Elements.d] + Stats.partialElements[Elements.catalyst]
                    gameState.incoming += elementStackOf(Elements.catalyst to floor(catalysts))
                    Stats.partialElements[Elements.catalyst] = catalysts.mod(1f)
                }
            },
            stringEffects = {
                val multiplier = 0.1 * it
                "Each \"${Elements.d.symbol}\" generates \"${Elements.catalyst.symbol}\" at ${(multiplier - 0.1).roundToOneDecimalPlace()} → ${multiplier.roundToOneDecimalPlace()} per second"
            },
            usageCap = 100
        )
    )
    val prestigious = register("prestigious",
        SpecialReaction(
            "Prestigious",
            {
                elementStackOf(
                    Elements.b to 1000.0 + if (it > 5) 200.0 * (it - 5) else 0.0
                )
            },
            effects = {
                Stats.elementMultipliers[Elements.b] = it.toDouble() + 1
            },
            stringEffects = {
                "\"${Elements.b.symbol}\" x${Stats.elementMultipliers[Elements.b]} → x${it + 1}"
            },
            usageCap = 100
        )
    )
    val overheat = register("overheat",
        SpecialReaction(
            "Overheat",
            {
                elementStackOf(
                    Elements.c to 4.0 * it
                )
            },
            effects = {
                Stats.elementCapMultipliers[Elements.heat] += 0.2
            },
            stringEffects = {
                "\"${Elements.heat.symbol}\" cap x${1 + (it - 1) * 0.2} → x${1 + it * 0.2}"
            },
            usageCap = 100
        )
    )
    val heatSink = register("heat_sink",
        SpecialReaction(
            "Heat Sink",
            {
                elementStackOf(
                    Elements.d to 20.0 + 2.0 * it
                )
            },
            effects = {
                NormalReactions.cminglyOp.inputs += elementStackOf(Elements.heat to 2.0)
                Unit
            },
            stringEffects = {
                "Heat cost on \"${NormalReactions.cminglyOp.name}\" ${8 + 2 * it} → ${10 + 2 * it}"
            },
            usageCap = 100
        )
    )
}

object NormalReactions: Library<Reaction>() {
    val aToB = register("a_to_b",
        Reaction(
            "A to B",
            elementStackOf(
                Elements.catalyst to 1.0,
                Elements.a to 1.0
            ),
            elementStackOf(
                Elements.b to 3.0
            )
        )
    )
    val bBackToA = register("b_back_to_a",
        Reaction(
            "B back to A",
            elementStackOf(
                Elements.b to 1.0
            ),
            elementStackOf(
                Elements.catalyst to 2.0,
                Elements.a to 1.0,
                Elements.heat to 0.5
            )
        )
    )
    val abcs = register("abcs",
        Reaction(
            "ABCs",
            elementStackOf(
                Elements.catalyst to 2.0,
                Elements.b to 16.0,
            ),
            elementStackOf(
                Elements.c to 1.0,
                Elements.heat to 5.0
            )
        )
    )
    val cminglyOp = register("cmingly_op",
        Reaction(
            "Cmingly OP",
            elementStackOf(
                Elements.heat to 8.0,
                Elements.c to 1.0,
            ),
            elementStackOf(
                Elements.a to 30.0,
                Elements.b to 10.0
            )
        )
    )
    val cataClysm = register("cataclysm",
        Reaction(
            "CataClysm",
            elementStackOf(
                Elements.c to 4.0,
            ),
            elementStackOf(
                Elements.catalyst to 40.0,
                Elements.heat to 9.0
            )
        )
    )
    val dscent = register("dscent",
        Reaction(
            "Dscent",
            elementStackOf(
                Elements.a to 120.0,
            ),
            elementStackOf(
                Elements.d to 1.0
            )
        )
    )
    val over900 = register("over900",
        Reaction(
            "Over 900",
            elementStackOf(
                Elements.b to 901.0,
            ),
            elementStackOf(
                Elements.d to 2.0
            )
        )
    )
}

object Elements: Library<ElementType>() {
    val catalyst = register("catalyst", ElementType("Catalyst", Symbols.catalyst))
    val a = register("element_a", ElementType("Element A", Symbols.a))
    val b = register("element_b", ElementType("Element B", Symbols.b))
    val c = register("element_c", ElementType("Element C", Symbols.c))
    val d = register("element_d", ElementType("Element D", Symbols.d))
    val e = register("element_e", ElementType("Element E", Symbols.e))
    val f = register("element_f", ElementType("Element F", Symbols.f))
    val g = register("element_g", ElementType("Element G", Symbols.g))
    val heat = register("heat", ElementType("Heat", Symbols.heat))
}

object Symbols: Library<Char>() {
    val catalyst = register("catalyst", 'ϟ')
    val a = register("a", 'a')
    val b = register("b", 'b')
    val c = register("c", 'c')
    val d = register("d", 'd')
    val e = register("e", 'e')
    val f = register("f", 'f')
    val g = register("g", 'g')
    val heat = register("heat", 'h')
}

object Options {
    var gameSpeed = 0.0
}

object Stats {
    val partialElements = mutableMapOf<ElementType, Double>().toMutableDefaultedMap(0.0)
    val elementMultipliers = mutableMapOf<ElementType, Double>().toMutableDefaultedMap(1.0)
    val elementCaps = elements.values.associateWith { 1000.0 }.toMutableMap().also {
        it[Elements.catalyst] = 100000.0
        it[Elements.heat] = 10.0
    }.toMutableDefaultedMap(Double.POSITIVE_INFINITY)
    val elementCapMultipliers = mutableMapOf<ElementType, Double>().toMutableDefaultedMap(1.0)
    val functionalElementCaps get() = Elements.values.associateWith { elementCaps[it] * elementCapMultipliers[it] }.toDefaultedMap(Double.POSITIVE_INFINITY)
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
}

fun <K, V> Map<K, V>.toDefaultedMap(value: V) = DefaultedMap(this, value)
fun <K, V> MutableMap<K, V>.toMutableDefaultedMap(value: V) = MutableDefaultedMap(this, value)

fun Double.roundToOneDecimalPlace() = (this * 10).roundToInt() / 10.0

fun <K, V> defaultedMapOf(defaultValue: V, vararg values: Pair<K, V>) = mapOf(*values).toDefaultedMap(defaultValue)
fun elementStackOf(vararg values: Pair<ElementType, Double>, defaultValue: Double = 0.0): ElementStack = defaultedMapOf(defaultValue, *values)

operator fun ElementStack.plus(other: ElementStack): ElementStack = entries.associate { (k, v) -> k to (v * other[k]) }.toDefaultedMap(0.0)

fun Map<ElementType, Double>.toElementStack(): ElementStack = toDefaultedMap(0.0)