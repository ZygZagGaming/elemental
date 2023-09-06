package core

import libraries.Elements
import libraries.Flag
import kotlin.math.pow

object Stats {
    val elementMultipliers = Elements.values.associateWith { basicMultiplierStack }.toMutableMap().toMutableDefaultedMap(
        basicMultiplierStack
    )
    val baseElementUpperBounds =
        BasicMutableStatMap(Elements.values.associateWith {
            when (it) {
                Elements.catalyst, Elements.omega, Elements.alpha -> 100000.0
                Elements.heat -> 10.0
                Elements.dualities -> Double.MAX_VALUE
                else -> if (it.isBasic) 1000.0 else 0.0
            }
        }, 1000.0)
    val elementUpperBoundMultipliers = BasicMutableStatMap(Elements.values.associateWith { basicMultiplierStack }, basicMultiplierStack)
    val functionalElementUpperBounds =
        ProductStatMap(baseElementUpperBounds, elementUpperBoundMultipliers) { a, b -> b.appliedTo(a) }
    val baseElementLowerBounds = BasicMutableStatMap<ElementType, Double>(emptyMap(), 0.0)
    val elementLowerBoundMultipliers = BasicMutableStatMap(Elements.values.associateWith { basicMultiplierStack }, basicMultiplierStack)
    val functionalElementLowerBounds =
        ProductStatMap(baseElementLowerBounds, elementLowerBoundMultipliers) { a, b -> b.appliedTo(a) }
    var gameSpeed = 0.0
    val elementAmounts = BasicMutableStatMap(defaultStartingElements, 0.0)
    var elementAmountsCached: MutableList<Map<ElementType, Double>> = mutableListOf()
    var elementDeltas = BasicMutableStatMap<ElementType, Double>(emptyMap(), 0.0)
    var elementRates = BasicMutableStatMap<ElementType, Double>(emptyMap(), 0.0)
    val tutorialsSeen = mutableSetOf<Tutorial>()
    //var elementDeltasUnspent = core.BasicMutableStatMap<core.ElementType, Double>(emptyMap(), 0.0)
    var lastTickDt = 0.0
    val flags = mutableSetOf<Flag>()
    val dualityThreshold = 100000
    var timeSinceLastDuality = 0.0
    var deltaReactionRespec = false

    fun resetDeltas() {
        Elements.values.forEach {
            if (it.isBasic) elementAmounts[it.delta] = functionalElementUpperBounds[it]
        }
    }

    fun addFlag(flag: Flag) {
        flags.add(flag)
    }

    fun removeFlag(flag: Flag) {
        flags.remove(flag)
    }

    fun hasFlag(flag: Flag): Boolean {
        return flag in flags
    }
}

val basicMultiplierStack get() = MultiplierStack()
class MultiplierStack {
    val shifts = mutableMapOf<String, () -> Double>()
    val multipliers = mutableMapOf<String, () -> Double>()
    val powers = mutableMapOf<String, () -> Double>()
    fun setMultiplier(name: String, value: () -> Double) {
        multipliers[name] = value
    }
    fun setMultiplier(name: String, value: Double) {
        multipliers[name] = { value }
    }
    fun removeMultiplier(name: String) {
        multipliers.remove(name)
    }

    fun setPower(name: String, value: () -> Double) {
        powers[name] = value
    }
    fun setPower(name: String, value: Double) {
        powers[name] = { value }
    }
    fun removePower(name: String) {
        powers.remove(name)
    }

    fun setShift(name: String, value: () -> Double) {
        shifts[name] = value
    }
    fun setShift(name: String, value: Double) {
        shifts[name] = { value }
    }
    fun removeShift(name: String) {
        shifts.remove(name)
    }

    fun appliedTo(n: Double): Double {
        val shift = shifts.values.fold(n) { acc, d -> acc + d() }
        val mult = multipliers.values.fold(shift) { acc, d -> acc * d() }
        return mult.pow(powers.values.fold(1.0) { acc, d -> acc * d() })
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun toggleDeltaReactionRespec() {
    Stats.deltaReactionRespec = !Stats.deltaReactionRespec
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun getFlags(): Array<String> {
    return Stats.flags.map { it.name }.toTypedArray()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun willRespecDeltaReactions(): Boolean {
    return Stats.deltaReactionRespec
}