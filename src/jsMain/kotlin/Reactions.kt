open class Reaction(val name: String) {
    open var inputs: ElementStack = defaultStack.toDefaultedMap(0.0)
    open var outputs: ElementStack = defaultStack.toDefaultedMap(0.0)
    val multipliedInputs get() = inputs.toDefaultedMap(0.0)
    val multipliedOutputs get() = outputs.entries.associate { (k, v) -> k to v * Stats.elementMultipliers[k] }.toDefaultedMap(0.0)

    companion object {
        operator fun invoke(name: String, inputs: ElementStack, outputs: ElementStack) = object : Reaction(name) {
            override var inputs: ElementStack = inputs
            override var outputs: ElementStack = outputs
        }
    }

    override fun toString(): String {
        if (this is NullReaction) return "-"
        return "${multipliedInputs.format()} ⇒ ${multipliedOutputs.format()}"
    }
}

class SpecialReaction(name: String, val inputsSupplier: (Int) -> ElementStack, val outputsSupplier: (Int) -> ElementStack = { defaultStack }, val effects: (Int) -> Unit = { }, val stringEffects: (Int) -> String = { "" }, val usageCap: Int = 1): Reaction(name) {
    override var inputs: ElementStack
        get() = inputsSupplier(nTimesUsed + 1)
        set(_) {}
    override var outputs: ElementStack
        get() = outputsSupplier(nTimesUsed + 1)
        set(_) {}
    val hasBeenUsed get() = nTimesUsed >= usageCap
    var nTimesUsed = 0
    fun execute() {
        nTimesUsed++
        effects(nTimesUsed)
    }
}

class MilestoneReaction(
    name: String,
    val inputs: ElementStack,
    val outputs: ElementStack,
    val effects: () -> Unit,
    val stringEffects: (Int) -> String = { "" },
    val usageCap: Int = 1
) {

}

object NullReaction: Reaction("") {
    override var inputs: ElementStack = defaultStack
    override var outputs: ElementStack = defaultStack
}