package libraries

import core.ElementType
import org.w3c.dom.Element

object Elements: Library<ElementType>() {
    val symbolMap by lazy {
        values.associateBy { it.symbol }
    }
    val basicElements by lazy {
        values.filter { it.isBasic }
    }

    val catalyst = register("catalyst", ElementType("Catalyst", Symbols.catalyst, isBasic = true))
    val a = register("element_a", ElementType("A", Symbols.a, isBasic = true))
    val b = register("element_b", ElementType("B", Symbols.b, isBasic = true))
    val c = register("element_c", ElementType("C", Symbols.c, isBasic = true))
    val d = register("element_d", ElementType("D", Symbols.d, isBasic = true))
    val e = register("element_e", ElementType("E", Symbols.e, isBasic = true))
    val f = register("element_f", ElementType("F", Symbols.f, isBasic = true))
    val g = register("element_g", ElementType("G", Symbols.g, isBasic = true))
    val heat = register("heat", ElementType("Heat", Symbols.heat, isDecimal = true, isBasic = true))

    val deltaCatalyst = register("delta_catalyst", ElementType("Delta Catalyst", "${Symbols.delta}${Symbols.catalyst}"))
    val deltaA = register("delta_element_a", ElementType("Delta A", "${Symbols.delta}${Symbols.a}"))
    val deltaB = register("delta_element_b", ElementType("Delta B", "${Symbols.delta}${Symbols.b}"))
    val deltaC = register("delta_element_c", ElementType("Delta C", "${Symbols.delta}${Symbols.c}"))
    val deltaD = register("delta_element_d", ElementType("Delta D", "${Symbols.delta}${Symbols.d}"))
    val deltaE = register("delta_element_e", ElementType("Delta E", "${Symbols.delta}${Symbols.e}"))
    val deltaF = register("delta_element_f", ElementType("Delta F", "${Symbols.delta}${Symbols.f}"))
    val deltaG = register("delta_element_g", ElementType("Delta G", "${Symbols.delta}${Symbols.g}"))
    val deltaHeat = register("delta_heat",
        ElementType("Delta Heat", "${Symbols.delta}${Symbols.heat}", isDecimal = true)
    )

    val omega = register("omega", ElementType("Omega", Symbols.omega))
    val alpha = register("alpha", ElementType("Alpha", Symbols.alpha))

    val dualities = register("dualities", ElementType("Dualities", Symbols.mu))
}