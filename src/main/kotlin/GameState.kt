import kotlinx.browser.document
import org.w3c.dom.get
import kotlin.math.max
import kotlin.math.min

class GameState {
    var hoveredReaction: Reaction = NullReaction
    var incoming = defaultMutableStack
    var lastReaction = hoveredReaction
    var timeSpent: Double = 0.0
    val clickersById = mutableMapOf<Int, Clicker>()

    fun tick(dt: Double, firstTick: Boolean = false) {
        Input.tick(dt)
        Stats.elementDeltas = Elements.values.associateWith { k ->
            max(if (Stats.elementDeltas[k].isNaN()) Double.NEGATIVE_INFINITY else Stats.elementDeltas[k], (Stats.elementAmounts[k] - Stats.elementAmountsLastTick[k]) / Stats.lastTickDt)
        }.toDefaultedMap(0.0)
        console.log((Stats.elementAmounts[Elements.b] - Stats.elementAmountsLastTick[Elements.b]))

        lastReaction = hoveredReaction
        timeSpent += dt

        clickersById.values.forEach { if (DynamicHTMLManager.shownPage == Pages.id(it.page)) it.tick(dt) }

        Stats.elementAmounts.deduct(Elements.heat.withCount(Stats.elementAmounts[Elements.heat] * 0.1 * dt * Stats.gameSpeed))
        if (!firstTick) for ((key, value) in incoming) Stats.elementAmounts[key] = min(
            Stats.functionalElementCaps[key], max(
                0.0,
                Stats.elementAmounts[key].plus(value)
            )
        )
        Stats.elementAmountsLastTick = Stats.elementAmounts.copy()
        incoming = defaultMutableStack
        Stats.lastTickDt = dt
    }

    fun canDoReaction(reaction: Reaction) =
        ((reaction !is NullReaction)
                && reaction.inputs.all { (k, v) -> v <= Stats.elementAmounts[k] }
                && reaction.multipliedOutputs.none { (k, v) -> if (k == Elements.heat) v + Stats.elementAmounts[k] - reaction.inputs[k] > Stats.functionalElementCaps[k] else v != 0.0 && Stats.elementAmounts[k] >= Stats.functionalElementCaps[k] })

    fun attemptReaction(reaction: Reaction) {
        if (canDoReaction(reaction)) {
            Stats.elementAmounts.deduct(reaction.multipliedInputs)
            incoming.add(reaction.multipliedOutputs)
            if (reaction is SpecialReaction) reaction.execute()
        }
    }

    override fun toString(): String {
        return super.toString()
    }

    fun addClicker(clicker: Clicker) {
        val old = clickersById[clicker.id]
        old?.deInit()
        clickersById[clicker.id] = clicker
        clicker.init()
        fixClickerDockOrder()
        clicker.moveToDock(force = true)
    }

    fun fixClickerDockOrder() {
        val fragment = document.createDocumentFragment()
        clickersById.entries.sortedBy { (id, _) -> id }.forEach { (_, clicker) -> fragment.appendChild(clicker.dock) }
        document.getElementsByClassName("clicker-dock-container")[0]!!.appendChild(fragment)
    }

    fun removeClicker(clicker: Clicker) {
        clickersById.remove(clicker.id)
        clicker.deInit()
    }
}