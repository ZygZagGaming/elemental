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
                    Resources.alpha to 1.0,
                    Resources.omega to 1.0
                )
            },
            effects = { n, offline ->
                Stats.elementMultipliers[Resources.a].setMultiplier("a_beginning") {
                    log(Stats.timeSinceLastDuality / 120.0 + 4, 4.0)
                }
            },
            stringEffects = {
                val multiplier = log(Stats.timeSinceLastDuality / 120.0 + 4, 4.0)
                "Multiplier to \"${Resources.a.symbol}\" gain based on time since last Duality: x${multiplier.toString(3)}"
            }
        )
    )
    val respect = register(
        "respect",
        MilestoneReaction(
            "Respect",
            inputsSupplier = {
                elementStackOf(
                    Resources.alpha to 3.0
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
                    Resources.omega to 3.0
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
                    Resources.alpha to 4.0.pow(it),
                    Resources.omega to 4.0.pow(it)
                )
            },
            effects = { it, offline ->
                Stats.elementMultipliers[Resources.e].setMultiplier("escalate") { 1.2.pow(it) }
            },
            undo = {
                Stats.elementMultipliers[Resources.e].removeMultiplier("escalate")
            },
            stringEffects = {
                "x1.2 \"${Resources.e.symbol}\" gain, currently x${1.2.pow(it - 1)}"
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
                    Resources.alpha to 30.0 * 40.0.pow(it - 1)
                )
            },
            effects = { it, offline ->
                for ((i, elem) in Resources.basicElements.withIndex()) if (i != 0 && i != 8) {
                    Stats.elementMultipliers[elem].setMultiplier("synergy") {
                        max(1.0, log(Stats.elementAmounts[Resources.basicElements[(i - 2).mod(Resources.basicElements.size - 1) + 1]], 4.0).pow(it * 0.25))
                    }
                    Stats.elementUpperBoundMultipliers[elem].setMultiplier("synergy") {
                        max(1.0, log(Stats.elementAmounts[Resources.basicElements[(i - 2).mod(Resources.basicElements.size - 1) + 1]], 4.0).pow(it * 0.25))
                    }
                }
            },
            undo = {
                for (elem in Resources.basicElements) if (elem != Resources.catalyst) {
                    Stats.elementMultipliers[elem].removeMultiplier("synergy")
                    Stats.elementUpperBoundMultipliers[elem].removeMultiplier("synergy")
                }
            },
            stringEffects = {
                "Multiplier to \"${Resources.a.symbol}\"-\"${Resources.g.symbol}\" and their caps based on the element anticlockwise"
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
                    Resources.omega to 30.0 * 30.0.pow(it - 1)
                )
            },
            effects = { it, offline ->
                for ((i, elem) in Resources.basicElements.withIndex()) if (i != 0 && i != 8) {
                    Stats.elementMultipliers[elem].setMultiplier("harmony") {
                        val n = Stats.elementAmounts[Resources.basicElements[i.mod(Resources.basicElements.size - 2) + 1]]
                        if (n <= 4.0) 1.0 else log(n, 4.0).pow(it * 0.26)
                    }
                    Stats.elementUpperBoundMultipliers[elem].setMultiplier("harmony") {
                        val n = Stats.elementAmounts[Resources.basicElements[i.mod(Resources.basicElements.size - 2) + 1]]
                        if (n <= 4.0) 1.0 else log(n, 4.0).pow(it * 0.26)
                    }
                }
            },
            undo = {
                for (elem in Resources.basicElements) if (elem != Resources.catalyst) {
                    Stats.elementMultipliers[elem].removeMultiplier("harmony")
                }
            },
            stringEffects = {
                "Multiplier to \"${Resources.a.symbol}\"-\"${Resources.g.symbol}\" and their caps based on the element clockwise"
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
                    Resources.omega to 50.0 * it * (if (it < 10) 1.0 else if (it < 50) it - 10.0 else (it - 10) * 2.0.pow(it - 50))
                )
            },
            effects = { it, offline ->
                Stats.elementMultipliers[Resources.catalyst].setMultiplier("cataclysm") { 1.05.pow(it) }
            },
            undo = {
                Stats.elementMultipliers[Resources.catalyst].removeMultiplier("cataclysm")
            },
            stringEffects = {
                "x1.05 \"${Resources.catalyst.symbol}\" gain, total: x${1.05.pow(it - 1)}"
            },
            usageCap = 100000
        )
    )

    val paraAlpha = register(
        "para_alpha",
        MilestoneReaction(
            "Para-Alpha",
            inputsSupplier = {
                elementStackOf(
                    Resources.omega to 50.0 * it * (if (it < 10) 1.0 else if (it < 50) it - 10.0 else (it - 10) * 2.0.pow(it - 50))
                )
            },
            effects = { _, _ ->
                Flags.paraAlpha.add()
            },
            undo = {
                Flags.paraAlpha.remove()
            },
            stringEffects = {
                when (it) {
                    1 -> "Obtain 0.2${Symbols.alpha} upon Duality for each Element count above 1000"
                    else -> "Obtain ${0.2 * (it - 1)}${Symbols.alpha} → ${0.2 * it}${Symbols.alpha} upon Duality for each Element count above 1000"
                }
            },
            usageCap = 100000
        )
    )
}