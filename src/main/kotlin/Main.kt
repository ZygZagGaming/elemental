@file:Suppress("UNUSED_VARIABLE", "MemberVisibilityCanBePrivate", "MoveLambdaOutsideParentheses")

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.hasClass
import org.w3c.dom.*
import kotlin.js.Date
import kotlin.math.*

fun main() {
    console.log("Hello, black men twerking")
    //val load = { load() }
}

lateinit var gameState: GameState

var reactionListScrollAmount = 0.0
var reactionListScrollSens = 0.4

@OptIn(ExperimentalJsExport::class)
@Suppress("RedundantUnitExpression")
@JsExport
fun loadGame() {
    doCircleShit()

    gameState = GameState()
    gameState.elementAmounts[Elements.catalyst] = 1.0
    gameState.elementAmounts[Elements.a] = 1.0
    GameTimer.registerTicker(gameState::tick)
    GameTimer.registerTicker {
        for ((symbol, element) in elements)
            DynamicHTMLManager.setVariable("element-$symbol-amount", "${gameState.elementAmounts[element] ?: 0}")

        for ((i, entry) in NormalReactions.map.entries.withIndex()) {
            val (backendId, reaction) = entry
            DynamicHTMLManager.setVariable("reaction-$i-title", reaction.name)
            DynamicHTMLManager.setVariable("reaction-$i-description", reaction.toString())
            DynamicHTMLManager.idSetClassPresence("reaction-option-$i", "disabled", !gameState.canDoReaction(reaction))
            DynamicHTMLManager.idSetDataVariable("reaction-option-$i", "backendReactionId", backendId)
        }

        for ((i, entry) in SpecialReactions.map.entries.withIndex()) {
            val (backendId, reaction) = entry
            DynamicHTMLManager.setVariable("special-reaction-$i-title", reaction.name)
            DynamicHTMLManager.setVariable("special-reaction-$i-description", reaction.toString())
            DynamicHTMLManager.setVariable("special-reaction-$i-effects", reaction.stringEffects)
            DynamicHTMLManager.idSetClassPresence("special-reaction-option-$i", "disabled", !gameState.canDoReaction(reaction) && !reaction.hasBeenUsed)
            DynamicHTMLManager.idSetClassPresence("special-reaction-option-$i", "active", reaction.hasBeenUsed)
            DynamicHTMLManager.idSetDataVariable("special-reaction-option-$i", "backendReactionId", backendId)
        }
    }
    GameTimer.registerTicker {
        DynamicHTMLManager.tick()
    }
    GameTimer.registerTicker {
        visuals(gameState)
    }
    val container1 = document.getElementById("reaction-container")
    if (container1 is HTMLElement) container1.apply {
        onwheel = { evt ->
            val rows = ceil(container1.children.length / 3.0)
            val maxScrollAmount = container1.children.toList().sumOf { it.clientHeight } + rows * 16 + 6 /*10px margin + 3px border on both sides*/ - container1.clientHeight
            reactionListScrollAmount = min(max(0.0, reactionListScrollAmount + reactionListScrollSens * evt.deltaY), maxScrollAmount.toDouble())
            children.iterator().forEach { it.style.top = (-reactionListScrollAmount).px }

            Unit // not kotlin begging me for a return
        }
        for ((child, i) in children.indexed) {
            child.onclick = { _ ->
                val reaction = NormalReactions.map[child.dataset["backendReactionId"]!!]!!
                gameState.attemptReaction(reaction)

                Unit
            }
            child.onmouseenter = { _ ->
                val reaction = NormalReactions.map[child.dataset["backendReactionId"]!!]!!
                gameState.hoveredReaction = reaction

                Unit
            }
            child.onmouseleave = { _ ->
                val reaction = NormalReactions.map[child.dataset["backendReactionId"]!!]!!
                gameState.hoveredReaction = NullReaction

                Unit
            }
        }
    }
    val container2 = document.getElementById("special-reaction-container")
    if (container2 is HTMLElement) container2.apply {
        onwheel = { evt ->
            val rows = ceil(children.length / 3.0)
            val maxScrollAmount = children.toList().sumOf { it.clientHeight } + rows * 16 + 6 /*10px margin + 3px border on both sides*/ - container2.clientHeight
            reactionListScrollAmount = min(max(0.0, reactionListScrollAmount + reactionListScrollSens * evt.deltaY), maxScrollAmount.toDouble())
            children.iterator().forEach { it.style.top = (-reactionListScrollAmount).px }

            Unit // not kotlin begging me for a return
        }
        for ((child, i) in children.indexed) {
            child.onclick = { _ ->
                val reaction = SpecialReactions.map[child.dataset["backendReactionId"]!!]!!
                if (!reaction.hasBeenUsed) {
                    gameState.attemptReaction(reaction)
                }

                Unit
            }
            child.onmouseenter = { _ ->
                val reaction = SpecialReactions.map[child.dataset["backendReactionId"]!!]!!
                gameState.hoveredReaction = reaction

                Unit
            }
            child.onmouseleave = { _ ->
                val reaction = SpecialReactions.map[child.dataset["backendReactionId"]!!]!!
                gameState.hoveredReaction = NullReaction

                Unit
            }
        }
    }
    GameTimer.tick()
}

fun sign(n: Int) = if (n < 0) -1 else if (n > 0) 1 else 0

object GameTimer {
    private val date get() = Date()
    fun timeMillis() = date.getTime()
    fun timeSex() = timeMillis() / 1000.0
    private val tickers = mutableListOf<Ticker>()
    fun registerTicker(ticker: Ticker) {
        tickers += ticker
    }
    var lastTick = timeSex()
    fun tick() {
        val dt = timeSex() - lastTick
        for (ticker in tickers) ticker(dt)

        window.setTimeout({ tick() }, 1)
        lastTick = timeSex()
    }
}

object DynamicHTMLManager {
    val variables = mutableMapOf<String, String>()
    fun setVariable(id: String, value: String) {
        variables[id] = value
    }

    fun setDataVariable(classToSelect: String, variable: String, value: String) {
        for (dyn in dynamix) if (dyn.classList.contains(classToSelect)) dyn.dataset[variable] = value
    }

    fun idSetDataVariable(id: String, variable: String, value: String) {
        val element = document.getElementById(id)
        if (element is HTMLElement) element.dataset[variable] = value
    }

    val dynamix get() = document.getElementsByClassName("dynamic")

    fun setElementClass(selectedClass: String, classToSet: String) {
        for (dyn in dynamix) if (dyn.classList.contains(selectedClass)) dyn.className = classToSet
    }

    fun addElementClass(selectedClass: String, classToAdd: String) {
        for (dyn in dynamix) if (dyn.classList.contains(selectedClass) && !dyn.classList.contains(classToAdd)) dyn.className += " $classToAdd"
    }

    fun removeElementClass(selectedClass: String, classToRemove: String) {
        for (dyn in dynamix) if (dyn.classList.contains(selectedClass) && dyn.classList.contains(classToRemove)) dyn.classList.remove(classToRemove)
    }

    fun idAddElementClass(id: String, classToAdd: String) {
        document.getElementById(id)?.classList?.add(classToAdd)
    }

    fun idRemoveElementClass(id: String, classToRemove: String) {
        document.getElementById(id)?.classList?.remove(classToRemove)
    }

    fun setClassPresence(selectedClass: String, classToSet: String, value: Boolean) {
        if (value) addElementClass(selectedClass, classToSet)
        else removeElementClass(selectedClass, classToSet)
    }

    fun idSetClassPresence(id: String, classToSet: String, value: Boolean) {
        if (value) idAddElementClass(id, classToSet)
        else idRemoveElementClass(id, classToSet)
    }

    fun tick() {
        for (dyn in dynamix) {
            val dataId = dyn.dataset["dynamicId"]
            val data = variables[dataId]
            if (data != null) {
                dyn.textContent = data
            }
        }
    }
}

open class AutomationTicker(var rateHertz: Double, var effects: () -> Unit) {
    var timer = 0.0
    val amountOfTime get() = if (rateHertz == 0.0) Double.POSITIVE_INFINITY else 1 / rateHertz
    fun tick(dt: Double) {
        timer += dt
        while (timer >= amountOfTime) {
            timer -= amountOfTime
            effects()
        }
    }
}

data class GameState(
    var hoveredReaction: Reaction = NullReaction,
    val elementAmounts: MutableElementStack = defaultMutableStack,
    var timeSpent: Double = 0.0,
    var elementCaps: Map<ElementType, Double> = defaultCaps.toMap()
) {
    var incoming = defaultMutableStack
    var lastAmounts = elementAmounts
    var lastReaction = hoveredReaction
    var automationTickers = mutableMapOf(
        "a_to_b" to AutomationTicker(0.0) { attemptReaction(NormalReactions.aToB) }
    )

    fun addAutomationTicker(name: String, automationTicker: AutomationTicker) {
        automationTickers[name] = automationTicker
    }

    fun tick(dt: Double) {
        lastAmounts = elementAmounts.copy()
        lastReaction = hoveredReaction
        timeSpent += dt

        automationTickers.values.forEach { it.tick(dt) }

        elementAmounts.deduct(Elements.heat.withCount(elementAmounts[Elements.heat]!! * 0.1 * dt * Options.gameSpeed))
        for ((key, value) in incoming) elementAmounts[key] = min(elementCaps[key]!!, max(0.0, elementAmounts[key]?.plus(value) ?: 0.0))
        incoming = defaultMutableStack
    }

    fun canDoReaction(reaction: Reaction) =
        ((reaction !is NullReaction)
                && reaction.inputs.all { (k, v) -> v <= (elementAmounts[k] ?: 0.0) }
                && reaction.outputs.none { (k, v) -> v + (elementAmounts[k] ?: 0.0) > (elementCaps[k] ?: 0.0) })

    fun attemptReaction(reaction: Reaction) {
        if (canDoReaction(reaction)) {
            elementAmounts.deduct(reaction.inputs)
            incoming.add(reaction.outputs)
            if (reaction is SpecialReaction) reaction.execute()
        }
    }
}

fun MutableElementStack.copy(): MutableElementStack = toMutableMap()

fun MutableElementStack.deduct(other: ElementStack) {
    for ((key, value) in other) this[key] = this[key]?.minus(value) ?: 0.0
}

fun MutableElementStack.add(other: ElementStack) {
    for ((key, value) in other) this[key] = this[key]?.plus(value) ?: 0.0
}

val defaultStack get() = elements.values.associateWith { 0.0 }
val defaultMutableStack get() = defaultStack.toMutableMap()

typealias Ticker = (Double) -> Unit

data class Vec2(val x: Double, val y: Double) {
    operator fun plus(other: Vec2) = Vec2(x + other.x, y + other.y)
    operator fun minus(other: Vec2) = Vec2(x - other.x, y - other.y)
    operator fun times(other: Double) = Vec2(x * other, y * other)
    operator fun div(other: Double) = Vec2(x / other, y / other)
    val transpose get() = Vec2(y, x)
    val magnitude get() = sqrt(x * x + y * y)
    val normalized get() = this / magnitude
}

fun getAlchemyElementPos(symbol: Char): Vec2 {
    val id = symbol.code - Symbols.a.code + 1
    if (id <= 7) {
        val angle = 2 * PI * (id - 1) / 7
        return if (id == 0) Vec2(0.0, 0.0) else Vec2(sin(angle), -cos(angle))
    }
    return Vec2(0.0, 0.0)
}

fun doCircleShit() {
    val alchemyContainers = document.getElementsByClassName("alchemy-container")
    for (alchemyContainer in alchemyContainers) {
        val half = alchemyContainer.getBoundingClientRect().width / 2
        val radius = half * 0.8
        val elements = alchemyContainer.children
        for ((element, i) in elements.indexed) {
            if (element.hasClass("alchemy-element")) {
                val symbol = element.dataset["element"]
                if (symbol != null) {
                    val halfElem = element.getBoundingClientRect().width / 2
                    val normalizedPos = getAlchemyElementPos(symbol[0]) // should only be 1 char
                    val pos = Vec2(half - halfElem, half - halfElem) + normalizedPos * radius
                    element.style.left = pos.x.px
                    element.style.top = pos.y.px
                }
            }
        }
    }
}

fun visuals(gameState: GameState) {
    val alchemyContainers = document.getElementsByClassName("alchemy-container")
    for (alchemyContainer in alchemyContainers) {
        val elements = alchemyContainer.children
        val half = alchemyContainer.getBoundingClientRect().width / 2
        val radius = half * 0.8
        val n = elements.length - 2
        val portion = 2 * PI / n
        val visuals = elements.toList().last() as HTMLCanvasElement
        val middle = Vec2(half, half)
        val context = (visuals.getContext("2d") as CanvasRenderingContext2D).apply {
            strokeStyle = "#000000"
            beginPath()
            moveTo(0.0, 0.0)
            lineTo(500.0, 0.0)
            lineTo(500.0, 500.0)
            lineTo(0.0, 500.0)
            fill()

            strokeStyle = "#ffffff"
            val polygonRadius = (half + radius) * 0.5
            lineWidth = 3.0
            beginPath()
            moveTo(half, half + polygonRadius)
            for (i in 1..n) {
                lineTo(half + polygonRadius * sin(portion * i), half + polygonRadius * cos(portion * i))
            }
            stroke()

            val k = 0.5
            val angle = graphicalHeatAmount * 2 * PI
            val strokeWidth = 10.0
            if (gameState.elementAmounts[Elements.heat]!! <= 1e-6) {
                lineWidth = strokeWidth
                beginPath()
                strokeStyle = "#cccccc"
                arc(half, half, 30.0 + strokeWidth / 2, 0.0, 2 * PI)
                stroke()
            } else {
                val big = graphicalHeatAmount > 0.75
                lineWidth = strokeWidth * (if (big) 3.0 / 2 else 1.0)
                beginPath()
                strokeStyle = if (big) createRadialGradient(half, half, 30.0, half, half, 30.0 + strokeWidth * 3 / 2).apply {
                    addColorStop(0.0, "rgba(255, 0, 0, 1)")
                    addColorStop(0.66, "rgba(255, 0, 0, 1)")
                    addColorStop(1.0, "rgba(255, 0, 0, 0)")
                } else "#ff0000"
                arc(half, half, 30.0 + strokeWidth * (if (big) 2 else 1) / 2, k * PI, k * PI + angle)
                stroke()

                lineWidth = strokeWidth
                beginPath()
                strokeStyle = "#cccccc"
                arc(half, half, 30.0 + strokeWidth / 2, k * PI + angle, (k + 2) * PI)
                stroke()
            }

            val reaction = gameState.hoveredReaction
            val canDoReaction = gameState.canDoReaction(reaction)
            for ((element, _) in reaction.inputs) {
                if (element.symbol !in listOf(Symbols.catalyst, Symbols.heat)) {
                    lineWidth = 10.0
                    val relative = getAlchemyElementPos(element.symbol) * radius
                    if (canDoReaction) {
                        gradientLineColorBar(
                            middle,
                            middle + relative,
                            255,
                            255,
                            0,
                            1.0,
                            10.0,
                            (-(gameState.timeSpent * 200 + element.hashCode() * 800) / relative.magnitude).mod(1.0),
                            0.2,
                            0.65
                        )
                    } else {
                        gradientLine(middle, middle + relative, 255, 255, 0, 0.5, 10.0)
                    }
                }
            }

            for ((element, _) in reaction.outputs) {
                if (element.symbol !in listOf(Symbols.catalyst, Symbols.heat)) {
                    lineWidth = 10.0
                    val relative = getAlchemyElementPos(element.symbol) * radius
                    if (canDoReaction) {
                        gradientLineColorBar(
                            middle,
                            middle + relative,
                            255,
                            0,
                            255,
                            1.0,
                            10.0,
                            ((gameState.timeSpent * 200 + element.hashCode() * 225) / relative.magnitude).mod(1.0),
                            0.2,
                            0.65
                        )
                    } else {
                        gradientLine(middle, middle + relative, 255, 0, 255, 0.5, 10.0)
                    }
                }
            }
        }

    }
}



fun CanvasRenderingContext2D.gradientLine(posA: Vec2, posB: Vec2, r: Int, g: Int, b: Int, a: Double, width: Double) {
    val len = (posA - posB).magnitude
    val avg = (posA + posB) / 2.0
    val diff = posA - posB
    val transpose = Vec2(-diff.y, diff.x).normalized * width// / 2.0
    val p1 = avg + transpose
    val p2 = avg - transpose
    val gradient = createLinearGradient(p1.x, p1.y, p2.x, p2.y)
    gradient.addColorStop(0.0, "rgba($r, $g, $b, 0.0)")
    gradient.addColorStop(0.5, "rgba($r, $g, $b, $a)")
    gradient.addColorStop(1.0, "rgba($r, $g, $b, 0.0)")
    strokeStyle = gradient
    beginPath()
    moveTo(posA)
    lineTo(posB)
    stroke()

}

val graphicalHeatAmount get() = (gameState.elementAmounts[Elements.heat] ?: 0.0) / (gameState.elementCaps[Elements.heat] ?: 0.0)

fun CanvasRenderingContext2D.gradientLineColorBar(posA: Vec2, posB: Vec2, r: Int, g: Int, b: Int, a: Double, width: Double, colorBarPosition: Double, colorBarWidth: Double, colorBarOpacity: Double) {
    val len = (posA - posB).magnitude
    val avg = (posA + posB) / 2.0
    val diff = posA - posB
    val transpose = Vec2(-diff.y, diff.x).normalized * width / 2.0
    val p1 = avg + transpose
    val p2 = avg - transpose
    val gradient = createLinearGradient(p1.x, p1.y, p2.x, p2.y)
    gradient.addColorStop(0.0, "rgba($r, $g, $b, 0.0)")
    gradient.addColorStop(0.5, "rgba($r, $g, $b, $a)")
    gradient.addColorStop(1.0, "rgba($r, $g, $b, 0.0)")
    strokeStyle = gradient
    beginPath()
    moveTo(posA)
    lineTo(posB)
    stroke()

    val secondaryGradient = createLinearGradient(posA.x, posA.y, posB.x, posB.y)
    secondaryGradient.addColorStop(0.0, "rgba(255, 255, 255, 0.0)")
    secondaryGradient.addColorStop(max(0.0, colorBarPosition - colorBarWidth), "rgba(255, 255, 255, 0.0)")
    secondaryGradient.addColorStop(colorBarPosition, "rgba(255, 255, 255, $colorBarOpacity)")
    secondaryGradient.addColorStop(min(1.0, colorBarPosition + colorBarWidth), "rgba(255, 255, 255, 0.0)")
    secondaryGradient.addColorStop(1.0, "rgba(255, 255, 255, 0.0)")
    strokeStyle = secondaryGradient
    beginPath()
    moveTo(posA)
    lineTo(posB)
    stroke()
}

fun CanvasRenderingContext2D.lineTo(vec2: Vec2) {
    lineTo(vec2.x, vec2.y)
}

fun CanvasRenderingContext2D.moveTo(vec2: Vec2) {
    moveTo(vec2.x, vec2.y)
}

data class ElementType(val name: String, val symbol: Char) {
    fun withCount(n: Double): ElementStack = mapOf(this to n)
}

typealias ElementStack = Map<ElementType, Double>
typealias MutableElementStack = MutableMap<ElementType, Double>
open class Reaction(val name: String, var inputs: ElementStack, var outputs: ElementStack) {
    override fun toString(): String {
        if (this is NullReaction) return "-"
        return "${inputs.format()} ⇒ ${outputs.format()}"
    }
}

class SpecialReaction(name: String, inputs: ElementStack, outputs: ElementStack = mapOf(), val effects: () -> Unit = { }, val stringEffects: String = ""): Reaction(name, inputs, outputs) {
    var hasBeenUsed = false
    fun execute() {
        effects()
        hasBeenUsed = true
    }
}

fun ElementStack.format(): String = map { (k, v) -> "${if (v == 1.0) "" else v}${k.symbol}" }.joinToString(" + ")

object NullReaction: Reaction("", emptyMap(), emptyMap())

val Int.px get() = "${this}px"
val Double.px get() = "${roundToInt()}px"

fun HTMLCollection.toList(): List<HTMLElement> {
    val list = mutableListOf<HTMLElement>()
    for (index in 0..length) {
        val elem = get(index)
        if (elem is HTMLElement) list.add(elem)
    }
    return list
}

data class Indexed<T>(val t: T, val index: Int)

operator fun HTMLCollection.iterator() = toList().iterator()

val HTMLCollection.indices get() = toList().indices
val HTMLCollection.indexed get() = toList().zip(indices) { a, b -> Indexed(a, b) }