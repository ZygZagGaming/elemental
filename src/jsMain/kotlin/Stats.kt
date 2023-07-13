object Stats {
    val elementMultipliers = mutableMapOf<ElementType, Double>().toMutableDefaultedMap(1.0)
    val baseElementUpperBounds =
        BasicMutableStatMap(mapOf(Elements.catalyst to 100000.0, Elements.heat to 10.0), 1000.0)
    val elementUpperBoundMultipliers = BasicMutableStatMap<ElementType, Double>(emptyMap(), 1.0)
    val functionalElementUpperBounds =
        ProductStatMap(baseElementUpperBounds, elementUpperBoundMultipliers, Double::times)
    val baseElementLowerBounds = BasicMutableStatMap<ElementType, Double>(emptyMap(), 0.0)
    val elementLowerBoundMultipliers = BasicMutableStatMap<ElementType, Double>(emptyMap(), 1.0)
    val functionalElementLowerBounds =
        ProductStatMap(baseElementLowerBounds, elementLowerBoundMultipliers, Double::times)
    var gameSpeed = 0.0
    val elementAmounts = BasicMutableStatMap(defaultElements, 0.0)
    var elementAmountsCached: MutableList<Map<ElementType, Double>> = mutableListOf()
    var elementDeltas = BasicMutableStatMap<ElementType, Double>(emptyMap(), 0.0)
    var elementRates = BasicMutableStatMap<ElementType, Double>(emptyMap(), 0.0)
    //var elementDeltasUnspent = BasicMutableStatMap<ElementType, Double>(emptyMap(), 0.0)
    var lastTickDt = 0.0
    val flags = mutableSetOf<String>()

    fun resetDeltas() {
        Elements.values.forEach {
            if (it.isBasic) elementAmounts[it.delta] = elementDeltas[it]
        }
    }
}