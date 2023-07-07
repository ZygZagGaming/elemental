@file:Suppress("unused")

import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.w3c.dom.Storage
import kotlin.js.Date
import kotlin.math.*

open class Library<T> {
    val map get() = backingMap.toMap()
    val values get() = map.values
    private val backingMap = mutableMapOf<String, T>()
    protected fun register(id: String, elem: T): T {
        if (id !in backingMap) backingMap[id] = elem
        else throw Exception("Duplicate values in registry")
        return elem
    }

    fun id(elem: T): String? {
        return map.entries.firstOrNull { it.value == elem }?.key
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
                if (it == 1) "Game speed x0 → x1"
                else "Game speed x1"
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
                when (it) {
                    1 -> {
                        repeat(5) { n -> gameState.addClicker(Clicker(n + 1, Pages.elementsPage, ClickerMode.MANUAL, 4.0, 6.0)) }
                    }
                    2 -> {
                        gameState.clickersById[1]!!.modesUnlocked.add(ClickerMode.AUTO)
                    }
                    3 -> {
                        gameState.clickersById[2]!!.modesUnlocked.add(ClickerMode.AUTO)
                        gameState.clickersById[3]!!.modesUnlocked.add(ClickerMode.AUTO)
                    }
                    else -> {
                        repeat(3) { n -> gameState.clickersById[n + 1]!!.autoCps += 0.5 }
                    }
                }
            },
            stringEffects = {
                when (it) {
                    1 -> "Unlock Clickers 1-5"
                    2 -> "Unlock Auto Mode on Clicker 1 with a click rate of 4 Hz"
                    3 -> "Unlock Auto Mode on Clickers 2 and 3 with click rates of 4 Hz"
                    else -> "All Clicker autoclick rates ${it / 2.0 + 2} → ${it / 2.0 + 2.5} Hz"
                }
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
                GameTimer.registerTicker("massiveClockTicker") { dt ->
                    val multiplier = 0.2 * it
                    val catalysts = max(0.0, min(dt * Stats.gameSpeed * multiplier * Stats.elementAmounts[Elements.d], Stats.functionalElementUpperBounds[Elements.catalyst] * .99 - Stats.elementAmounts[Elements.catalyst]))
                    gameState.incoming.add(elementStackOf(Elements.catalyst to catalysts))
                }
            },
            stringEffects = {
                val multiplier = 0.2 * it
                "Each \"${Elements.d.symbol}\" generates \"${Elements.catalyst.symbol}\" at ${(multiplier - 0.2).roundToOneDecimalPlace()} → ${multiplier.roundToOneDecimalPlace()} per second (until 99% of cap)"
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
                "\"${Elements.b.symbol}\" production x${Stats.elementMultipliers[Elements.b]} → x${it + 1}"
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
                Stats.elementUpperBoundMultipliers[Elements.heat] += 0.2
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
                    Elements.d to 4.0 + it * it,
                    Elements.heat to 2.0 * (it + 1) * (it + 1)
                )
            },
            effects = {
                NormalReactions.cminglyOp.apply {
                    inputs = inputs.mutateDefaulted { map ->
                        map[Elements.heat] = 2.0 * (it + 2) * (it + 2)
                    }
                }
            },
            stringEffects = {
                "Heat cost on \"${NormalReactions.cminglyOp.name}\" ${2 * it * it} → ${2 * (it + 1) * (it + 1)}"
            },
            usageCap = 100
        )
    )
    val exponEntial = register("expon_ential",
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
                GameTimer.registerTicker("double_b_cap") {
                    Stats.elementUpperBoundMultipliers[Elements.b] = (Stats.elementAmounts[Elements.e] + 1) * multiplier
                }
            },
            stringEffects = {
                if (it == 1) "Multiplier to \"${Elements.b.symbol}\" cap equal to \"${Elements.e.symbol}\" count (plus 1)"
                else "Multiplier to \"${Elements.b.symbol}\" cap equal to \"${Elements.e.symbol}\" count (+ 1) x${(it - 1).squared()} → x${it.squared()}"
            },
            usageCap = 100
        )
    )
    val infoNerd = register("info_nerd",
        SpecialReaction(
            "Info Nerd",
            {
                elementStackOf(
                    Elements.heat to 20.0,
                    Elements.b to 2000.0
                )
            },
            effects = {
                DynamicHTMLManager.addElementClass("heat-element", "shown")
            },
            stringEffects = {
                "Shows numeric values for heat"
            }
        )
    )
//    val onEfficiency = register("on_efficiency",
//        SpecialReaction(
//            "On Efficiency",
//            {
//                elementStackOf(
//                    Elements.catalyst to 5000.0 * it,
//                    Elements.b to 4000.0 * it
//                )
//            },
//            effects = {
//                Stats.reactionEfficiencies[NormalReactions.bBackToA] = 2.0 * it
//            },
//            stringEffects = {
//                "\"${NormalReactions.bBackToA.name}\" reaction efficiency x${if (it == 1) 1 else 2 * it - 2} → x${2 * it}"
//            },
//            usageCap = 100
//        )
//    )
//    val noneLeft = register("none_left",
//        SpecialReaction(
//            "None Left",
//            {
//                elementStackOf(
//                    Elements.catalyst to 100000.0 * (if (it <= 5) 1.0 else (it - 5.0) * (it - 5))
//                )
//            },
//            effects = {
//                Stats.elementMultipliers[Elements.a] = (it + 1.0) * (it + 1)
//                Stats.elementCapMultipliers[Elements.a] = (it + 1.0) * (it + 1)
//            },
//            stringEffects = {
//                "\"${Elements.a.symbol}\" production and \"${Elements.a.symbol}\" cap x${it * it} → x${(it + 1) * (it + 1)}"
//            },
//            usageCap = 100
//        )
//    )
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
    val symbolMap get() = values.associateBy { it.symbol }

    val catalyst = register("catalyst", ElementType("Catalyst", Symbols.catalyst))
    val a = register("element_a", ElementType("Element A", Symbols.a))
    val b = register("element_b", ElementType("Element B", Symbols.b))
    val c = register("element_c", ElementType("Element C", Symbols.c))
    val d = register("element_d", ElementType("Element D", Symbols.d))
    val e = register("element_e", ElementType("Element E", Symbols.e))
    val f = register("element_f", ElementType("Element F", Symbols.f))
    val g = register("element_g", ElementType("Element G", Symbols.g))
    val heat = register("heat", ElementType("Heat", Symbols.heat, isDecimal = true))
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
    var autoclickerDockSnapDistance = 50.0
}

object Stats {
    val elementMultipliers = mutableMapOf<ElementType, Double>().toMutableDefaultedMap(1.0)
    val baseElementUpperBounds = Elements.values.associateWith { 1000.0 }.toMutableMap().also {
        it[Elements.catalyst] = 100000.0
        it[Elements.heat] = 10.0
    }.toMutableDefaultedMap(Double.POSITIVE_INFINITY)
    val elementUpperBoundMultipliers = mutableMapOf<ElementType, Double>().toMutableDefaultedMap(1.0)
    val functionalElementUpperBounds get() = Elements.values.associateWith { baseElementUpperBounds[it] * elementUpperBoundMultipliers[it] }.toDefaultedMap(Double.POSITIVE_INFINITY)
    val baseElementLowerBounds = Elements.values.associateWith { 0.0 }.toMutableMap().toMutableDefaultedMap(Double.POSITIVE_INFINITY)
    val elementLowerBoundMultipliers = mutableMapOf<ElementType, Double>().toMutableDefaultedMap(1.0)
    val functionalElementLowerBounds get() = Elements.values.associateWith { baseElementLowerBounds[it] * elementLowerBoundMultipliers[it] }.toDefaultedMap(Double.NEGATIVE_INFINITY)
    val reactionEfficiencies = mutableMapOf<Reaction, Double>().toMutableDefaultedMap(1.0)
    var gameSpeed = 0.0
    val elementAmounts: MutableElementStack = defaultElements
    var elementAmountsCached: MutableList<ElementStack> = mutableListOf()
    var elementDeltas: MutableElementStack = mutableDefaultedMapOf(0.0)
    var elementRates: MutableElementStack = mutableDefaultedMapOf(0.0)
    var elementDeltasUnspent: MutableElementStack = mutableDefaultedMapOf(0.0)
    var lastTickDt = 0.0

    fun resetDeltas() {
        elementDeltasUnspent = elementDeltas
    }
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
        localStorage["elementAmts"] = Elements.map.map { (k, v) -> "$k:${Stats.elementAmounts[v]}" }.joinToString(separator = ",")
        localStorage["reactionAmts"] = SpecialReactions.map.map { (k, v) -> "$k:${v.nTimesUsed}" }.joinToString(separator = ",")
        localStorage["timestamp"] = Date().toDateString()
        localStorage["timeSpent"] = gameState.timeSpent.toString()
        localStorage["autoclickerPositions"] = gameState.clickersById.map { (id, clicker) -> "${id}:${if (clicker.docked) "docked" else "${clicker.htmlElement.x.pxToVw},${clicker.htmlElement.y.pxToVw}"}" }.joinToString(separator = ";")
        localStorage["elementDeltas"] = Elements.map.map { (k, v) -> "$k:${Stats.elementDeltas[v]}" }.joinToString(separator = ",")
        localStorage["elementDeltasUnspent"] = Elements.map.map { (k, v) -> "$k:${Stats.elementDeltasUnspent[v]}" }.joinToString(separator = ",")
        localStorage["autoclickerSettings"] = gameState.clickersById.map { (id, clicker) -> "${id}:(${clicker.mode},${Input.keybinds["keyclicker-$id"]!!.key.key})" }.joinToString(separator = ";")
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
                if (element != null) Stats.elementAmounts[element] = pair[1].toDouble()
            }
            gameState.timeSpent = localStorage["timeSpent"].toDouble()
            simulateTime((Date().getUTCMilliseconds() - Date(timestamp).getUTCMilliseconds()) / 1000.0)
            val positions = localStorage["autoclickerPositions"].split(";").filter { it != "" }.associate {
                val pair = it.split(":")
                val pair2 = pair[1].split(",")
                pair[0].toInt() to (if (pair2.size == 2) pair2[0] to pair2[1] else null)
            }
            gameState.clickersById.forEach { (id, it) ->
                val pos = positions[id]
                if (pos != null) {
                    it.htmlElement.style.left = pos.first
                    it.htmlElement.style.top = pos.second
                    it.docked = false
                    it.canvasParent.style.left = pos.first
                    it.canvasParent.style.top = pos.second
                } else {
                    it.moveToDock(force = true)
                }
            }
            localStorage["elementDeltas"].split(',').forEach {
                val pair = it.split(':')
                val element = Elements.map[pair[0]]
                if (element != null) Stats.elementDeltas[element] = pair[1].toDouble()
            }
            localStorage["elementDeltasUnspent"].split(',').forEach {
                val pair = it.split(':')
                val element = Elements.map[pair[0]]
                if (element != null) Stats.elementDeltasUnspent[element] = pair[1].toDouble()
            }
            localStorage["autoclickerSettings"].split(';').forEach {
                val pair = it.split(':')
                if (pair.size == 2) {
                    val pair2 = pair[1].trim('(', ')').split(',')
                    val clicker = gameState.clickersById[pair[0].toInt()]
                    if (clicker != null) {
                        clicker.setMode(ClickerMode.valueOf(pair2[0]))
                        Input.keybinds["keyclicker-${pair[0]}"]!!.key = Key(pair2[1])
                    }
                }
            }
        }
    }
}

fun simulateTime(dt: Double) {
    gameState.tick(dt, offline = true)
    GameTimer.tick(dt)
}

data class Page(val name: String)

object Pages: Library<Page>() {
    val elementsPage = register("elements", Page("Elements"))
    val optionsPage = register("options", Page("Options"))
    val capsPage = register("duality", Page("Duality"))
}