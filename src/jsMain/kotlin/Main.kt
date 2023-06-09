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
const val gameVersion = "v1.2.1"

@OptIn(ExperimentalJsExport::class)
@JsExport
fun resetSave() {
    localStorage.clear()
    window.location.reload()
}

val notation = RateOfChangeNotation.MAXPERSEC
val htmlUpdateInterval = 5.0
var lastHtmlUpdate = 0.0

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
        val updateAll = GameTimer.timeSex() - lastHtmlUpdate > htmlUpdateInterval
        if (updateAll) lastHtmlUpdate = GameTimer.timeSex()
        DynamicHTMLManager.apply {
            for ((name, element) in Elements.map) {
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
                    Stats.elementAmounts.clearChanged(element)
                }
                if (updateAll || Stats.functionalElementLowerBounds.changed(element) || Stats.functionalElementUpperBounds.changed(element)) {
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
                if (updateAll || Stats.elementDeltasUnspent.changed(element)) {
                    setVariable(
                        "delta-$symbol-amount",
                        "${Stats.elementDeltasUnspent[element].roundTo(2)}"
                    )
                    Stats.elementDeltasUnspent.clearChanged(element)
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

    val tutorialWrapper = document.getElementById("tutorial-box-wrapper")!!
    tutorialWrapper.addEventListener("click", {
        DynamicHTMLManager.clearTutorial()
        if (DynamicHTMLManager.currentTutorial == Tutorials.welcome) Stats.flags.add("seenTutorial")
    })
    val tutorialBox = document.getElementById("tutorial-box")!!
    tutorialBox.addEventListener("click", {
        it.stopPropagation()
    })

    if ("seenTutorial" !in Stats.flags) {
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

open class MovableClicker(val id: Int, val page: Page) {
    val htmlElement: HTMLElement
    val canvas: HTMLCanvasElement
    val dock: HTMLElement
    var clickPercent = 0.0
    var sinceLastClick = 0.0
    init {
        val parent = DynamicHTMLManager.getPageElement(page)!!
        htmlElement = document.createElement("div") as HTMLElement
        canvas = document.createElement("canvas") as HTMLCanvasElement
        parent.appendChild(canvas)
        parent.appendChild(htmlElement)
        canvas.apply {
            width = vw(3.0).roundToInt()
            height = vw(3.0).roundToInt()
            style.position = "absolute"
            classList.add("no-autoclick")
        }
        htmlElement.apply {
            classList.apply {
                add("autoclicker")
                add("draggable")
                add("dynamic")
                add("no-autoclick")
            }
            id = "autoclicker-${this@MovableClicker.id}"
            style.apply {
                position = "absolute"
                top = "50vh"
                left = "50vw"
            }
        }

        dock = document.createElement("div") as HTMLElement
        parent.children.toList().first { it.classList.contains("autoclicker-dock-container") }.appendChild(dock)
        dock.apply {
            id = "autoclicker-$id-dock"
            classList.apply {
                add("autoclicker-dock")
            }
            style.apply {
                position = "absolute"
                left = "0"
                bottom = "0"
            }
            onclick = {
                if (htmlElement.dataset["dragging"] != "true") {
                    htmlElement.screenMiddleX = dock.screenMiddleX
                    htmlElement.screenY = dock.screenY
                }

                Unit
            }
        }
    }

    fun click() {
        //console.log(document.elementsFromPoint(htmlElement.getBoundingClientRect().xMiddle, htmlElement.getBoundingClientRect().yMiddle))
        val element = document.elementsFromPoint(htmlElement.getBoundingClientRect().xMiddle, htmlElement.getBoundingClientRect().top).firstOrNull { !it.classList.contains("no-autoclick") }
        if (element is HTMLElement) {
            element.click()
        }
    }

    open fun tick(dt: Double) {
        sinceLastClick += dt
        while (clickPercent > 1) {
            clickPercent--
            click()
            sinceLastClick = 0.0
        }
        canvas.apply {
            val pixels = 10000 * dt
            val dx = htmlElement.screenX - screenX
            val dy = htmlElement.screenY - screenY
            val totalDistance = sqrt(dx * dx + dy * dy + 0.0)
            if (totalDistance > 1e-6) {
                if (totalDistance < pixels) {
                    screenX = htmlElement.screenX
                    screenY = htmlElement.screenY
                } else {
                    screenX += (dx * pixels / totalDistance).roundToInt()
                    screenY += (dy * pixels / totalDistance).roundToInt()
                }
                style.width = htmlElement.clientWidth.px
                style.height = htmlElement.clientHeight.px
            }
        }
    }
}

class KeyClicker(id: Int, page: Page, var holdCps: Double, var key: Key): MovableClicker(id, page) {
    override fun tick(dt: Double) {
        if (Input.keyPressedThisTick[key]) clickPercent += 1.0
        else if (Input.keyDownMap[key]) clickPercent += holdCps * dt
        else clickPercent = 0.0
        super.tick(dt)
    }
}

class AutoClicker(id: Int, page: Page, var cps: Double = 2.0): MovableClicker(id, page) {
    override fun tick(dt: Double) {
        clickPercent += dt * cps
        super.tick(dt)
    }
}

val screenWidth get() = window.screen.width
val screenHeight get() = window.screen.height
