package libraries

import core.*
import kotlin.math.log
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object SpecialReactions: Library<SpecialReaction>() {
    val clockStarted = register("clock_started",
        SpecialReaction(
            "Clock Started",
            {
                elementStackOf(
                    Resources.heat to 10.0,
                    Resources.b to 100.0
                )
            },
            {
                elementStackOf(
                    Resources.heat to 6.0
                )
            },
            effects = { it, offline ->
                Stats.gameSpeed = 1.0
                DynamicHTMLManager.showTutorial(Tutorials.clockStarted)
            },
            undo = {
                Stats.gameSpeed = 0.0
            },
            stringEffects = {
                if (it == 1) "Game speed x0 → x1"
                else "Game speed x1"
            }
        )
    )
    val clockwork = register("clockwork",
        SpecialReaction(
            "Clockwork",
            {
                if (Flags.betterClockworkCostScaling.isUnlocked()) elementStackOf(
                    Resources.a to 40.0 + 30.0 * it
                )
                else elementStackOf(
                    Resources.a to 80.0 + 80.0 * it
                )
            },
            effects = { it, offline ->
                when (it) {
                    1 -> {
                        var n = 1
                        while (n <= 5) {
                            if (n !in gameState.clickersById) {
                                val clicker = Clicker(n, Pages.elementsPage, ClickerMode.MANUAL, 4.0, 6.0)
                                gameState.addClicker(clicker)
                                GameTimer.nextTick {
                                    clicker.moveToDock(true, source = "initialized")
                                }
                            }
                            n++
                        }
                        gameState.clickersById.values.forEach {
                            it.modesUnlocked.add(ClickerMode.MANUAL)
                            if (it.id <= 5 && ClickerMode.AUTO !in it.modesUnlocked) it.setMode(ClickerMode.MANUAL)
                        }
                        DynamicHTMLManager.showTutorial(Tutorials.clickers)
                    }
                    2 -> {
                        gameState.clickersById[1]!!.apply {
                            modesUnlocked.add(ClickerMode.AUTO)
                            setMode(ClickerMode.AUTO)
                        }
                    }
                    3 -> {
                        gameState.clickersById[2]!!.apply {
                            modesUnlocked.add(ClickerMode.AUTO)
                            setMode(ClickerMode.AUTO)
                        }
                        gameState.clickersById[3]!!.apply {
                            modesUnlocked.add(ClickerMode.AUTO)
                            setMode(ClickerMode.AUTO)
                        }
                        if (Flags.automagic.isUnlocked()) {
                            gameState.clickersById[4]!!.apply {
                                modesUnlocked.add(ClickerMode.AUTO)
                                setMode(ClickerMode.AUTO)
                            }
                            gameState.clickersById[5]!!.apply {
                                modesUnlocked.add(ClickerMode.AUTO)
                                setMode(ClickerMode.AUTO)
                            }
                        }
                    }
                    else -> {
                        val shift = (it - 3) * 0.5
                        if (shift >= 3) DynamicHTMLManager.showTutorial(Tutorials.superclickers)
                        gameState.autoclickspeedMultiplierStack.setShift("clockwork", shift)
                    }
                }
            },
            undo = {
                // keep autoclickers but restrict modes
                gameState.clickersById.values.forEach {
                    if (it.id in 1..5) {
                        it.modesUnlocked.removeAll(
                            setOf(
                                ClickerMode.AUTO,
                                ClickerMode.MANUAL
                            )
                        )
                    }
                }
                gameState.autoclickspeedMultiplierStack.removeShift("clockwork")
            },
            stringEffects = {
                when (it) {
                    1 -> {
                        if (Stats.elementAmounts[Resources.dualities] > 0) "Unlock Manual Mode on Clickers 1-5"
                        else "Unlock Clickers 1-5"
                    }
                    2 -> "Unlock Auto Mode on Clicker 1 with a click rate of 4 Hz"
                    3 -> if (Flags.automagic.isUnlocked()) "Unlock Auto Mode on Clickers 2-5 with click rates of 4 Hz" else "Unlock Auto Mode on Clickers 2 and 3 with click rates of 4 Hz"
                    else -> "All Clicker base autoclick rates +0.5 Hz"
                }
            },
            usageCap = 100
        )
    )
    val massiveClock = register("massive_clock",
        SpecialReaction(
            "Ticking Clock",
            {
                elementStackOf(
                    Resources.d to 2.0 * it * (if (it > 5) it - 5.0 else 1.0)
                )
            },
            effects = { it, offline ->
                GameTimer.registerTicker("massiveClockTicker") { dt ->
                    val multiplier = 0.08 * it
                    val catalysts = max(
                        0.0,
                        min(
                            Stats.elementMultipliers[Resources.catalyst].appliedTo(dt * Stats.gameSpeed * multiplier * Stats.elementAmounts[Resources.d]),
                            Stats.functionalElementUpperBounds[Resources.catalyst] * .99 - Stats.elementAmounts[Resources.catalyst]
                        )
                    )
                    gameState.incoming.add(elementStackOf(Resources.catalyst to catalysts))
                }
            },
            undo = {
                GameTimer.removeTicker("massiveClockTicker")
            },
            stringEffects = {
                val multiplier = 0.08 * it
                "Each \"${Resources.d.symbol}\" generates \"${Resources.catalyst.symbol}\" at ${
                    (multiplier - 0.08).roundTo(
                        2
                    )
                } → ${multiplier.roundTo(2)} per second (until 99% of cap)"
            },
            usageCap = 100
        )
    )
    val heatingUp = register("heating_up",
        SpecialReaction(
            "Heating Up",
            {
                elementStackOf(
                    Resources.a to 1000.0 + if (it <= 3) 0.0 else if (it <= 7) 200.0 * (it - 3) else 200.0 * (it - 3) * (it - 7)
                )
            },
            effects = { it, offline ->
                Stats.elementMultipliers[Resources.b].setMultiplier("heating_up", it.toDouble() + 1)
            },
            undo = {
                Stats.elementMultipliers[Resources.b].removeMultiplier("heating_up")
            },
            stringEffects = {
                "\"${Resources.b.symbol}\" production x$it → x${it + 1}"
            },
            usageCap = 100
        )
    )
    val overheat = register("overheat",
        SpecialReaction(
            "Overheat",
            {
                elementStackOf(
                    Resources.c to 64.0 * it
                )
            },
            effects = { it, offline ->
                Stats.elementUpperBoundMultipliers[Resources.heat].setMultiplier("overheat", 1.0 + it * 1.8)
            },
            undo = {
                Stats.elementUpperBoundMultipliers[Resources.heat].removeMultiplier("overheat")
            },
            stringEffects = {
                "Heat cap x${(1 + (it - 1) * 1.8).roundToOneDecimalPlace()} → x${(1 + it * 1.8).roundToOneDecimalPlace()}"
            },
            usageCap = 100
        )
    )
    val heatSink = register("heat_sink",
        SpecialReaction(
            "Heat Sink",
            {
                elementStackOf(
                    Resources.c to 16.0 * it.squared(),
                    Resources.heat to 2.0 * (it + 2).squared()
                )
            },
            effects = { it, offline ->
                NormalReactions.cminglyOp.apply {
                    inputs = inputs.mutateDefaulted { map ->
                        map[Resources.heat] = 2.0 * (it + 2).squared()
                    }
                }
            },
            undo = {
                NormalReactions.cminglyOp.apply {
                    inputs = inputs.mutateDefaulted { map ->
                        map[Resources.heat] = 8.0
                    }
                }
            },
            stringEffects = {
                "Heat cost on \"${NormalReactions.cminglyOp.name}\" ${2 * (it + 1).squared()} → ${2 * (it + 2).squared()}"
            },
            usageCap = 100
        )
    )
    val exponEntial = register("expon_ential",
        SpecialReaction(
            "ExponEntial",
            {
                elementStackOf(
                    Resources.e to 2.0.pow(it - 1)
                )
            },
            effects = { it, offline ->
                Stats.elementUpperBoundMultipliers[Resources.a].setMultiplier("expon_ential") { 1.1.pow(it) }
            },
            undo = {
                Stats.elementUpperBoundMultipliers[Resources.a].removeMultiplier("expon_ential")
            },
            stringEffects = {
                "x1.1 to \"${Resources.a.symbol}\" cap, currently x${1.1.pow(it - 1).roundTo(3)}"
            },
            usageCap = 100
        )
    )
    val infoNerd = register("info_nerd",
        SpecialReaction(
            "Info Nerd",
            {
                elementStackOf(
                    Resources.heat to 20.0,
                    Resources.a to 1000.0
                )
            },
            effects = { it, offline ->
                DynamicHTMLManager.addElementClass("heat-element", "shown")
            },
            undo = {
                DynamicHTMLManager.removeElementClass("heat-element", "shown")
            },
            stringEffects = {
                "Shows numeric values for heat"
            }
        )
    )
//    val onEfficiency = register("on_efficiency",
//        core.SpecialReaction(
//            "On Efficiency",
//            {
//                core.elementStackOf(
//                    libraries.Elements.catalyst to 5000.0 * it,
//                    libraries.Elements.b to 4000.0 * it
//                )
//            },
//            effects = {
//                core.Stats.reactionEfficiencies[libraries.NormalReactions.bBackToA] = 2.0 * it
//            },
//            stringEffects = {
//                "\"${libraries.NormalReactions.bBackToA.name}\" reaction efficiency x${if (it == 1) 1 else 2 * it - 2} → x${2 * it}"
//            },
//            usageCap = 100
//        )
//    )
//    val noneLeft = register("none_left",
//        core.SpecialReaction(
//            "None Left",
//            {
//                core.elementStackOf(
//                    libraries.Elements.catalyst to 100000.0 * (if (it <= 5) 1.0 else (it - 5.0) * (it - 5))
//                )
//            },
//            effects = {
//                core.Stats.elementMultipliers[libraries.Elements.a] = (it + 1.0) * (it + 1)
//                core.Stats.elementCapMultipliers[libraries.Elements.a] = (it + 1.0) * (it + 1)
//            },
//            stringEffects = {
//                "\"${libraries.Elements.a.symbol}\" production and \"${libraries.Elements.a.symbol}\" cap x${it * it} → x${(it + 1) * (it + 1)}"
//            },
//            usageCap = 100
//        )
//    )
    val moneyUp = register("money_up",
        SpecialReaction(
            "Money Up",
            {
                elementStackOf(
                    Resources.catalyst to 10000.0
                )
            },
            effects = { _, _ ->
                Stats.elementMultipliers[Resources.catalyst].setMultiplier("money_up") {
                    val heat = Stats.elementAmounts[Resources.heat]
                    log(heat + 1.0, 4.0) / 3.0 + 1 + if (heat > 50) log(heat - 49, 4.0) else 0.0
                }
            },
            undo = {
                Stats.elementMultipliers[Resources.catalyst].removeMultiplier("money_up")
            },
            stringEffects = {
                val heat = max(0.0, Stats.elementAmounts[Resources.heat])
                val multiplier = log(heat + 1.0, 4.0) / 3.0 + 1 + if (heat > 50) log(heat - 49, 4.0) else 0.0
                "Multiplier to \"${Resources.catalyst.symbol}\" gain based on heat amount, currently x${multiplier.roundTo(3)}"
            }
        )
    )
}