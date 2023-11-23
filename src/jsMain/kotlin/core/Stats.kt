package core

import libraries.Resources
import libraries.Flag
import kotlin.math.pow

object Stats {
    val elementMultipliers = Resources.values.associateWith { basicMultiplierStack }.toMutableMap().toMutableDefaultedMap(
        basicMultiplierStack
    )
    val baseElementUpperBounds =
        BasicMutableStatMap(Resources.values.associateWith {
            when (it) {
                Resources.catalyst, Resources.omega, Resources.alpha -> 100000.0
                Resources.heat -> 10.0
                Resources.dualities -> Double.MAX_VALUE
                else -> if (it.isElement) 1000.0 else 0.0
            }
        }, 1000.0)
    val elementUpperBoundMultipliers = BasicMutableStatMap(Resources.values.associateWith { basicMultiplierStack }, basicMultiplierStack)
    val functionalElementUpperBounds =
        ProductStatMap(baseElementUpperBounds, elementUpperBoundMultipliers) { a, b -> b.appliedTo(a) }
    val baseElementLowerBounds = BasicMutableStatMap<Resource, Double>(emptyMap(), 0.0)
    val elementLowerBoundMultipliers = BasicMutableStatMap(Resources.values.associateWith { basicMultiplierStack }, basicMultiplierStack)
    val functionalElementLowerBounds =
        ProductStatMap(baseElementLowerBounds, elementLowerBoundMultipliers) { a, b -> b.appliedTo(a) }
    var gameSpeed = 0.0
    val elementAmounts = BasicMutableStatMap(defaultStartingElements, 0.0)
    val elementPercentages = ProductStatMap(
        ProductStatMap(elementAmounts, functionalElementLowerBounds) { a, b -> a - b },
        ProductStatMap(functionalElementUpperBounds, functionalElementLowerBounds) { a, b -> a - b }
    ) { a, b -> a / b }
    var elementAmountsCached: MutableList<Pair<Map<Resource, Double>, Double>> = mutableListOf()
    var elementDeltas = BasicMutableStatMap<Resource, Double>(emptyMap(), 0.0)
    var elementRates = BasicMutableStatMap<Resource, Double>(emptyMap(), 0.0)
    val tutorialsSeen = mutableSetOf<Tutorial>()
    //var elementDeltasUnspent = core.BasicMutableStatMap<core.ElementType, Double>(emptyMap(), 0.0)
    var lastTickDt = 0.0
    val flags = mutableSetOf<Flag>()
    val dualityThreshold = 100000
    var timeSinceLastDuality = 0.0
    var deltaReactionRespec = false
    val alphaThreshold = 0.99
    val omegaThreshold = 0.01

    fun resetDeltas() {
        Resources.values.forEach {
            if (it.isElement) elementAmounts[it.delta] = functionalElementUpperBounds[it]
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

    fun parallel(other: MultiplierStack): MultiplierStack {
        val stack = MultiplierStack()

        for ((k, v) in shifts) stack.setShift(k, v)
        for ((k, v) in multipliers) stack.setMultiplier(k, v)
        for ((k, v) in powers) stack.setPower(k, v)

        for ((k, v) in other.shifts) stack.setShift(k, v)
        for ((k, v) in other.multipliers) stack.setMultiplier(k, v)
        for ((k, v) in other.powers) stack.setPower(k, v)
        return stack
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