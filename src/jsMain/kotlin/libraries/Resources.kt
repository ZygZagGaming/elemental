package libraries

import core.Resource

object Resources: Library<Resource>() {
    val symbolMap by lazy {
        values.associateBy { it.symbol }
    }
    val basicElements by lazy {
        values.filter { it.isElement }
    }

    val catalyst = register("catalyst", Resource("Catalyst", Symbols.catalyst, isElement = true, color = "#f2ef33"))
    val a = register("element_a", Resource("A", Symbols.a, isElement = true, color = "#b0743c"))
    val b = register("element_b", Resource("B", Symbols.b, isElement = true, color = "#b0c73c"))
    val c = register("element_c", Resource("C", Symbols.c, isElement = true, color = "#38961b"))
    val d = register("element_d", Resource("D", Symbols.d, isElement = true, color = "#1b9673"))
    val e = register("element_e", Resource("E", Symbols.e, isElement = true, color = "#325269"))
    val f = register("element_f", Resource("F", Symbols.f, isElement = true, color = "#8b2c94"))
    val g = register("element_g", Resource("G", Symbols.g, isElement = true, color = "#7d134a"))
    val heat = register("heat", Resource("Heat", Symbols.heat, isDecimal = true, isElement = true))

    val deltaCatalyst = register("delta_catalyst", Resource("Delta Catalyst", "${Symbols.delta}${Symbols.catalyst}"))
    val deltaA = register("delta_element_a", Resource("Delta A", "${Symbols.delta}${Symbols.a}"))
    val deltaB = register("delta_element_b", Resource("Delta B", "${Symbols.delta}${Symbols.b}"))
    val deltaC = register("delta_element_c", Resource("Delta C", "${Symbols.delta}${Symbols.c}"))
    val deltaD = register("delta_element_d", Resource("Delta D", "${Symbols.delta}${Symbols.d}"))
    val deltaE = register("delta_element_e", Resource("Delta E", "${Symbols.delta}${Symbols.e}"))
    val deltaF = register("delta_element_f", Resource("Delta F", "${Symbols.delta}${Symbols.f}"))
    val deltaG = register("delta_element_g", Resource("Delta G", "${Symbols.delta}${Symbols.g}"))
    val deltaHeat = register("delta_heat",
        Resource("Delta Heat", "${Symbols.delta}${Symbols.heat}", isDecimal = true)
    )

    val omega = register("omega", Resource("Omega", Symbols.omega))
    val alpha = register("alpha", Resource("Alpha", Symbols.alpha))

    val dualities = register("dualities", Resource("Dualities", Symbols.mu))
}