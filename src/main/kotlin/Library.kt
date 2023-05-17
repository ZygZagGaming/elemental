@file:Suppress("unused")

import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.w3c.dom.Storage
import kotlin.js.Date
import kotlin.math.*

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
            {
                elementStackOf(
                    Elements.heat to 6.0
                )
            },
            effects = {
                Stats.gameSpeed = 1.0
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
                    Elements.a to 60.0 + 60.0 * it
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
                    val multiplier = 0.2 * it
                    val catalysts = dt * Stats.gameSpeed * multiplier * gameState.elementAmounts[Elements.d] + Stats.partialElements[Elements.catalyst]
                    gameState.incoming += elementStackOf(Elements.catalyst to floor(catalysts))
                    Stats.partialElements[Elements.catalyst] = catalysts.mod(1f)
                }
            },
            stringEffects = {
                val multiplier = 0.2 * it
                "Each \"${Elements.d.symbol}\" generates \"${Elements.catalyst.symbol}\" at ${(multiplier - 0.2).roundToOneDecimalPlace()} → ${multiplier.roundToOneDecimalPlace()} per second"
            },
            usageCap = 100
        )
    )
    val heatingUp = register("heating_up",
        SpecialReaction(
            "Heating Up",
            {
                elementStackOf(
                    Elements.b to 1000.0 + if (it <= 5) 0.0 else if (it <= 10) 200.0 * (it - 5) else 200.0 * (it - 5) * (it - 10)
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
                "Heat cap x${(1 + (it - 1) * 0.2).roundToOneDecimalPlace()} → x${(1 + it * 0.2).roundToOneDecimalPlace()}"
            },
            usageCap = 100
        )
    )
    val heatSink = register("heat_sink",
        SpecialReaction(
            "Heat Sink",
            {
                elementStackOf(
                    Elements.d to 4.0 + 2.0 * it,
                    Elements.heat to 8.0 + 2.0 * it
                )
            },
            effects = {
                NormalReactions.cminglyOp.inputs += elementStackOf(Elements.heat to 2.0)
                Unit
            },
            stringEffects = {
                "Heat cost on \"${NormalReactions.cminglyOp.name}\" ${6 + 2 * it} → ${8 + 2 * it}"
            },
            usageCap = 100
        )
    )
    val doubleBCap = register("double_b_cap",
        SpecialReaction(
            "ExponEntial",
            {
                elementStackOf(
                    Elements.e to 2.0.pow(it),
                    Elements.b to 250.0 * 4.0.pow(it)
                )
            },
            effects = {
                val multiplier = it.squared()
                GameTimer.registerNamedTicker("double_b_cap") {
                    Stats.elementCapMultipliers[Elements.b] = (gameState.elementAmounts[Elements.e] + 1) * multiplier
                }
                Unit
            },
            stringEffects = {
                if (it == 1) "Multiplier to \"${Elements.b.symbol}\" cap equal to \"${Elements.e.symbol}\" count (plus 1)"
                else "Multiplier to \"${Elements.b.symbol}\" cap equal to \"${Elements.e.symbol}\" count (plus 1) x${(it - 1).squared()} → x${it.squared()}"
            },
            usageCap = 100
        )
    )
    val onEfficiency = register("on_efficiency",
        SpecialReaction(
            "On Efficiency",
            {
                elementStackOf(
                    Elements.catalyst to 5000.0 * it,
                    Elements.b to 4000.0 * it
                )
            },
            effects = {
                Stats.reactionEfficiencies[NormalReactions.bBackToA] = 2.0 * it
            },
            stringEffects = {
                "\"${NormalReactions.bBackToA.name}\" reaction efficiency x${if (it == 1) 1 else 2 * it - 2} → x${2 * it}"
            },
            usageCap = 100
        )
    )
    val noneLeft = register("none_left",
        SpecialReaction(
            "None Left",
            {
                elementStackOf(
                    Elements.catalyst to 100000.0 * (if (it <= 5) 1.0 else (it - 5.0) * (it - 5))
                )
            },
            effects = {
                Stats.elementMultipliers[Elements.a] = (it + 1.0) * (it + 1)
                Stats.elementCapMultipliers[Elements.a] = (it + 1.0) * (it + 1)
            },
            stringEffects = {
                "\"${Elements.a.symbol}\" and \"${Elements.a.symbol}\" cap x${it * it} → x${(it + 1) * (it + 1)}"
            },
            usageCap = 100
        )
    )
}

fun Double.squared() = this * this
fun Int.squared() = this * this

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
//    val cataClysm = register("cataclysm",
//        Reaction(
//            "CataClysm",
//            elementStackOf(
//                Elements.c to 4.0,
//            ),
//            elementStackOf(
//                Elements.catalyst to 40.0,
//                Elements.heat to 9.0
//            )
//        )
//    )
//    val dscent = register("dscent",
//        Reaction(
//            "Dscent",
//            elementStackOf(
//                Elements.a to 120.0,
//            ),
//            elementStackOf(
//                Elements.d to 2.0
//            )
//        )
//    )
    val over900 = register("over900",
        Reaction(
            "Over 900",
            elementStackOf(
                Elements.b to 950.0,
            ),
            elementStackOf(
                Elements.d to 3.0
            )
        )
    )
    val exotherm = register("exotherm",
        Reaction(
            "Exotherm",
            elementStackOf(
                Elements.c to 20.0,
                Elements.catalyst to 1000.0
            ),
            elementStackOf(
                Elements.e to 1.0,
                Elements.heat to 20.0
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
    var saveInterval = 5.0
    var saveMode = SaveMode.LOCAL_STORAGE
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
    val reactionEfficiencies = mutableMapOf<Reaction, Double>().toMutableDefaultedMap(1.0)
    var gameSpeed = 0.0
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
    return min(vh(percent), vw(percent));
}

fun vmax(percent: Double): Double {
    return max(vh(percent), vw(percent));
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

fun save(saveMode: SaveMode = Options.saveMode) {
    when (saveMode) {
        SaveMode.LOCAL_STORAGE -> saveLocalStorage()
    }
}

fun load(saveMode: SaveMode = Options.saveMode) {
    when (saveMode) {
        SaveMode.LOCAL_STORAGE -> loadLocalStorage()
    }
}

fun saveLocalStorage() {
    console.log("Saving game to local storage...")
    document.apply {
        localStorage["elementAmts"] = Elements.map.map { (k, v) -> "$k:${gameState.elementAmounts[v]}" }.joinToString(separator = ",")
        localStorage["reactionAmts"] = SpecialReactions.map.map { (k, v) -> "$k:${v.nTimesUsed}" }.joinToString(separator = ",")
        localStorage["timestamp"] = Date().toDateString()
        localStorage["timeSpent"] = gameState.timeSpent.toString()
    }
}

fun loadLocalStorage() {
    console.log("Loading game from local storage...")
    document.apply {
        val timestamp = localStorage["timestamp"]
        if (timestamp != "") {
            localStorage["reactionAmts"].split(',').forEach {
                val pair = it.split(':')
                val reaction = SpecialReactions.map[pair[0]]
                if (reaction != null) repeat(pair[1].toInt()) { reaction.execute() }
            }
            localStorage["elementAmts"].split(',').forEach {
                val pair = it.split(':')
                val element = Elements.map[pair[0]]
                if (element != null) gameState.elementAmounts[element] = pair[1].toDouble()
            }
            gameState.timeSpent = localStorage["timeSpent"].toDouble()
            simulateTime((Date().getUTCMilliseconds() - Date(timestamp).getUTCMilliseconds()) / 1000.0)
        }
    }
}

fun simulateTime(dt: Double) {
    gameState.tick(dt)
}