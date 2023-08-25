package core

import kotlinx.browser.document
import libraries.Elements
import libraries.SpecialReactions
import org.w3c.dom.get
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class GameState {
    var hoveredReaction: Reaction = NullReaction
    var incoming = emptyMutableStack
    var lastReaction = hoveredReaction
    var timeSpent: Double = 0.0
    var timeSpentOnPage: Double = 0.0
    val clickersById = mutableMapOf<Int, Clicker>()
    val clickersByPage get() = clickersById.values.groupBy { it.page }
    val timeBetweenRateTicks = 0.25

    fun tick(dt: Double, offline: Boolean = false) {
        Input.tick(dt)

        Stats.timeSinceLastDuality += dt

        lastReaction = hoveredReaction
        timeSpent += dt
        timeSpentOnPage += dt

        clickersById.values.forEach { /*if (core.DynamicHTMLManager.shownPage == libraries.Pages.id(it.page))*/ it.tick(dt) }

        Stats.elementAmounts[Elements.heat] *= (1 - 0.1 * dt * Stats.gameSpeed)
        if (!offline) {
            for ((key, value) in incoming) {
                //if (key.name.contains("Delta") && core.Stats.functionalElementUpperBounds[key] < core.Stats.elementAmounts[key]) console.log(core.Stats.functionalElementUpperBounds[key])
                Stats.elementAmounts[key] += min(
                    Stats.functionalElementUpperBounds[key] - Stats.elementAmounts[key],
                    max(
                        0.0,
                        value
                    )
                )
            }
        }
        if (GameTimer.every(timeBetweenRateTicks, dt) || offline) {
            if (!offline && timeSpentOnPage >= 5.0) Elements.values.forEach {
                Stats.elementRates[it] = (Stats.elementAmounts[it] - (Stats.elementAmountsCached.firstOrNull()?.get(it) ?: 0.0)) / (timeBetweenRateTicks * 16)
                /*if (it.isBasic) {
                    val old = core.Stats.elementDeltas[it]
                    val oldDelta = core.Stats.elementAmounts[it.libraries.getDelta]
                    core.Stats.elementDeltas[it] = max(
                        core.Stats.elementDeltas[it],
                        core.Stats.elementRates[it]
                    )
                    core.Stats.baseElementUpperBounds[it.libraries.getDelta] = core.Stats.elementDeltas[it]
                    core.Stats.elementAmounts[it.libraries.getDelta] += core.Stats.elementDeltas[it] - oldDelta
                } else {*/
                    val old = Stats.elementDeltas[it]
                    Stats.elementDeltas[it] = max(
                        Stats.elementDeltas[it],
                        Stats.elementRates[it]
                    )
                    if (it.isBasic) {
                        Stats.baseElementUpperBounds[it.delta] = max(Stats.elementDeltas[it], Stats.baseElementUpperBounds[it.delta])
                        Stats.elementAmounts[it.delta] += Stats.elementDeltas[it] - old
                    }
                //}
            }
            Stats.elementAmountsCached.add(Stats.elementAmounts.asMap)
            if (Stats.elementAmountsCached.size >= 16) Stats.elementAmountsCached.removeFirst()
        }
        incoming = emptyMutableStack
        Stats.lastTickDt = dt
    }

    fun canDoReaction(reaction: Reaction) =
        ((reaction !is NullReaction)
                && reaction.inputs.all { (k, v) -> v <= Stats.elementAmounts[k] }
                && reaction.multipliedOutputs.none { (k, v) -> if (k == Elements.heat) v + Stats.elementAmounts[k] - reaction.inputs[k] > Stats.functionalElementUpperBounds[k] else v != 0.0 && Stats.elementAmounts[k] >= Stats.functionalElementUpperBounds[k] })

    fun attemptReaction(reaction: Reaction) {
        if (canDoReaction(reaction)) {
            if (reaction.consumesElements) Elements.values.forEach {
                Stats.elementAmounts[it] -= reaction.multipliedInputs[it]
            }
            incoming.add(reaction.multipliedOutputs)
            if (reaction is SpecialReaction) reaction.execute(false)
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
        clickersById.entries.filter { it.value.page == DynamicHTMLManager.shownPage }.sortedBy { (id, _) -> id }.forEach { (_, clicker) -> fragment.appendChild(clicker.dock) }
        document.getElementsByClassName("clicker-dock-container")[0]!!.appendChild(fragment)
    }

    fun removeClicker(clicker: Clicker) {
        clickersById.remove(clicker.id)
        clicker.deInit()
    }
    fun removeClickerById(id: Int) {
        clickersById.remove(id)?.deInit()
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun duality() {
    Stats.elementAmounts[Elements.catalyst]
    val omega = Elements.basicElements.count { abs(Stats.elementAmounts[it] - Stats.functionalElementLowerBounds[it]) <= epsilon }
    val alpha = Elements.basicElements.count { abs(Stats.elementAmounts[it] - Stats.functionalElementUpperBounds[it]) <= epsilon }
    Elements.basicElements.forEach { Stats.elementAmounts[it] = defaultStartingElements[it] }
    SpecialReactions.values.forEach { if ((it != SpecialReactions.massiveClock && it != SpecialReactions.infoNerd) || "escapism" !in Stats.flags) it.undoEffects() }
    Stats.elementAmounts[Elements.omega] += omega.toDouble()
    Stats.elementAmounts[Elements.alpha] += alpha.toDouble()
    Stats.timeSinceLastDuality = 0.0
}

val epsilon = 1e-7