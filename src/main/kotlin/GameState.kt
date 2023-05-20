import kotlin.math.max
import kotlin.math.min

class GameState {
    val elementAmounts: MutableDefaultedMap<ElementType, Double> = defaultElements
    var hoveredReaction: Reaction = NullReaction
    var incoming = defaultMutableStack
    var lastAmounts = elementAmounts
    var lastReaction = hoveredReaction
    var timeSpent: Double = 0.0
    val autoclickers = mutableListOf<Autoclicker>()

    fun tick(dt: Double, cap: Boolean = true) {
        lastAmounts = elementAmounts.copy().toMutableDefaultedMap(0.0)
        lastReaction = hoveredReaction
        timeSpent += dt

        autoclickers.forEach { if (DynamicHTMLManager.shownPage == Pages.id(it.page)) it.tick(dt) }

        elementAmounts.deduct(Elements.heat.withCount(elementAmounts[Elements.heat] * 0.1 * dt * Stats.gameSpeed))
        if (cap) for ((key, value) in incoming) elementAmounts[key] = min(
            Stats.functionalElementCaps[key], max(
                0.0,
                elementAmounts[key].plus(value)
            )
        )
        incoming = defaultMutableStack
    }

    fun canDoReaction(reaction: Reaction) =
        ((reaction !is NullReaction)
                && reaction.inputs.all { (k, v) -> v <= elementAmounts[k] }
                && reaction.multipliedOutputs.none { (k, v) -> if (k == Elements.heat) v + elementAmounts[k] - reaction.inputs[k] > Stats.functionalElementCaps[k] else v != 0.0 && elementAmounts[k] >= Stats.functionalElementCaps[k] })

    fun attemptReaction(reaction: Reaction) {
        if (canDoReaction(reaction)) {
            elementAmounts.deduct(reaction.multipliedInputs)
            incoming.add(reaction.multipliedOutputs)
            if (reaction is SpecialReaction) reaction.execute()
        }
    }

    override fun toString(): String {
        return super.toString()
    }
}