package libraries

import core.*
import kotlin.math.log
import kotlin.math.max
import kotlin.math.pow

object DualityMilestones: Library<MilestoneReaction>() {
    val aBeginning = register(
        "a_beginning",
        MilestoneReaction(
            "A Beginning",
            {
                elementStackOf(
                    Elements.alpha to 1.0,
                    Elements.omega to 1.0
                )
            },
            effects = { n, offline ->
                Stats.elementMultipliers[Elements.a].setMultiplier("a_beginning") {
                    log(Stats.timeSinceLastDuality / 120.0 + 4, 4.0)
                }
            },
            stringEffects = {
                val multiplier = log(Stats.timeSinceLastDuality / 120.0 + 4, 4.0)
                "Multiplier to \"${Elements.a.symbol}\" gain based on time since last Duality: x${multiplier.toString(3)}"
            }
        )
    )
    val respect = register(
        "respect",
        MilestoneReaction(
            "Respect",
            inputsSupplier = {
                elementStackOf(
                    Elements.alpha to 3.0
                )
            },
            effects = { it, offline ->
                Flags.respecUnlocked.add()
                DynamicHTMLManager.showTutorial(Tutorials.deltaReactionRespec)
            },
            undo = {
                Flags.respecUnlocked.remove()
            },
            stringEffects = {
                "Unlock Delta Reaction respec"
            },
            usageCap = 1
        )
    )

    val automagic = register(
        "automagic",
        MilestoneReaction(
            "Automagic",
            {
                elementStackOf(
                    Elements.omega to 3.0
                )
            },
            effects = { n, offline ->
                Flags.automagic.add()
                if (SpecialReactions.clockwork.nTimesUsed >= 3) {
                    gameState.clickersById[4]?.apply {
                        modesUnlocked.add(ClickerMode.AUTO)
                        setMode(ClickerMode.AUTO)
                    }
                    gameState.clickersById[5]?.apply {
                        modesUnlocked.add(ClickerMode.AUTO)
                        setMode(ClickerMode.AUTO)
                    }
                }
            },
            stringEffects = {
                "Allow \"${SpecialReactions.clockwork.name}\" to unlock Auto Mode on Clickers 4 and 5"
            }
        )
    )

    val escalate = register(
        "escalate",
        MilestoneReaction(
            "Escalate",
            inputsSupplier = {
                elementStackOf(
                    Elements.alpha to 4.0.pow(it),
                    Elements.omega to 4.0.pow(it)
                )
            },
            effects = { it, offline ->
                Stats.elementMultipliers[Elements.e].setMultiplier("escalate") { 1.2.pow(it) }
            },
            undo = {
                Stats.elementMultipliers[Elements.e].removeMultiplier("escalate")
            },
            stringEffects = {
                "+20% \"${Elements.e.symbol}\" gain, currently x${1.2.pow(it - 1)}"
            },
            usageCap = 100
        )
    )

    val synergy = register(
        "synergy",
        MilestoneReaction(
            "Synergy",
            inputsSupplier = {
                elementStackOf(
                    Elements.alpha to 30.0 * 40.0.pow(it - 1)
                )
            },
            effects = { it, offline ->
                for ((i, elem) in Elements.basicElements.withIndex()) if (i != 0) {
                    Stats.elementMultipliers[elem].setMultiplier("synergy") {
                        max(1.0, log(Stats.elementAmounts[Elements.basicElements[(i - 2).mod(Elements.basicElements.size - 1) + 1]], 4.0).pow(it * 0.25))
                    }
                    Stats.elementUpperBoundMultipliers[elem].setMultiplier("synergy") {
                        max(1.0, log(Stats.elementAmounts[Elements.basicElements[(i - 2).mod(Elements.basicElements.size - 1) + 1]], 4.0).pow(it * 0.25))
                    }
                }
            },
            undo = {
                for (elem in Elements.basicElements) if (elem != Elements.catalyst) {
                    Stats.elementMultipliers[elem].removeMultiplier("synergy")
                    Stats.elementUpperBoundMultipliers[elem].removeMultiplier("synergy")
                }
            },
            stringEffects = {
                "Multiplier to \"${Elements.a.symbol}\"-\"${Elements.g.symbol}\" and their caps based on the element anticlockwise"
            },
            usageCap = 4
        )
    )

    val harmony = register(
        "harmony",
        MilestoneReaction(
            "Harmony",
            inputsSupplier = {
                elementStackOf(
                    Elements.omega to 30.0 * 30.0.pow(it - 1)
                )
            },
            effects = { it, offline ->
                for ((i, elem) in Elements.basicElements.withIndex()) if (i != 0) {
                    Stats.elementMultipliers[elem].setMultiplier("harmony") {
                        max(1.0, log(Stats.elementAmounts[Elements.basicElements[(i - 1).mod(Elements.basicElements.size - 1) + 2]], 4.0).pow(it * 0.26))
                    }
                    Stats.elementUpperBoundMultipliers[elem].setMultiplier("harmony") {
                        max(1.0, log(Stats.elementAmounts[Elements.basicElements[(i - 1).mod(Elements.basicElements.size - 1) + 2]], 4.0).pow(it * 0.26))
                    }
                }
            },
            undo = {
                for (elem in Elements.basicElements) if (elem != Elements.catalyst) {
                    Stats.elementMultipliers[elem].removeMultiplier("harmony")
                }
            },
            stringEffects = {
                "Multiplier to \"${Elements.a.symbol}\"-\"${Elements.g.symbol}\" and their caps based on the element clockwise"
            },
            usageCap = 4
        )
    )

    val cataclysm = register(
        "cataclysm",
        MilestoneReaction(
            "Cataclysm",
            inputsSupplier = {
                elementStackOf(
                    Elements.omega to 5.0 * it * (if (it < 10) 1.0 else if (it < 50) it - 10.0 else (it - 10) * 2.0.pow(it - 50)),
                    Elements.alpha to 5.0 * it * (if (it < 10) 1.0 else if (it < 50) it - 10.0 else (it - 10) * 2.0.pow(it - 50))
                )
            },
            effects = { it, offline ->
                Stats.elementMultipliers[Elements.catalyst].setMultiplier("cataclysm") { 1.05.pow(it) }
            },
            undo = {
                Stats.elementMultipliers[Elements.catalyst].removeMultiplier("cataclysm")
            },
            stringEffects = {
                "+5% \"${Elements.catalyst.symbol}\" gain, total: x${1.05.pow(it - 1)}"
            },
            usageCap = 100000
        )
    )
}