@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package libraries

import core.*
import kotlin.math.pow

object DeltaReactions: Library<SpecialReaction>() {
    val overclocking = register(
        "overclocking",
        SpecialReaction(
            "Overclocking",
            inputsSupplier = {
                elementStackOf(
                    Elements.deltaA to 60.0 * 2.0.pow(it),
                    Elements.deltaB to 15.0 * 2.0.pow(it)
                )
            },
            effects = { it, offline ->
                val multiplier = (it * 0.125 + 1.0).squared()
                gameState.clickersById.values.forEach {
                    it.autoCpsModifiers.setMultiplier("overclocking", multiplier)
                }
            },
            undo = {
                gameState.clickersById.values.forEach {
                    it.autoCpsModifiers.removeMultiplier("overclocking")
                }
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
                    Elements.deltaCatalyst to 1000.0
                )
                else elementStackOf(
                    Elements.deltaCatalyst to 250.0 * 2.0.pow(it)
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
                        Elements.deltaA to 200.0,
                        Elements.deltaC to 30.0
                    )
                    else -> elementStackOf(
                        Elements.deltaA to 200.0,
                        Elements.deltaC to 30.0
                    )
                }
            },
            effects = { it, offline ->
                when (it) {
                    1 -> {
                        DynamicHTMLManager.showTutorial(Tutorials.alteredReactions)
                        NormalReactions.bBackToA.alterations++
                    }
                }
            },
            undo = {
                NormalReactions.bBackToA.apply {
                    name = name.substring(0, name.length - 2)
                    inputs = elementStackOf(
                        Elements.b to 1.0
                    )
                    outputs = elementStackOf(
                        Elements.catalyst to 3.0,
                        Elements.a to 2.0,
                        Elements.heat to 0.5
                    )
                }
            },
            stringEffects = {
                "Alter reaction \"${NormalReactions.aToB.name}\""
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
                     Elements.deltaCatalyst to 1000.0 * it,
                     Elements.deltaC to 30.0 * it
                 )
            },
            effects = { it, offline ->
                val clicker =
                    Clicker(
                        it + 5,
                        Pages.elementsPage,
                        ClickerMode.MANUAL,
                        4.0, 6.0
                    )
                gameState.addClicker(clicker)
                clicker.modesUnlocked.add(ClickerMode.MANUAL)
                clicker.setMode(ClickerMode.MANUAL)
            },
            undo = {
                for (id in 6..it + 5) {
                    gameState.removeClickerById(id)
                }
            },
            stringEffects = {
                "Unlock Keyclicker ${it + 5}"
            },
            usageCap = 15
        )
    )
}