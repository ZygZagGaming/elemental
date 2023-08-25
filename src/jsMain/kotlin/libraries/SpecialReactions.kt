package libraries

import core.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object SpecialReactions: Library<SpecialReaction>() {
    val clockStarted = register("clock_started",
        SpecialReaction(
            "Clock Started",
            {
                elementStackOf(
                    Elements.heat to 10.0,
                    Elements.b to 100.0
                )
            },
            {
                elementStackOf(
                    Elements.heat to 6.0
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
                elementStackOf(
                    Elements.a to 80.0 + 80.0 * it
                )
            },
            effects = { it, offline ->
                when (it) {
                    1 -> {
                        var n = 1
                        while (n <= 5) {
                            val clicker = Clicker(n++, Pages.elementsPage, ClickerMode.MANUAL, 4.0, 6.0)
                            gameState.addClicker(clicker)
                            GameTimer.nextTick {
                                clicker.moveToDock(true)
                            }
                        }
                        gameState.clickersById.values.forEach {
                            it.modesUnlocked.add(ClickerMode.MANUAL)
                            it.setMode(ClickerMode.MANUAL)
                        }
                        Stats.flags.add("clickersUnlocked")
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
                        if ("automagic" in Stats.flags) {
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
                        repeat(5) { n ->
                            gameState.clickersById[n + 1]!!.autoCpsModifiers.setShift("clockwork", shift)
                        }
                    }
                }
            },
            undo = {
                // keep autoclickers but restrict modes
                gameState.clickersById.values.forEach {
                    it.modesUnlocked.removeAll(
                        setOf(
                            ClickerMode.AUTO,
                            ClickerMode.MANUAL
                        )
                    )
                }
            },
            stringEffects = {
                when (it) {
                    1 -> {
                        if ("clickersUnlocked" in Stats.flags) "Unlock Manual Mode on Clickers 1-5"
                        else "Unlock Clickers 1-5"
                    }
                    2 -> "Unlock Auto Mode on Clicker 1 with a click rate of 4 Hz"
                    3 -> if ("automagic" in Stats.flags) "Unlock Auto Mode on Clickers 2-5 with click rates of 4 Hz" else "Unlock Auto Mode on Clickers 2 and 3 with click rates of 4 Hz"
                    else -> "All Clicker base autoclick rates ${(it - 4) * 0.5 + 4.0} → ${(it - 4) * 0.5 + 4.5} Hz"
                }
            },
            usageCap = 100
        )
    )
    val massiveClock = register("massive_clock",
        SpecialReaction(
            "Massive Clock",
            {
                elementStackOf(
                    Elements.d to 2.0 * it.squared()
                )
            },
            effects = { it, offline ->
                GameTimer.registerTicker("massiveClockTicker") { dt ->
                    val multiplier = 0.15 * it
                    val catalysts = max(
                        0.0,
                        min(
                            Stats.elementMultipliers[Elements.catalyst].appliedTo(dt * Stats.gameSpeed * multiplier * Stats.elementAmounts[Elements.d]),
                            Stats.functionalElementUpperBounds[Elements.catalyst] * .99 - Stats.elementAmounts[Elements.catalyst]
                        )
                    )
                    gameState.incoming.add(elementStackOf(Elements.catalyst to catalysts))
                }
            },
            undo = {
                GameTimer.removeTicker("massiveClockTicker")
            },
            stringEffects = {
                val multiplier = 0.15 * it
                "Each \"${Elements.d.symbol}\" generates \"${Elements.catalyst.symbol}\" at ${
                    (multiplier - 0.15).roundTo(
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
                    Elements.a to 1000.0 + if (it <= 3) 0.0 else if (it <= 7) 200.0 * (it - 3) else 200.0 * (it - 3) * (it - 7)
                )
            },
            effects = { it, offline ->
                Stats.elementMultipliers[Elements.b].setMultiplier("heating_up", it.toDouble() + 1)
            },
            undo = {
                Stats.elementMultipliers[Elements.b].removeMultiplier("heating_up")
            },
            stringEffects = {
                "\"${Elements.b.symbol}\" production x$it → x${it + 1}"
            },
            usageCap = 100
        )
    )
    val overheat = register("overheat",
        SpecialReaction(
            "Overheat",
            {
                elementStackOf(
                    Elements.c to 6.0 * it
                )
            },
            effects = { it, offline ->
                Stats.elementUpperBoundMultipliers[Elements.heat].setMultiplier("overheat", 1.0 + it * 0.2)
            },
            undo = {
                Stats.elementUpperBoundMultipliers[Elements.heat].removeMultiplier("overheat")
            },
            stringEffects = {
                "Heat cap x${(1 + (it - 1) * 0.2).roundToOneDecimalPlace()} → x${(1 + it * 0.2).roundToOneDecimalPlace()}"
            },
            usageCap = 100
        )
    )
    val heatSink = register("heat_sink",
        SpecialReaction(
            "Heat Sink",
            {
                elementStackOf(
                    Elements.c to 32.0 * it.squared(),
                    Elements.heat to 2.0 * (it + 2).squared()
                )
            },
            effects = { it, offline ->
                NormalReactions.cminglyOp.apply {
                    inputs = inputs.mutateDefaulted { map ->
                        map[Elements.heat] = 2.0 * (it + 2).squared()
                    }
                }
            },
            undo = {
                NormalReactions.cminglyOp.apply {
                    inputs = inputs.mutateDefaulted { map ->
                        map[Elements.heat] = 8.0
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
                    Elements.e to 2.0.pow(it - 1),
                    Elements.a to 250.0 * 4.0.pow(it)
                )
            },
            effects = { it, offline ->
                val multiplier = it.squared()
                Stats.elementUpperBoundMultipliers[Elements.a].setMultiplier("expon_ential") { (Stats.elementAmounts[Elements.e] + 1) * multiplier }
            },
            undo = {
                Stats.elementUpperBoundMultipliers[Elements.a].removeMultiplier("expon_ential")
            },
            stringEffects = {
                if (it == 1) "Multiplier to \"${Elements.a.symbol}\" cap equal to \"${Elements.e.symbol}\" count (plus 1)"
                else "Multiplier to \"${Elements.a.symbol}\" cap equal to \"${Elements.e.symbol}\" count (+ 1) x${(it - 1).squared()} → x${it.squared()}"
            },
            usageCap = 100
        )
    )
    val infoNerd = register("info_nerd",
        SpecialReaction(
            "Info Nerd",
            {
                elementStackOf(
                    Elements.heat to 20.0,
                    Elements.a to 1000.0
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
                    Elements.catalyst to 10000.0
                )
            },
            effects = { _, _ ->
                Stats.elementMultipliers[Elements.catalyst].setMultiplier("money_up") {
                    1.0 + 0.1 * Stats.elementAmounts[Elements.heat]
                }
            },
            undo = {
                Stats.elementMultipliers[Elements.catalyst].removeMultiplier("money_up")
            },
            stringEffects = {
                val multiplier = (1.0 + 0.1 * Stats.elementAmounts[Elements.heat]).toString(3)
                "Multiplier to \"${Elements.catalyst.symbol}\" gain based on \"${Elements.heat.symbol}\" amount, currently x$multiplier"
            }
        )
    )
}