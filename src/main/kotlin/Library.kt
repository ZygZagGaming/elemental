import kotlin.math.floor

val elements = listOf(
    ElementType("Catalyst", Symbols.catalyst),
    ElementType("Element A", Symbols.a),
    ElementType("Element B", Symbols.b),
    ElementType("Element C", Symbols.c),
    ElementType("Element D", Symbols.d),
    ElementType("Element E", Symbols.e),
    ElementType("Element F", Symbols.f),
    ElementType("Element G", Symbols.g),
    ElementType("Heat", Symbols.heat)
).associateBy { it.symbol }

val defaultCaps get() = elements.values.associateWith { 1000.0 }.toMutableMap().also {
    it[Elements.catalyst] = 100000.0
    it[Elements.heat] = 10.0
}.toMap()

open class Library<T> {
    val map get() = backingMap.toMap()
    val values get() = map.values
    private val backingMap = mutableMapOf<String, T>()
    protected fun register(id: String, elem: T): T {
        if (id !in backingMap) backingMap[id] = elem
        else throw Exception("Duplicate values in registry")
        return elem
    }
}

object SpecialReactions: Library<SpecialReaction>() {
    val clockStarted = register("clock_started",
        SpecialReaction(
            "Clock Started",
            mapOf(
                Elements.heat to 10.0,
                Elements.b to 40.0
            ),
            effects = {
                Options.gameSpeed = 1.0
            },
            stringEffects = "Game speed 0 → 1"
        )
    )
    val clockwork = register("clockwork",
        SpecialReaction(
            "Clockwork",
            mapOf(
                Elements.a to 60.0
            ),
            effects = {
                gameState.automationTickers["a_to_b"]!!.rateHertz = 2.0
            },
            stringEffects = "Automate \"A to B\" at a rate of 0 → 2 per second"
        )
    )
    val massiveClock = register("massive_clock",
        SpecialReaction(
            "Massive Clock",
            mapOf(
                Elements.d to 4.0
            ),
            effects = {
                GameTimer.registerTicker { dt ->
                    val catalysts = dt * Options.gameSpeed * 0.1 * (gameState.elementAmounts[Elements.d] ?: 0.0) + Stats.partialCatalysts
                    gameState.incoming += mapOf(Elements.catalyst to floor(catalysts))
                    Stats.partialCatalysts = catalysts.mod(1f)
                }
            },
            stringEffects = "Each \"${Elements.d.symbol}\" generates \"${Elements.catalyst.symbol}\" at 0 → 0.1 per second"
        )
    )
}

object NormalReactions: Library<Reaction>() {
    val aToB = register("a_to_b",
        Reaction(
            "A to B",
            mapOf(
                Elements.catalyst to 1.0,
                Elements.a to 1.0
            ),
            mapOf(
                Elements.b to 3.0
            )
        )
    )
    val bBackToA = register("b_back_to_a",
        Reaction(
            "B back to A",
            mapOf(
                Elements.b to 1.0
            ),
            mapOf(
                Elements.catalyst to 2.0,
                Elements.a to 1.0,
                Elements.heat to 0.5
            )
        )
    )
    val abcs = register("abcs",
        Reaction(
            "ABCs",
            mapOf(
                Elements.catalyst to 2.0,
                Elements.a to 1.0,
                Elements.b to 16.0,
            ),
            mapOf(
                Elements.c to 1.0,
                Elements.heat to 5.0
            )
        )
    )
    val cminglyOp = register("cmingly_op",
        Reaction(
            "Cmingly OP",
            mapOf(
                Elements.heat to 8.0,
                Elements.c to 1.0,
            ),
            mapOf(
                Elements.a to 30.0,
                Elements.b to 10.0
            )
        )
    )
    val cataClysm = register("cataclysm",
        Reaction(
            "CataClysm",
            mapOf(
                Elements.c to 4.0,
            ),
            mapOf(
                Elements.catalyst to 40.0,
                Elements.heat to 9.0
            )
        )
    )
    val dscent = register("dscent",
        Reaction(
            "Dscent",
            mapOf(
                Elements.a to 160.0,
            ),
            mapOf(
                Elements.d to 1.0
            )
        )
    )
}

object Elements: Library<ElementType>() {
    val catalyst = register("catalyst", ElementType("Catalyst", Symbols.catalyst))
    val a = register("element_a", ElementType("Element A", Symbols.a))
    val b = register("element_b", ElementType("Element B", Symbols.b))
    val c = register("element_c", ElementType("Element C", Symbols.c))
    val d = register("element_d", ElementType("Element D", Symbols.d))
    val e = register("element_e", ElementType("Element E", Symbols.e))
    val f = register("element_f", ElementType("Element F", Symbols.f))
    val g = register("element_g", ElementType("Element G", Symbols.g))
    val heat = register("heat", ElementType("Heat", Symbols.heat))
}

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
}

object Options {
    var gameSpeed = 0.0
}

object Stats {
    var partialCatalysts = 0.0
}