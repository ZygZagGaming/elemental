@file:Suppress("UNUSED_VARIABLE", "MemberVisibilityCanBePrivate", "MoveLambdaOutsideParentheses")

import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.dom.hasClass
import org.w3c.dom.*
import kotlin.math.*

fun main() {
    console.log("Hello!")
    console.log("You are currently playing Elemental $gameVersion!")
}

lateinit var gameState: GameState

var reactionListScrollAmount = 0.0
var reactionListScrollSens = 0.4
const val gameVersion = "v1.1.0"

@OptIn(ExperimentalJsExport::class)
@JsExport
fun resetSave() {
    localStorage.clear()
    window.location.reload()
}

val notation = RateOfChangeNotation.MAXPERSEC

@OptIn(ExperimentalJsExport::class)
@Suppress("RedundantUnitExpression")
@JsExport
fun loadGame() {
    doCircleShit()
    document.getElementById("title")?.textContent = "Elemental $gameVersion"
    ContextMenu.applyEventListeners()

    gameState = GameState()
    ContextMenu.init()
    GameTimer.registerTicker("HTML updates") {
        val prefix = notation.prefix
        val suffix = notation.suffix
        for ((name, element) in Elements.map) {
            val symbol = element.symbol
            DynamicHTMLManager.setVariable(
                "element-$symbol-amount",
                "${if (element.isDecimal) Stats.elementAmounts[element].roundTo(2) else floor(Stats.elementAmounts[element])}"
            )
            DynamicHTMLManager.setVariable(
                "$symbol-amount-display",
                "$symbol = ${floor(Stats.elementAmounts[element])}"
            )
            DynamicHTMLManager.setVariable(
                "$symbol-bounds-display",
                "${Stats.functionalElementLowerBounds[element].roundTo(2)} ≤ $symbol ≤ ${Stats.functionalElementUpperBounds[element].roundTo(2)}"
            )
            DynamicHTMLManager.setVariable(
                "$symbol-rate-display",
                "current $symbol / s = ${Stats.elementRates[element].roundTo(2)}"
            )
            DynamicHTMLManager.setVariable(
                "$symbol-max-rate-display",
                "$prefix$symbol$suffix = ${Stats.elementDeltas[element].roundTo(2)}"
            )
        }

        for ((id, keybind) in Input.keybinds) {
            DynamicHTMLManager.setVariable(
                "keybind-$id-key",
                keybind.key.key
            )
        }

        for ((id, clicker) in gameState.clickersById) {
            DynamicHTMLManager.setVariable(
                "clicker-$id-mode",
                clicker.mode.pretty
            )
        }

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
    GameTimer.registerTicker("DynamicHTMLManager tick") {
        DynamicHTMLManager.tick()
    }
    GameTimer.registerTicker("Visuals") {
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
                val reaction = NormalReactions.map[child.dataset["backendReactionId"] ?: ""]
                if (reaction != null) gameState.attemptReaction(reaction)

                Unit
            }
            child.onmouseenter = { _ ->
                val reaction = NormalReactions.map[child.dataset["backendReactionId"] ?: ""]
                if (reaction != null) gameState.hoveredReaction = reaction

                Unit
            }
            child.onmouseleave = { _ ->
                val reaction = NormalReactions.map[child.dataset["backendReactionId"] ?: ""]
                if (reaction != null) gameState.hoveredReaction = NullReaction

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
    GameTimer.registerTicker("Saving") {
        if (GameTimer.timeSex() - lastSave >= Options.saveInterval) {
            saveLocalStorage()
            lastSave = GameTimer.timeSex()
        }
    }
    GameTimer.registerTicker("gameState tick", gameState::tick)
    GameTimer.beginTicking()
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
                if (symbol != null && symbol != "h") {
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


