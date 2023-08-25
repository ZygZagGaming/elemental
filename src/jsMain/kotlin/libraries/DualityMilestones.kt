package libraries

import core.*
import kotlin.math.log

object DualityMilestones: Library<MilestoneReaction>() {
    val aBeginning = register(
        "a_beginning",
        MilestoneReaction(
            "A Beginning",
            {
                elementStackOf(
                    Elements.omega to 1.0,
                    Elements.alpha to 1.0
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
    val automagic = register(
        "automagic",
        MilestoneReaction(
            "Automagic",
            {
                elementStackOf(
                    Elements.omega to 2.0,
                    Elements.alpha to 2.0
                )
            },
            effects = { n, offline ->
                Stats.flags.add("automagic")
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

    val respect = register(
        "respect",
        MilestoneReaction(
            "Respect",
            inputsSupplier = {
                elementStackOf(
                    Elements.alpha to 3.0,
                    Elements.omega to 3.0
                )
            },
            effects = { it, offline ->
                Stats.flags.add("respecUnlocked")
                DynamicHTMLManager.showTutorial(Tutorials.deltaReactionRespec)
            },
            undo = {
                Stats.flags.remove("respecUnlocked")
            },
            stringEffects = {
                "Unlock Delta Reaction respec"
            },
            usageCap = 1
        )
    )
}