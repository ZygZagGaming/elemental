import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

open class Library<T> {
    val map get() = backingMap.toMap()
    val values get() = map.values
    private val backingMap = mutableMapOf<String, T>()
    protected fun register(id: String, elem: T): T {
        if (id !in backingMap) backingMap[id] = elem
        else throw Exception("Duplicate values in registry")
        return elem
    }

    fun id(elem: T): String? {
        return map.entries.firstOrNull { it.value == elem }?.key
    }
}

@Suppress("unused")
object SpecialReactions: Library<SpecialReaction>() {
    val clockStarted = register("clock_started",
        SpecialReaction(
            "Clock Started",
            {
                elementStackOf(
                    Elements.heat to 10.0,
                    Elements.b to 40.0
                )
            },
            {
                elementStackOf(
                    Elements.heat to 6.0
                )
            },
            effects = {
                Stats.gameSpeed = 1.0
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
                    Elements.a to 50.0 + 50.0 * it
                )
            },
            effects = {
                when (it) {
                    1 -> {
                        repeat(5) { n ->
                            val clicker = Clicker(n + 1, Pages.elementsPage, ClickerMode.MANUAL, 4.0, 6.0)
                            gameState.addClicker(clicker)
                            GameTimer.nextTick {
                                clicker.moveToDock(true)
                            }
                        }
                    }
                    2 -> {
                        gameState.clickersById[1]!!.modesUnlocked.add(ClickerMode.AUTO)
                    }
                    3 -> {
                        gameState.clickersById[2]!!.modesUnlocked.add(ClickerMode.AUTO)
                        gameState.clickersById[3]!!.modesUnlocked.add(ClickerMode.AUTO)
                    }
                    else -> {
                        repeat(3) { n -> gameState.clickersById[n + 1]!!.autoCps += 0.5 }
                    }
                }
            },
            stringEffects = {
                when (it) {
                    1 -> "Unlock Clickers 1-5"
                    2 -> "Unlock Auto Mode on Clicker 1 with a click rate of 4 Hz"
                    3 -> "Unlock Auto Mode on Clickers 2 and 3 with click rates of 4 Hz"
                    else -> "All Clicker autoclick rates ${it / 2.0 + 2} → ${it / 2.0 + 2.5} Hz"
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
            effects = {
                GameTimer.registerTicker("massiveClockTicker") { dt ->
                    val multiplier = 0.15 * it
                    val catalysts = max(
                        0.0,
                        min(
                            dt * Stats.gameSpeed * multiplier * Stats.elementAmounts[Elements.d],
                            Stats.functionalElementUpperBounds[Elements.catalyst] * .99 - Stats.elementAmounts[Elements.catalyst]
                        )
                    )
                    gameState.incoming.add(elementStackOf(Elements.catalyst to catalysts))
                }
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
                    Elements.b to 1000.0 + if (it <= 3) 0.0 else if (it <= 7) 200.0 * (it - 3) else 200.0 * (it - 3) * (it - 7)
                )
            },
            effects = {
                Stats.elementMultipliers[Elements.b] = it.toDouble() + 1
            },
            stringEffects = {
                "\"${Elements.b.symbol}\" production x${Stats.elementMultipliers[Elements.b]} → x${it + 1}"
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
            effects = {
                Stats.elementUpperBoundMultipliers[Elements.heat] += 0.2
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
                    Elements.d to 8.0 * it.squared(),
                    Elements.heat to 2.0 * (it + 2).squared()
                )
            },
            effects = {
                NormalReactions.cminglyOp.apply {
                    inputs = inputs.mutateDefaulted { map ->
                        map[Elements.heat] = 2.0 * (it + 2).squared()
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
                    Elements.e to 2.0.pow(it),
                    Elements.b to 250.0 * 4.0.pow(it)
                )
            },
            effects = {
                val multiplier = it.squared()
                GameTimer.registerTicker("double_b_cap") {
                    Stats.elementUpperBoundMultipliers[Elements.b] = (Stats.elementAmounts[Elements.e] + 1) * multiplier
                }
            },
            stringEffects = {
                if (it == 1) "Multiplier to \"${Elements.b.symbol}\" cap equal to \"${Elements.e.symbol}\" count (plus 1)"
                else "Multiplier to \"${Elements.b.symbol}\" cap equal to \"${Elements.e.symbol}\" count (+ 1) x${(it - 1).squared()} → x${it.squared()}"
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
                    Elements.b to 2000.0
                )
            },
            effects = {
                DynamicHTMLManager.addElementClass("heat-element", "shown")
            },
            stringEffects = {
                "Shows numeric values for heat"
            }
        )
    )
//    val onEfficiency = register("on_efficiency",
//        SpecialReaction(
//            "On Efficiency",
//            {
//                elementStackOf(
//                    Elements.catalyst to 5000.0 * it,
//                    Elements.b to 4000.0 * it
//                )
//            },
//            effects = {
//                Stats.reactionEfficiencies[NormalReactions.bBackToA] = 2.0 * it
//            },
//            stringEffects = {
//                "\"${NormalReactions.bBackToA.name}\" reaction efficiency x${if (it == 1) 1 else 2 * it - 2} → x${2 * it}"
//            },
//            usageCap = 100
//        )
//    )
//    val noneLeft = register("none_left",
//        SpecialReaction(
//            "None Left",
//            {
//                elementStackOf(
//                    Elements.catalyst to 100000.0 * (if (it <= 5) 1.0 else (it - 5.0) * (it - 5))
//                )
//            },
//            effects = {
//                Stats.elementMultipliers[Elements.a] = (it + 1.0) * (it + 1)
//                Stats.elementCapMultipliers[Elements.a] = (it + 1.0) * (it + 1)
//            },
//            stringEffects = {
//                "\"${Elements.a.symbol}\" production and \"${Elements.a.symbol}\" cap x${it * it} → x${(it + 1) * (it + 1)}"
//            },
//            usageCap = 100
//        )
//    )
}

object NormalReactions: Library<Reaction>() {
    val aToB = register("a_to_b",
        Reaction(
            "A to B",
            elementStackOf(
                Elements.catalyst to 1.0,
                Elements.a to 1.0
            ),
            elementStackOf(
                Elements.b to 3.0
            )
        )
    )
    val bBackToA = register("b_back_to_a",
        Reaction(
            "B back to A",
            elementStackOf(
                Elements.b to 1.0
            ),
            elementStackOf(
                Elements.catalyst to 2.0,
                Elements.a to 1.0,
                Elements.heat to 0.5
            )
        )
    )
    val abcs = register("abcs",
        Reaction(
            "ABCs",
            elementStackOf(
                Elements.catalyst to 2.0,
                Elements.b to 23.0,
            ),
            elementStackOf(
                Elements.c to 1.0,
                Elements.heat to 4.0
            )
        )
    )
    val cminglyOp = register("cmingly_op",
        Reaction(
            "Cmingly OP",
            elementStackOf(
                Elements.heat to 8.0,
                Elements.c to 1.0,
            ),
            elementStackOf(
                Elements.a to 30.0,
                Elements.b to 10.0
            )
        )
    )
//    val cataClysm = register("cataclysm",
//        Reaction(
//            "CataClysm",
//            elementStackOf(
//                Elements.c to 4.0,
//            ),
//            elementStackOf(
//                Elements.catalyst to 40.0,
//                Elements.heat to 9.0
//            )
//        )
//    )
//    val dscent = register("dscent",
//        Reaction(
//            "Dscent",
//            elementStackOf(
//                Elements.a to 120.0,
//            ),
//            elementStackOf(
//                Elements.d to 2.0
//            )
//        )
//    )
    val over900 = register("over900",
    Reaction(
        "Over 900",
        elementStackOf(
            Elements.b to 901.0,
        ),
        elementStackOf(
            Elements.d to 3.0
        )
    )
    )
    val exotherm = register("exotherm",
        Reaction(
            "Exotherm",
            elementStackOf(
                Elements.c to 60.0,
                Elements.a to 1000.0,
                Elements.catalyst to 3000.0
            ),
            elementStackOf(
                Elements.e to 1.0,
                Elements.heat to 20.0
            )
        )
    )
}

object Elements: Library<ElementType>() {
    val symbolMap get() = values.associateBy { it.symbol }

    val catalyst = register("catalyst", ElementType("Catalyst", Symbols.catalyst))
    val a = register("element_a", ElementType("A", Symbols.a))
    val b = register("element_b", ElementType("B", Symbols.b))
    val c = register("element_c", ElementType("C", Symbols.c))
    val d = register("element_d", ElementType("D", Symbols.d))
    val e = register("element_e", ElementType("E", Symbols.e))
    val f = register("element_f", ElementType("F", Symbols.f))
    val g = register("element_g", ElementType("G", Symbols.g))
    val heat = register("heat", ElementType("Heat", Symbols.heat, isDecimal = true))

    val deltaCatalyst = register("delta_catalyst", ElementType("Delta Catalyst", "${Symbols.delta}${Symbols.catalyst}"))
    val deltaA = register("delta_element_a", ElementType("Delta A", "${Symbols.delta}${Symbols.a}"))
    val deltaB = register("delta_element_b", ElementType("Delta B", "${Symbols.delta}${Symbols.b}"))
    val deltaC = register("delta_element_c", ElementType("Delta C", "${Symbols.delta}${Symbols.c}"))
    val deltaD = register("delta_element_d", ElementType("Delta D", "${Symbols.delta}${Symbols.d}"))
    val deltaE = register("delta_element_e", ElementType("Delta E", "${Symbols.delta}${Symbols.e}"))
    val deltaF = register("delta_element_f", ElementType("Delta F", "${Symbols.delta}${Symbols.f}"))
    val deltaG = register("delta_element_g", ElementType("Delta G", "${Symbols.delta}${Symbols.g}"))
    val deltaHeat = register("delta_heat", ElementType("Delta Heat", "${Symbols.delta}${Symbols.heat}", isDecimal = true))
}

val ElementType.isBasic get() = symbol.length == 1
val ElementType.delta get() = Elements.symbolMap["${Symbols.delta}$symbol"]!!

object Symbols: Library<Char>() {
    val catalyst = register("catalyst", 'ϟ')
    val a = register("a", 'a')
    val b = register("b", 'b')
    val c = register("c", 'c')
    val d = register("d", 'd')
    val e = register("e", 'e')
    val f = register("f", 'f')
    val g = register("g", 'g')
    val heat = register("heat", 'h')
    val delta = register("delta", 'Δ')
}

object Pages: Library<Page>() {
    val elementsPage = register("elements", Page("Elements"))
    val optionsPage = register("options", Page("Options"))
    val dualityPage = register("duality", Page("Duality"))
}

object TutorialPages: Library<TutorialPage>() {
    val titleScreenPage = register(
        "titleScreenPage",
        ImageTitleTutorialPage(
            image = "images/goober.png",
            imageAlt = "goober",
            titleText = "Elemental",
            subTitleText = "Made by ZygZag"
        )
    )
    val elementsPage = register(
        "elementsPage",
        ImageTextTutorialPage(
            image = "images/elements.png",
            imageAlt = "elements",
            headerText = "Elements",
            text = "These are your Elements. Most Elements have a bubble showing the Element's symbol and count. You can right-click on Element bubbles to show additional information like bounds and rates."
        )
    )
    val reactionsPage = register(
        "reactionsPage",
        ImageTextTutorialPage(
            image = "images/normalreactions.png",
            imageAlt = "goober",
            headerText = "Reactions",
            text = "These are your Normal Reactions. Each one shows the Elements they consume on the left of the arrow, and the Elements they produce on the right. Normal Reactions can be used as many times as you can afford."
        )
    )
    val specialReactionsPage = register(
        "specialReactionsPage",
        ImageTextTutorialPage(
            image = "images/specialreactions.png",
            imageAlt = "goober",
            headerText = "Special Reactions",
            text = "These are your Special Reactions. They consume and produce Elements like Normal Reactions, but also have an alternative effect. Special Reactions' cost and effect usually increase each time they are used."
        )
    )
    val heatPage = register(
        "heatPage",
        ImageTextTutorialPage(
            image = "images/heat.png",
            imageAlt = "goober",
            headerText = "Heat",
            text = "Heat (h) is an Element that is shown visually around the ϟ bubble. Unlike other elements, heat dissipates over time, and has a maximum of 10h. You cannot use reactions that would take you over the maximum."
        )
    )
}

object Tutorials: Library<Tutorial>() {
    val welcome = register(
        "welcome",
        listOf(
            TutorialPages.titleScreenPage,
            TutorialPages.elementsPage,
            TutorialPages.reactionsPage,
            TutorialPages.specialReactionsPage,
            TutorialPages.heatPage
        )
    )
}

