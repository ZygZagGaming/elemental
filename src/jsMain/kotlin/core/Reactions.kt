package core

import libraries.Elements

open class Reaction(open var name: String, val consumesElements: Boolean = true) {
    open var inputs: ElementStack = emptyStack.toDefaultedMap(0.0)
    open var outputs: ElementStack = emptyStack.toDefaultedMap(0.0)
    val multipliedInputs get() = inputs.toDefaultedMap(0.0)
    val multipliedOutputs get() = outputs.entries.associate { (k, v) -> k to Stats.elementMultipliers[k].appliedTo(v) }.toDefaultedMap(0.0)

    companion object {
        operator fun invoke(name: String, inputs: ElementStack, outputs: ElementStack) = object : Reaction(name) {
            override var inputs: ElementStack = inputs
            override var outputs: ElementStack = outputs
        }

        operator fun invoke(name: String, inputsSupplier: () -> ElementStack, outputsSupplier: () -> ElementStack) = object : Reaction(name) {
            override var inputs: ElementStack
                get() = inputsSupplier()
            set(_) { }
            override var outputs: ElementStack
                get() = outputsSupplier()
            set(_) { }
        }
    }

    override fun toString(): String {
        if (this is NullReaction) return "-"
        return "${multipliedInputs.format()} ⇒ ${multipliedOutputs.format()}"
    }
}

class AlterableReaction(name: String, val inputsSupplier: (Int) -> ElementStack, val outputsSupplier: (Int) -> ElementStack, consumesElements: Boolean = true): Reaction(name, consumesElements) {
    var alterations = 0
    override var inputs: ElementStack
        get() = inputsSupplier(alterations)
        set(_) { }
    override var outputs: ElementStack
        get() = outputsSupplier(alterations)
        set(_) { }

    var baseName = name
    override var name: String
        get() = baseName + "*".repeat(alterations)
        set(value) {
            baseName = value
        }
}

open class SpecialReaction(
    name: String,
    val inputsSupplier: (Int) -> ElementStack,
    val outputsSupplier: (Int) -> ElementStack = { emptyStack },
    val effects: (Int, Boolean) -> Unit = { _, _ -> },
    val stringEffects: (Int) -> String = { "" },
    val undo: (Int) -> Unit = { },
    val usageCap: Int = 1,
    consumesElements: Boolean = true
): Reaction(name, consumesElements) {
    override var inputs: ElementStack
        get() = inputsSupplier(nTimesUsed + 1)
        set(_) {}
    override var outputs: ElementStack
        get() = outputsSupplier(nTimesUsed + 1)
        set(_) {}
    val hasBeenUsed get() = nTimesUsed >= usageCap
    var nTimesUsed = 0
    fun execute(offline: Boolean) {
        nTimesUsed++
        effects(nTimesUsed, offline)
    }

    fun undoEffects(refund: Boolean = false) {
        undo(nTimesUsed)
        if (refund) while (nTimesUsed > 0) {
            val inputs = inputsSupplier(nTimesUsed)
            val outputs = outputsSupplier(nTimesUsed)
            for (element in Elements.values) {
                Stats.elementAmounts[element] += outputs[element] - inputs[element]
            }
            nTimesUsed--
        } else {
            nTimesUsed = 0
        }
    }
}

class MilestoneReaction(
    name: String,
    inputsSupplier: (Int) -> ElementStack,
    outputsSupplier: (Int) -> ElementStack = { emptyStack },
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
    override var inputs: ElementStack = emptyStack
    override var outputs: ElementStack = emptyStack
}