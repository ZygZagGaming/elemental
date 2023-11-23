@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package libraries

import core.*
import kotlin.math.pow

@Suppress("unused")
object DeltaReactions: Library<SpecialReaction>() {
    val overclocking = register(
        "overclocking",
        SpecialReaction(
            "Overclocking",
            inputsSupplier = {
                elementStackOf(
                    Resources.deltaA to 15.0 * 2.0.pow(it),
                    Resources.deltaB to 60.0 * 2.0.pow(it)
                )
            },
            effects = { it, offline ->
                val multiplier = (it * 0.125 + 1.0).squared()
                gameState.autoclickspeedMultiplierStack.setMultiplier("overclocking", multiplier)
            },
            undo = {
                gameState.autoclickspeedMultiplierStack.removeMultiplier("overclocking")
            },
            stringEffects = {
                val currentMultiplier = ((it - 1) * 0.125 + 1).squared()
                val nextMultiplier = (it * 0.125 + 1).squared()
                "Multiplier to clicker autoclick speed: x${currentMultiplier.roundTo(4)} → x${nextMultiplier.roundTo(4)}"
            },
            usageCap = 100
        )
    )

    val escapism = register(
        "escapism",
        SpecialReaction(
            "Escapism",
            inputsSupplier = {
                if (it >= 2) elementStackOf(
                    Resources.deltaCatalyst to 1000.0
                )
                else elementStackOf(
                    Resources.deltaCatalyst to 100.0 * 2.0.pow(it)
                )
            },
            effects = { it, offline ->
                when (it) {
                    1 -> Flags.escapism1.add()
                    2 -> Flags.escapism2.add()
                }
            },
            undo = {
                Flags.escapism1.remove()
                Flags.escapism2.remove()
            },
            stringEffects = {
                when (it) {
                    1 -> "Duality no longer resets ${SpecialReactions.massiveClock.name} or ${SpecialReactions.infoNerd.name} purchases"
                    2 -> "Duality no longer resets ${SpecialReactions.moneyUp.name} purchases"
                    else -> "Duality no longer resets ${SpecialReactions.moneyUp.name} purchases"
                }
            },
            usageCap = 2
        )
    )

    val alternateDuality = register(
        "alternate_duality",
        SpecialReaction(
            "Alternate Duality",
            inputsSupplier = {
                when (it) {
                    1 -> elementStackOf(
                        Resources.deltaA to 100.0,
                        Resources.deltaC to 5.0
                    )
                    else -> elementStackOf(
                        Resources.deltaA to 100.0,
                        Resources.deltaC to 5.0
                    )
                }
            },
            effects = { it, offline ->
                when (it) {
                    1 -> {
                        Flags.betterClockworkCostScaling.add()
                    }
                }
            },
            undo = {
                Flags.betterClockworkCostScaling.remove()
            },
            stringEffects = {
                "Reduce \"${SpecialReactions.clockwork.name}\" cost scaling"
            },
            usageCap = 1
        )
    )

    val clickerOverload = register(
        "clicker_overload",
        SpecialReaction(
            "Clicker Overload",
            inputsSupplier = {
                 elementStackOf(
                     Resources.deltaCatalyst to 500.0 * it,
                     Resources.deltaC to 5.0 * it
                 )
            },
            effects = { it, offline ->
                val clicker =
                    Clicker(
                        it + 5,
                        Pages.elementsPage,
                        ClickerMode.AUTO,
                        1.0, 6.0
                    )
                gameState.addClicker(clicker)
                clicker.modesUnlocked.addAll(setOf(ClickerMode.MANUAL, ClickerMode.AUTO))
                clicker.setMode(ClickerMode.AUTO)
            },
            undo = {
                for (id in 6..it + 5) {
                    gameState.removeClickerById(id)
                }
            },
            stringEffects = {
                "Unlock Autoclicker ${it + 5} with x0.25 autoclick speed"
            },
            usageCap = 15
        )
    )
}