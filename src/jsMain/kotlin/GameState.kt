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
    val timeBetweenRateTicks = 0.25

    fun tick(dt: Double, offline: Boolean = false) {
        Input.tick(dt)

        lastReaction = hoveredReaction
        timeSpent += dt

        clickersById.values.forEach { if (DynamicHTMLManager.shownPage == Pages.id(it.page)) it.tick(dt) }

        Stats.elementAmounts[Elements.heat] *= (1 - 0.1 * dt * Stats.gameSpeed)
        if (!offline) {
            for ((key, value) in incoming) Stats.elementAmounts[key] = min(
                Stats.functionalElementUpperBounds[key], max(
                    0.0,
                    Stats.elementAmounts[key].plus(value)
                )
            )
        }
        if ((timeSpent - dt).mod(timeBetweenRateTicks) > timeSpent.mod(timeBetweenRateTicks) || offline) {
            if (!offline) Elements.values.forEach {
                Stats.elementRates[it] = (Stats.elementAmounts[it] - (Stats.elementAmountsCached.firstOrNull()?.get(it) ?: 0.0)) / (timeBetweenRateTicks * 16)
                Stats.elementDeltas[it] = max(
                    Stats.elementDeltas[it],
                    Stats.elementRates[it]
                )
            }
            Stats.elementAmountsCached.add(Stats.elementAmounts.asMap)
            if (Stats.elementAmountsCached.size >= 16) Stats.elementAmountsCached.removeFirst()
        }
        incoming = defaultMutableStack
        Stats.lastTickDt = dt
    }

    fun canDoReaction(reaction: Reaction) =
        ((reaction !is NullReaction)
                && reaction.inputs.all { (k, v) -> v <= Stats.elementAmounts[k] }
                && reaction.multipliedOutputs.none { (k, v) -> if (k == Elements.heat) v + Stats.elementAmounts[k] - reaction.inputs[k] > Stats.functionalElementUpperBounds[k] else v != 0.0 && Stats.elementAmounts[k] >= Stats.functionalElementUpperBounds[k] })

    fun attemptReaction(reaction: Reaction) {
        if (canDoReaction(reaction)) {
            Elements.values.forEach {
                Stats.elementAmounts[it] -= reaction.multipliedInputs[it]
            }
            incoming.add(reaction.multipliedOutputs)
            if (reaction is SpecialReaction) reaction.execute()
        }
    }

    fun addClicker(clicker: Clicker) {
        val old = clickersById[clicker.id]
        val x = old?.htmlElement?.screenX ?: 0.0
        val y = old?.htmlElement?.screenY ?: 0.0
        old?.deInit()
        clickersById[clicker.id] = clicker
        clicker.init()
        fixClickerDockOrder()
        clicker.htmlElement.screenX = x
        clicker.htmlElement.screenY = y
        //clicker.moveToDock(force = true)
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