package core

import libraries.Resources
import kotlin.properties.Delegates

open class Reaction(open var name: String, val consumesElements: Boolean = true) {
    open var inputs = emptyStack.toDefaultedMap(0.0)
        set(value) {
            for ((key, _) in field) {
                Stats.elementAmounts.removeListener(key, "$name input")
            }
            field = value
            for ((key, v) in field) {
                Stats.elementAmounts.addThresholdListener(key, "$name input", v) { _ ->
                    computeString()
                }
            }
        }
    open var outputs = emptyStack.toDefaultedMap(0.0)
        set(value) {
            for ((key, _) in field) {
                Stats.elementAmounts.removeListener(key, "$name output")
                Stats.elementMultipliers.removeListener(key, "$name output")
            }
            field = value
            for ((key, v) in field) {
                Stats.elementAmounts.addListener(key, "$name output") { _, _ ->
                    computeString()
                }
                Stats.elementMultipliers.addListener(key, "$name output") { old, new ->
                    val amt = Stats.elementAmounts[key]
                    if (old == null || (old.appliedTo(amt) > v) != (new.appliedTo(amt) > v)) computeString()
                }
            }
        }
    var changed = true
    val multipliedInputs get() = inputs.toDefaultedMap(0.0)
    val multipliedOutputs get() = outputs.entries.associate { (k, v) -> k to Stats.elementMultipliers[k].appliedTo(v) }.toDefaultedMap(0.0)

    companion object {
        operator fun invoke(name: String, inputs: ResourceStack, outputs: ResourceStack) = object : Reaction(name) {
            init {
                for ((key, v) in inputs) {
                    Stats.elementAmounts.addThresholdListener(key, "$name input", v) { _ ->
                        computeString()
                    }
                }

                for ((key, v) in outputs) {
                    Stats.elementAmounts.addListener(key, "$name output") { _, _ ->
                        computeString()
                    }
                    Stats.elementMultipliers.addListener(key, "$name output") { old, new ->
                        val amt = Stats.elementAmounts[key]
                        if (old == null || (old.appliedTo(amt) > v) != (new.appliedTo(amt) > v)) computeString()
                    }
                }
            }

            override var inputs: ResourceStack = inputs
                set(value) {
                    for ((key, _) in field) {
                        Stats.elementAmounts.removeListener(key, "$name input")
                    }
                    field = value
                    for ((key, v) in field) {
                        Stats.elementAmounts.addThresholdListener(key, "$name input", v) { _ ->
                            computeString()
                        }
                    }
                }
            override var outputs: ResourceStack = outputs
                set(value) {
                    for ((key, _) in field) {
                        Stats.elementAmounts.removeListener(key, "$name output")
                        Stats.elementMultipliers.removeListener(key, "$name output")
                    }
                    field = value
                    for ((key, v) in field) {
                        Stats.elementAmounts.addListener(key, "$name output") { _, _ ->
                            computeString()
                        }
                        Stats.elementMultipliers.addListener(key, "$name output") { old, new ->
                            val amt = Stats.elementAmounts[key]
                            if (old == null || (old.appliedTo(amt) > v) != (new.appliedTo(amt) > v)) computeString()
                        }
                    }
                }
        }
    }

    var cachedToString = ""
    override fun toString(): String {
        if (this is NullReaction) return "-"
        if (changed) {
            changed = false
            computeString()
        }
        return cachedToString
    }
    open fun computeString() {
        cachedToString = "${multipliedInputs.formatReactionInputs()} ⇒ ${multipliedOutputs.formatReactionOutputs()}"
    }
}

class AlterableReaction(name: String, val inputsSupplier: (Int) -> ResourceStack, val outputsSupplier: (Int) -> ResourceStack, consumesElements: Boolean = true): Reaction(name, consumesElements) {
    var alterations: Int by Delegates.observable(0) { _, _, _ ->
        for ((key, _) in inputs) {
            Stats.elementAmounts.removeListener(key, "$baseName input")
        }
        for ((key, _) in outputs) {
            Stats.elementAmounts.removeListener(key, "$baseName output")
            Stats.elementMultipliers.removeListener(key, "$baseName output")
        }
        for ((key, value) in inputs) {
            Stats.elementAmounts.addThresholdListener(key, "$baseName input", value) { _ ->
                computeString()
            }
        }
        for ((key, value) in outputs) {
            Stats.elementAmounts.addListener(key, "$baseName output") { _, _ ->
                computeString()
            }
            Stats.elementMultipliers.addListener(key, "$baseName output") { old, new ->
                val amt = Stats.elementAmounts[key]
                if (old == null || (old.appliedTo(amt) > value) != (new.appliedTo(amt) > value)) computeString()
            }
        }
    }
    override var inputs: ResourceStack
        get() = inputsSupplier(alterations)
        set(_) { }
    override var outputs: ResourceStack
        get() = outputsSupplier(alterations)
        set(_) { }

    var baseName = name

    init {
        for ((key, value) in inputs) {
            Stats.elementAmounts.addThresholdListener(key, "$name input", value) { _ ->
                computeString()
            }
        }
        for ((key, value) in outputs) {
            Stats.elementAmounts.addListener(key, "$name output") { _, _ ->
                computeString()
            }
            Stats.elementMultipliers.addListener(key, "$name output") { old, new ->
                val amt = Stats.elementAmounts[key]
                if (old == null || (old.appliedTo(amt) > value) != (new.appliedTo(amt) > value)) computeString()
            }
        }
    }
    override var name: String
        get() = baseName + "*".repeat(alterations)
        set(value) {
            baseName = value
        }
}

open class SpecialReaction(
    name: String,
    val inputsSupplier: (Int) -> ResourceStack,
    val outputsSupplier: (Int) -> ResourceStack = { emptyStack },
    val effects: (Int, Boolean) -> Unit = { _, _ -> },
    val stringEffects: (Int) -> String = { "" },
    val undo: (Int) -> Unit = { },
    val usageCap: Int = 1,
    consumes: Boolean = true
): Reaction(name, consumes) {
    override var inputs: ResourceStack
        get() = inputsSupplier(nTimesUsed + 1)
        set(value) {}
    override var outputs: ResourceStack
        get() = outputsSupplier(nTimesUsed + 1)
        set(_) {}
    val hasBeenUsed get() = nTimesUsed >= usageCap
    var nTimesUsed: Int by Delegates.observable(0) { _, _, _ ->
        for ((key, _) in inputs) {
            Stats.elementAmounts.removeListener(key, "$name input")
        }
        for ((key, _) in outputs) {
            Stats.elementAmounts.removeListener(key, "$name output")
            Stats.elementMultipliers.removeListener(key, "$name output")
        }
        for ((key, value) in inputs) {
            Stats.elementAmounts.addThresholdListener(key, "$name input", value) { _ ->
                computeString()
            }
        }
        for ((key, value) in outputs) {
            Stats.elementAmounts.addListener(key, "$name output") { _, _ ->
                computeString()
            }
            Stats.elementMultipliers.addListener(key, "$name output") { old, new ->
                val amt = Stats.elementAmounts[key]
                if (old == null || (old.appliedTo(amt) > value) != (new.appliedTo(amt) > value)) computeString()
            }
        }
    }
    fun execute(offline: Boolean) {
        nTimesUsed++
        effects(nTimesUsed, offline)
    }

    init {
        for ((key, value) in inputsSupplier(nTimesUsed + 1)) {
            Stats.elementAmounts.addThresholdListener(key, "$name input", value) { _ ->
                computeString()
            }
        }
        for ((key, value) in outputsSupplier(nTimesUsed + 1)) {
            Stats.elementAmounts.addListener(key, "$name output") { _, _ ->
                computeString()
            }
            Stats.elementMultipliers.addListener(key, "$name output") { old, new ->
                val amt = Stats.elementAmounts[key]
                if (old == null || (old.appliedTo(amt) > value) != (new.appliedTo(amt) > value)) computeString()
            }
        }
    }

    fun undoEffects(refund: Boolean = false) {
        undo(nTimesUsed)
        if (refund) while (nTimesUsed > 0) {
            val inputs = inputsSupplier(nTimesUsed)
            val outputs = outputsSupplier(nTimesUsed)
            for (element in Resources.values) {
                Stats.elementAmounts[element] += outputs[element] - inputs[element]
            }
            nTimesUsed--
        } else {
            nTimesUsed = 0
        }
    }

    override fun toString(): String {
        if (changed) {
            changed = false
            computeString()
        }
        return cachedToString
    }

    override fun computeString() {
        cachedToString = "${if (hasBeenUsed) multipliedInputs.format() else multipliedInputs.formatReactionInputs()} ⇒ ${if (hasBeenUsed) multipliedOutputs.format() else multipliedOutputs.formatReactionOutputs()}"
    }
}

class MilestoneReaction(
    name: String,
    inputsSupplier: (Int) -> ResourceStack,
    outputsSupplier: (Int) -> ResourceStack = { emptyStack },
    effects: (Int, Boolean) -> Unit = { _, _ -> },
    stringEffects: (Int) -> String = { "" },
    undo: (Int) -> Unit = { },
    usageCap: Int = 1
): SpecialReaction(
    name,
    inputsSupplier,
    outputsSupplier,
    effects,
    stringEffects,
    undo,
    usageCap,
    false
)

object NullReaction: Reaction("") {
    override var inputs: ResourceStack = emptyStack
    override var outputs: ResourceStack = emptyStack
}