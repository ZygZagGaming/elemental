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
    GameTimer.registerTicker {
        for ((symbol, element) in elements)
            DynamicHTMLManager.setVariable("element-$symbol-amount", "${gameState.elementAmounts[element]}")

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
            DynamicHTMLManager.setVariable("special-reaction-$i-effects", reaction.stringEffects(reaction.nTimesUsed + 1))
            DynamicHTMLManager.idSetClassPresence("special-reaction-option-$i", "disabled", !gameState.canDoReaction(reaction) && !reaction.hasBeenUsed)
            DynamicHTMLManager.idSetClassPresence("special-reaction-option-$i", "active", reaction.hasBeenUsed)
            DynamicHTMLManager.idSetDataVariable("special-reaction-option-$i", "backendReactionId", backendId)
        }

        doCircleShit()
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
            reactionListScrollAmount = min(max(0.0, reactionListScrollAmount + reactionListScrollSens * evt.deltaY),
                maxScrollAmount
            )
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
            reactionListScrollAmount = min(max(0.0, reactionListScrollAmount + reactionListScrollSens * evt.deltaY),
                maxScrollAmount
            )
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
    loadLocalStorage()
    var lastSave = 0.0
    GameTimer.registerTicker {
        if (GameTimer.timeSex() - lastSave >= Options.saveInterval) {
            saveLocalStorage()
            lastSave = GameTimer.timeSex()
        }
    }
    GameTimer.registerTicker(gameState::tick)
    GameTimer.beginTicking()
}

fun sign(n: Int) = if (n < 0) -1 else if (n > 0) 1 else 0

object GameTimer {
    private val date get() = Date()
    fun timeMillis() = date.getTime()
    fun timeSex() = timeMillis() / 1000.0
    private val tickers = mutableListOf<Ticker>()
    private val namedTickers = mutableMapOf<String, Ticker>()
    fun registerTicker(ticker: Ticker) {
        tickers += ticker
    }
    fun registerNamedTicker(name: String, ticker: Ticker) {
        namedTickers[name] = ticker
    }
    var lastTick = timeSex()
    fun tick(dt: Double) {
        for (ticker in tickers) ticker(dt)
        for ((_, ticker) in namedTickers) ticker(dt)
    }

    fun beginTicking() {
        val dt = timeSex() - lastTick
        tick(dt)
        window.setTimeout({ beginTicking() }, 1)
        lastTick = timeSex()
    }
}

object DynamicHTMLManager {
    val variables = mutableMapOf<String, String>()
    var shownPage = "elements"
    set (value) {
        field = value
        pages.toList().forEach {
            if (it.dataset["pageId"] == shownPage) it.classList.remove("hidden")
            else it.classList.add("hidden")
        }
        pageButtons.toList().forEach {
            if (it.dataset["pageId"] == shownPage) it.classList.add("active")
            else it.classList.remove("active")
        }
    }
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
    val pages get() = document.getElementsByClassName("page")
    val pageButtons get() = document.getElementsByClassName("page-button")

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
        for (pageButton in pageButtons) {
            val pageId = pageButton.dataset["pageId"]
            val page = Pages.map[pageId]
            val child = pageButton.children.toList().firstOrNull { it.classList.contains("page-text") }
            if (pageId != null && page != null) {
                if (child != null) {
                    child.textContent = page.name
                }
                pageButton.onclick = {
                    shownPage = pageId

                    Unit
                }
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

class GameState {
    val elementAmounts: MutableDefaultedMap<ElementType, Double> = defaultMutableStack.toMutableDefaultedMap(0.0)
    var hoveredReaction: Reaction = NullReaction
    var incoming = defaultMutableStack
    var lastAmounts = elementAmounts
    var lastReaction = hoveredReaction
    var timeSpent: Double = 0.0
    var automationTickers = mutableMapOf(
        "a_to_b" to AutomationTicker(0.0) { attemptReaction(NormalReactions.aToB) }
    )

    fun addAutomationTicker(name: String, automationTicker: AutomationTicker) {
        automationTickers[name] = automationTicker
    }

    fun tick(dt: Double, cap: Boolean = true) {
        lastAmounts = elementAmounts.copy().toMutableDefaultedMap(0.0)
        lastReaction = hoveredReaction
        timeSpent += dt

        automationTickers.values.forEach { it.tick(dt) }

        elementAmounts.deduct(Elements.heat.withCount(elementAmounts[Elements.heat] * 0.1 * dt * Stats.gameSpeed))
        if (cap) for ((key, value) in incoming) elementAmounts[key] = min(Stats.functionalElementCaps[key], max(0.0,
            elementAmounts[key].plus(value)
        ))
        incoming = defaultMutableStack
    }

    fun canDoReaction(reaction: Reaction) =
        ((reaction !is NullReaction)
                && reaction.inputs.all { (k, v) -> v <= elementAmounts[k] }
                && reaction.multipliedOutputs.none { (k, v) -> v + elementAmounts[k] - reaction.inputs[k] > Stats.functionalElementCaps[k] })

    fun attemptReaction(reaction: Reaction) {
        if (canDoReaction(reaction)) {
            elementAmounts.deduct(reaction.multipliedInputs)
            incoming.add(reaction.multipliedOutputs)
            if (reaction is SpecialReaction) reaction.execute()
        }
    }

    override fun toString(): String {
        return super.toString()
    }
}

fun MutableElementStack.copy(): MutableElementStack = toMutableMap().toMutableDefaultedMap(defaultValue)

fun MutableElementStack.deduct(other: ElementStack) {
    for ((key, value) in other) this[key] = this[key].minus(value)
}

fun MutableElementStack.add(other: ElementStack) {
    for ((key, value) in other) this[key] = this[key].plus(value)
}

val defaultStack get() = elements.values.associateWith { 0.0 }.toDefaultedMap(0.0)
val defaultMutableStack get() = defaultStack.toMutableMap().toMutableDefaultedMap(0.0)

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


data class ElementType(val name: String, val symbol: Char) {
    fun withCount(n: Double): ElementStack = mapOf(this to n).toDefaultedMap(0.0)
}

typealias ElementStack = DefaultedMap<ElementType, Double>
typealias MutableElementStack = MutableDefaultedMap<ElementType, Double>
open class Reaction(val name: String) {
    open var inputs: ElementStack = defaultStack.toDefaultedMap(0.0)
    open var outputs: ElementStack = defaultStack.toDefaultedMap(0.0)
    val multipliedInputs get() = inputs.entries.associate { (k, v) -> k to v * Stats.reactionEfficiencies[this] }.toDefaultedMap(0.0)
    val multipliedOutputs get() = outputs.entries.associate { (k, v) -> k to v * Stats.elementMultipliers[k] * Stats.reactionEfficiencies[this] }.toDefaultedMap(0.0)

    companion object {
        operator fun invoke(name: String, inputs: ElementStack, outputs: ElementStack) = object : Reaction(name) {
            override var inputs: ElementStack = inputs
            override var outputs: ElementStack = outputs
        }
    }

    override fun toString(): String {
        if (this is NullReaction) return "-"
        return "${multipliedInputs.format()} ⇒ ${multipliedOutputs.format()}"
    }
}

class SpecialReaction(name: String, val inputsSupplier: (Int) -> ElementStack, val outputsSupplier: (Int) -> ElementStack = { defaultStack }, val effects: (Int) -> Unit = { }, val stringEffects: (Int) -> String = { "" }, val usageCap: Int = 1): Reaction(name) {
    override var inputs: ElementStack
        get() = inputsSupplier(nTimesUsed + 1)
        set(_) {}
    override var outputs: ElementStack
        get() = outputsSupplier(nTimesUsed + 1)
        set(_) {}
    val hasBeenUsed get() = nTimesUsed >= usageCap
    var nTimesUsed = 0
    fun execute() {
        nTimesUsed++
        effects(nTimesUsed)
    }
}

fun Double.toString1DecPlace(): String {
    if (abs(this % 1.0) <= 1e-7) return toString().split('.')[0]
    val str = (this * 10).toString()
    return str.substring(0 until str.length - 1) + '.' + str.last()
}

fun ElementStack.format(): String = filter { (k, v) -> v != 0.0 }.map { (k, v) -> "${if (v == 1.0) "" else v.toString1DecPlace()}${k.symbol}" }.joinToString(" + ")

object NullReaction: Reaction("") {
    override var inputs: ElementStack = defaultStack
    override var outputs: ElementStack = defaultStack
}

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