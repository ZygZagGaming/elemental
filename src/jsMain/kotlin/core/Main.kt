@file:Suppress("UNUSED_VARIABLE", "MemberVisibilityCanBePrivate", "MoveLambdaOutsideParentheses")

package core

import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.dom.hasClass
import libraries.*
import org.w3c.dom.*
import kotlin.math.*

fun main() {
    console.log("Hello!")
    console.log("You are currently playing Elemental $gameVersion!")
}

lateinit var gameState: GameState

var reactionListScrollAmount = 0.0
var reactionListScrollSens = 0.4
const val gameVersion = "v2.0.0"

@OptIn(ExperimentalJsExport::class)
@JsExport
fun resetSave() {
    localStorage.clear()
    window.location.reload()
}

val notation = RateOfChangeNotation.MAXPERSEC
const val htmlUpdateInterval = 5.0
var lastHtmlUpdate = 0.0

@OptIn(ExperimentalJsExport::class)
@Suppress("RedundantUnitExpression")
@JsExport
fun loadGame() {
    DynamicHTMLManager.setupHTML()
    document.getElementById("title")?.textContent = "Elemental $gameVersion"
    ContextMenu.applyEventListeners()

    gameState = GameState()
    ContextMenu.init()
    GameTimer.registerTicker("HTML updates") {
        val prefix = notation.prefix
        val suffix = notation.suffix
        val updateAll = GameTimer.timeSex() - lastHtmlUpdate > htmlUpdateInterval
        if (updateAll) lastHtmlUpdate = GameTimer.timeSex()
        DynamicHTMLManager.apply {
            for ((name, element) in Resources.map) {
                val n = if (element == Resources.catalyst) 0 else (element.symbol[0] - 'a' + 1)
                val symbol = element.symbol
                if (updateAll || Stats.elementAmounts.changed(element)) {
                    val displayText =
                        "${if (element.isDecimal) Stats.elementAmounts[element].roundTo(2) else floor(Stats.elementAmounts[element])}"
                    setVariable(
                        "element-$symbol-amount",
                        displayText
                    )
                    setVariable(
                        "$symbol-amount-display",
                        "$symbol = $displayText"
                    )
                    if (element == Resources.catalyst) setClassPresence(
                        "duality-button",
                        "disabled",
                        !Flags.reachedDuality.isUnlocked()
                    )
                    if (element == Resources.dualities) {
                        idSetClassPresence(
                            "big-duality-button",
                            "hidden",
                            Stats.elementAmounts[Resources.dualities] > 0
                        )
                        idSetClassPresence(
                            "main-duality-page",
                            "hidden",
                            Stats.elementAmounts[Resources.dualities] <= 0
                        )
                    }

                    if (element.isElement && element != Resources.heat) {
                        val color = element.color
                        var cutoff = (Stats.elementAmounts[element] - Stats.functionalElementLowerBounds[element]) * 100 / (Stats.functionalElementUpperBounds[element] - Stats.functionalElementLowerBounds[element])
                        cutoff += 50
                        cutoff %= 100
                        cutoff = cutoff.clamp(0.0..99.99)
                        val gradient = "linear-gradient(#909090, #909090), " +
                                if (cutoff <= 0.01) {
                                    "conic-gradient(white 0%, white 50%, $color 50.01%, $color 100%)"
                                } else if (abs(cutoff - 50.0) < 0.01) {
                                    if (Stats.elementAmounts[element] * 2 < Stats.functionalElementUpperBounds[element] + Stats.functionalElementLowerBounds[element]) "conic-gradient(white 0%, white 100%)"
                                    else "conic-gradient($color 0%, $color 100%)"
                                } else if (cutoff < 50) {
                                    "conic-gradient($color 0%, $color $cutoff%, white ${cutoff + 0.01}%, white 50%, $color 50.01%, $color 100%)"
                                } else {
                                    "conic-gradient(white 0%, white 50%, $color 50.01%, $color $cutoff%, white ${cutoff + 0.01}%, white 100%)"
                                }
                        (document.getElementById("alchemy-element-$n") as HTMLElement).style.backgroundImage = gradient
                    }
                    Stats.elementAmounts.clearChanged(element)
                }
                if (updateAll || Stats.functionalElementLowerBounds.changed(element) || Stats.functionalElementUpperBounds.changed(
                        element
                    )
                ) {
                    setVariable(
                        "$symbol-bounds-display",
                        "${Stats.functionalElementLowerBounds[element].roundTo(2)} ≤ $symbol ≤ ${
                            Stats.functionalElementUpperBounds[element].roundTo(
                                2
                            )
                        }"
                    )
                    Stats.functionalElementLowerBounds.clearChanged(element)
                    Stats.functionalElementUpperBounds.clearChanged(element)
                }
                if (updateAll || Stats.elementRates.changed(element)) {
                    setVariable(
                        "$symbol-rate-display",
                        "current $symbol / s = ${Stats.elementRates[element].roundTo(2)}"
                    )
                    Stats.elementRates.clearChanged(element)
                }
                if (updateAll || Stats.elementDeltas.changed(element)) {
                    setVariable(
                        "$symbol-max-rate-display",
                        "$prefix$symbol$suffix = ${Stats.elementDeltas[element].roundTo(2)}"
                    )
                    Stats.elementDeltas.clearChanged(element)
                }
            }

            for ((id, keybind) in Input.keybinds) {
                setVariable(
                    "keybind-$id-key",
                    keybind.key.key
                )
            }

            for ((id, clicker) in gameState.clickersById) {
                setVariable(
                    "clicker-$id-mode",
                    clicker.mode.pretty
                )
                setVariable(
                    "$id-clicker-cps-display",
                    "Click rate = ${clicker.cps.roundTo(3)} Hz"
                )
            }

            for ((i, entry) in NormalReactions.map.entries.withIndex()) {
                val (backendId, reaction) = entry
                setVariable("reaction-$i-title", reaction.name)
                setVariable("reaction-$i-description", reaction.toString())
                idSetClassPresence(
                    "reaction-option-$i",
                    "disabled",
                    !gameState.canDoReaction(reaction)
                )
                idSetDataVariable("reaction-option-$i", "backendReactionId", backendId)
            }

            for ((i, entry) in SpecialReactions.map.entries.withIndex()) {
                val (backendId, reaction) = entry
                setVariable("special-reaction-$i-title", reaction.name)
                setVariable("special-reaction-$i-description", reaction.toString())
                setVariable(
                    "special-reaction-$i-effects",
                    reaction.stringEffects(reaction.nTimesUsed + 1)
                )
                idSetClassPresence(
                    "special-reaction-option-$i",
                    "disabled",
                    !gameState.canDoReaction(reaction) && !reaction.hasBeenUsed
                )
                idSetClassPresence("special-reaction-option-$i", "active", reaction.hasBeenUsed)
                idSetDataVariable("special-reaction-option-$i", "backendReactionId", backendId)
            }

            for ((i, entry) in DualityMilestones.map.entries.withIndex()) {
                val (backendId, reaction) = entry
                setVariable("duality-reaction-$i-title", reaction.name)
                setVariable("duality-reaction-$i-description", reaction.toString())
                setVariable(
                    "duality-reaction-$i-effects",
                    reaction.stringEffects(reaction.nTimesUsed + 1)
                )
                idSetClassPresence(
                    "duality-reaction-option-$i",
                    "disabled",
                    !gameState.canDoReaction(reaction) && !reaction.hasBeenUsed
                )
                idSetClassPresence("duality-reaction-option-$i", "active", reaction.hasBeenUsed)
                idSetDataVariable("duality-reaction-option-$i", "backendReactionId", backendId)
            }

            for ((i, entry) in DeltaReactions.map.entries.withIndex()) {
                val (backendId, reaction) = entry
                setVariable("delta-reaction-$i-title", reaction.name)
                setVariable("delta-reaction-$i-description", reaction.toString())
                setVariable(
                    "delta-reaction-$i-effects",
                    reaction.stringEffects(reaction.nTimesUsed + 1)
                )
                idSetClassPresence(
                    "delta-reaction-option-$i",
                    "disabled",
                    !gameState.canDoReaction(reaction) && !reaction.hasBeenUsed
                )
                idSetClassPresence("delta-reaction-option-$i", "active", reaction.hasBeenUsed)
                idSetDataVariable("delta-reaction-option-$i", "backendReactionId", backendId)
            }

            setVariable("deltaReactionRespecToggleCheckbox", "${if (Stats.deltaReactionRespec) Symbols.xBox else Symbols.box}")
            setVariable("autosaving", Options.autosaving.toStringOnOff())
            idSetClassPresence("duality-respec-container", "hidden", !Flags.respecUnlocked.isUnlocked())
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
            val maxScrollAmount = container1.children.toList().sumOf { it.clientHeight } + rows * 16 + 6 /*10px core.margin + 3px border on both sides*/ - container1.clientHeight
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
            val maxScrollAmount = children.toList().sumOf { it.clientHeight } + rows * 16 + 6 /*10px core.margin + 3px border on both sides*/ - container2.clientHeight
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
    val container3 = document.getElementById("duality-reaction-container")
    if (container3 is HTMLElement) container3.apply {
        onwheel = { evt ->
            val rows = ceil(children.length / 3.0)
            val maxScrollAmount = children.toList().sumOf { it.clientHeight } + rows * 16 + 6 /*10px core.margin + 3px border on both sides*/ - container3.clientHeight
            reactionListScrollAmount = min(max(0.0, reactionListScrollAmount + reactionListScrollSens * evt.deltaY),
                maxScrollAmount
            )
            children.iterator().forEach { it.style.top = (-reactionListScrollAmount).px }

            Unit // not kotlin begging me for a return
        }
        for ((child, i) in children.indexed) {
            child.onclick = { _ ->
                val reaction = DualityMilestones.map[child.dataset["backendReactionId"]!!]!!
                if (!reaction.hasBeenUsed) {
                    gameState.attemptReaction(reaction)
                }

                Unit
            }
            child.onmouseenter = { _ ->
                val reaction = DualityMilestones.map[child.dataset["backendReactionId"]!!]!!
                gameState.hoveredReaction = reaction

                Unit
            }
            child.onmouseleave = { _ ->
                val reaction = DualityMilestones.map[child.dataset["backendReactionId"]!!]!!
                gameState.hoveredReaction = NullReaction

                Unit
            }
        }
    }
    val container4 = document.getElementById("delta-reaction-container")
    if (container4 is HTMLElement) container4.apply {
        onwheel = { evt ->
            val rows = ceil(children.length / 3.0)
            val maxScrollAmount = children.toList().sumOf { it.clientHeight } + rows * 16 + 6 /*10px core.margin + 3px border on both sides*/ - container4.clientHeight
            reactionListScrollAmount = min(max(0.0, reactionListScrollAmount + reactionListScrollSens * evt.deltaY),
                maxScrollAmount
            )
            children.iterator().forEach { it.style.top = (-reactionListScrollAmount).px }

            Unit // not kotlin begging me for a return
        }
        for ((child, i) in children.indexed) {
            child.onclick = { _ ->
                val reaction = DeltaReactions.map[child.dataset["backendReactionId"]!!]!!
                if (!reaction.hasBeenUsed) {
                    gameState.attemptReaction(reaction)
                }

                Unit
            }
            child.onmouseenter = { _ ->
                val reaction = DeltaReactions.map[child.dataset["backendReactionId"]!!]!!
                gameState.hoveredReaction = reaction

                Unit
            }
            child.onmouseleave = { _ ->
                val reaction = DeltaReactions.map[child.dataset["backendReactionId"]!!]!!
                gameState.hoveredReaction = NullReaction

                Unit
            }
        }
    }
    loadLocalStorage()
    var lastSave = 1.0
    GameTimer.registerTicker("Saving") {
        if (Options.autosaving && GameTimer.timeSex() - lastSave >= Options.saveInterval) {
            saveLocalStorage()
            lastSave = GameTimer.timeSex()
        }
    }
    GameTimer.registerTicker("GameState tick", gameState::tick)
    GameTimer.beginTicking()

    val tutorialWrapper = document.getElementById("modal-wrapper")!!
    tutorialWrapper.addEventListener("click", {
        DynamicHTMLManager.clearModal()
    })
    val tutorialBox = document.getElementById("modal")!!
    tutorialBox.addEventListener("click", {
        it.stopPropagation()
    })

    if (!Flags.seenTutorial.isUnlocked()) {
        DynamicHTMLManager.showTutorial(Tutorials.welcome)
    }
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
            if (i <= 7 && element.hasClass("alchemy-element")) {
                val symbol = element.dataset["element"]
                if (symbol != null && symbol != "h") {
                    val halfElem = element.getBoundingClientRect().width / 2
                    val normalizedPos = getAlchemyElementPos(symbol.last())
                    val pos = Vec2(half - halfElem, half - halfElem) + normalizedPos * radius
                    element.style.left = pos.x.px
                    element.style.top = pos.y.px
                }
            }
        }
    }
}

val screenWidth get() = window.screen.width
val screenHeight get() = window.screen.height
