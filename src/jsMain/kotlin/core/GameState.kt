package core

import kotlinx.browser.document
import libraries.*
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
    val autoclickspeedMultiplierStack = MultiplierStack()
    val clickersByPage get() = clickersById.values.groupBy { it.page }
    val timeBetweenRateTicks = 0.25

    fun tick(dt: Double, offline: Boolean = false) {
        Input.tick(dt)

        Stats.timeSinceLastDuality += dt

        lastReaction = hoveredReaction
        timeSpent += dt
        timeSpentOnPage += dt

        clickersById.values.forEach { /*if (core.DynamicHTMLManager.shownPage == libraries.Pages.id(it.page))*/ it.tick(dt) }

        Stats.elementAmounts[Resources.heat] = max(0.0, Stats.elementAmounts[Resources.heat] * (1 - 0.1 * dt * Stats.gameSpeed))
        if (Stats.elementAmounts[Resources.catalyst] >= Stats.dualityThreshold && !Flags.reachedDuality.isUnlocked() && Stats.timeSinceLastDuality > 1.0) Flags.reachedDuality.add()
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
        if (dt < timeBetweenRateTicks && (GameTimer.every(timeBetweenRateTicks, dt) || offline)) {
            if (!offline && timeSpentOnPage >= 5.0 && Stats.elementAmountsCached.isNotEmpty()) {
                val (elements, timestamp) = Stats.elementAmountsCached.first()
                Resources.values.forEach {
                    Stats.elementRates[it] = (Stats.elementAmounts[it] - (elements[it] ?: 0.0)) / (timeSpent - timestamp)
                    val old = Stats.elementDeltas[it]
                    Stats.elementDeltas[it] = max(
                        Stats.elementDeltas[it],
                        Stats.elementRates[it]
                    )
                    if (it.isElement) {
                        Stats.baseElementUpperBounds[it.delta] = max(Stats.elementDeltas[it], Stats.baseElementUpperBounds[it.delta])
                        Stats.elementAmounts[it.delta] += Stats.elementDeltas[it] - old
                    }
                }
            }
            Stats.elementAmountsCached.add(Stats.elementAmounts.asMap to timeSpent)
            while (Stats.elementAmountsCached.size >= 16) Stats.elementAmountsCached.removeFirst()
        } else if (dt > timeBetweenRateTicks) Stats.elementAmountsCached
        incoming = emptyMutableStack
        Stats.lastTickDt = dt
    }

    fun canDoReaction(reaction: Reaction) =
        ((reaction !is NullReaction)
                && reaction.inputs.all { (k, v) -> v <= Stats.elementAmounts[k] }
                && reaction.multipliedOutputs.none { (k, v) -> if (k == Resources.heat) v + Stats.elementAmounts[k] - reaction.inputs[k] > Stats.functionalElementUpperBounds[k] else v != 0.0 && Stats.elementAmounts[k] >= Stats.functionalElementUpperBounds[k] })

    fun attemptReaction(reaction: Reaction) {
        if (canDoReaction(reaction)) {
            if (reaction.consumesElements) Resources.values.forEach {
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

    fun removeClickerById(id: Int) {
        clickersById.remove(id)?.deInit()
    }
}


@OptIn(ExperimentalJsExport::class)
@JsExport
fun duality(force: Boolean = false) {
    if (force || Flags.reachedDuality.isUnlocked()) {
        val omega =
            Resources.basicElements.count { Stats.elementPercentages[it] >= Stats.alphaThreshold }
        var alpha =
            Resources.basicElements.count { Stats.elementPercentages[it] <= Stats.omegaThreshold }
        if (Flags.paraAlpha.isUnlocked()) {
            alpha += Resources.basicElements.count { Stats.elementAmounts[it] > 1000 }
        }
        Resources.basicElements.forEach { Stats.elementAmounts[it] = defaultStartingElements[it] }
        SpecialReactions.values.forEach {
            if (!((it == SpecialReactions.massiveClock || it == SpecialReactions.infoNerd) && Flags.escapism1.isUnlocked())) {
                it.undoEffects()
            }
        }
        if (Stats.deltaReactionRespec) {
            Stats.deltaReactionRespec = false
            DeltaReactions.values.forEach { it.undoEffects(refund = true) }
        }
        Stats.elementAmounts[Resources.omega] += omega.toDouble()
        Stats.elementAmounts[Resources.alpha] += alpha.toDouble()
        Stats.timeSinceLastDuality = 0.0
        Stats.elementAmounts[Resources.dualities]++
        Flags.reachedDuality.remove()

        DynamicHTMLManager.showTutorial(Tutorials.duality)
    }
}

val epsilon = 1e-7