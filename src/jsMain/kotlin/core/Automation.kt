package core

import kotlinx.browser.document
import org.w3c.dom.*
import kotlin.math.*
import kotlin.random.Random

class Clicker(val id: Int, val page: Page, var mode: ClickerMode, val autoCps: Double, val heldCps: Double) {
    val cps get() = if (mode == ClickerMode.MANUAL) heldCps else gameState.autoclickspeedMultiplierStack.parallel(autoCpsModifiers).appliedTo(autoCps)
    var clickPercent = 0.0
    lateinit var htmlElement: HTMLElement
    lateinit var canvas: HTMLCanvasElement
    lateinit var canvasParent: HTMLElement
    lateinit var dock: HTMLElement
    lateinit var dockCanvas: HTMLCanvasElement
    val randomOffset = Random.nextDouble(0.0, 1.0)
    var timeSinceLastClick = 0.0
    var wasDragging = false
    val autoCpsModifiers = basicMultiplierStack
    val dragging get() = htmlElement.dataset["dragging"] == "true"
    var docked get() = htmlElement.dataset["docked"] == "true"
    set(value) {
        htmlElement.dataset["docked"] = value.toString()
    }
    lateinit var text: HTMLElement
    val modesUnlocked = defaultModes.toMutableSet()

    var lifetime = 0.0

    init {
        if (id > 5)
            autoCpsModifiers.setMultiplier("nerf", 0.25) // TODO: clean this up
    }

    fun deInit() {
        htmlElement.remove()
        canvas.remove()
        canvasParent.remove()
        dock.remove()
        dockCanvas.remove()
        text.remove()
        Input.removeKeybind("keyclicker-$id")
    }

    fun init() {
        val parent = DynamicHTMLManager.getPageElement(page)!!
        htmlElement = document.createElement("div") as HTMLElement
        canvasParent = document.createElement("div") as HTMLElement
        canvasParent.classList.add("canvas-parent")
        canvas = document.createElement("canvas") as HTMLCanvasElement
        parent.appendChild(canvasParent)
        canvasParent.appendChild(canvas)
        parent.appendChild(htmlElement)
        canvas.apply {
            width = vw(3.0).roundToInt()
            height = vw(3.0).roundToInt()
            style.position = "relative"
            dataset["autoclickerId"] = this@Clicker.id.toString()
            classList.add("no-autoclick")
        }
        canvasParent.apply {
            style.position = "absolute"
            classList.add("no-autoclick")
            dataset["autoclickerId"] = this@Clicker.id.toString()
        }
        htmlElement.apply {
            classList.apply {
                add("clicker")
                add("draggable")
                add("dynamic")
                add("no-autoclick")
            }
            id = "autoclicker-${this@Clicker.id}"
            dataset["autoclickerId"] = this@Clicker.id.toString()
            style.apply {
                position = "absolute"
                top = "50vh"
                left = "50vw"
            }
        }

        dock = document.createElement("div") as HTMLElement
        parent.children.toList().first { it.classList.contains("clicker-dock-container") }.appendChild(dock)
        dock.apply {
            id = "clicker-$id-dock"
            classList.apply {
                add("clicker-dock")
                add("no-autoclick")
            }
            onclick = {
                if (!dragging) moveToDock(source = "dock clicked")

                Unit
            }
            dockCanvas = document.createElement("canvas") as HTMLCanvasElement
            appendChild(dockCanvas)
            dockCanvas.apply {
                width = vw(3.0).roundToInt()
                height = vw(3.0).roundToInt()
                style.position = "absolute"
                classList.add("no-autoclick")
                dataset["autoclickerId"] = this@Clicker.id.toString()
            }
            dataset["autoclickerId"] = this@Clicker.id.toString()
        }
        text = document.createElement("div") as HTMLElement
        text.id = "clicker-$id-text"
        text.textContent = id.toString()
        text.classList.add("clicker-text")
        text.dataset["autoclickerId"] = this@Clicker.id.toString()
        canvasParent.appendChild(text)

        if (mode == ClickerMode.MANUAL) canvasParent.classList.add("keyclicker")
        Input.addKeybind(KeyclickerKeybind(this))
    }

    fun setMode(newMode: ClickerMode) {
        canvasParent.classList.apply {
            setPresence("keyclicker", newMode == ClickerMode.MANUAL)
            setPresence("off", newMode == ClickerMode.DISABLED)
        }
        mode = newMode
    }

    var hoveredElement: HTMLElement? = null
    fun click() {
        hoveredElement?.click()
    }

    val recheckHoveringInterval = 1.0
    fun tick(dt: Double) {
        lifetime += dt
        timeSinceLastClick += dt
        if (mode !in modesUnlocked) setMode(ClickerMode.DISABLED)
        if (dragging || (DynamicHTMLManager.shownPage == page && GameTimer.every(
                recheckHoveringInterval,
                dt
            ))
        ) hoveredElement = document.elementsFromPoint(
            htmlElement.getBoundingClientRect().xMiddle,
            htmlElement.getBoundingClientRect().top
        ).firstOrNull { !it.classList.contains("no-autoclick") } as HTMLElement?
        if (!docked && mode == ClickerMode.AUTO) clickPercent += dt * cps
        if (!docked) {
            while (clickPercent > 1) {
                clickPercent--
                click()
                timeSinceLastClick = 0.0
            }
        }
        if (DynamicHTMLManager.shownPage == page) {
            canvasParent.apply {
                val pixels = 10000 * dt
                val dx = htmlElement.screenX - screenX
                val dy = htmlElement.screenY - screenY
                val totalDistance = sqrt(dx * dx + dy * dy + 0.0)
                if (totalDistance > 1e-6) {
                    if (totalDistance < pixels) {
                        screenX = htmlElement.screenX
                        screenY = htmlElement.screenY
                    } else {
                        screenX += dx * pixels / totalDistance
                        screenY += dy * pixels / totalDistance
                    }
                }
                if (abs(screenWidth - htmlElement.screenWidth) > 0.5) screenWidth = htmlElement.screenWidth
                if (abs(screenHeight - htmlElement.screenHeight) > 0.5) screenHeight = htmlElement.screenHeight
            }
            canvas.apply {
                if (abs(width - dock.screenWidth) > 0.5) width = dock.screenWidth.roundToInt()
                if (abs(height - dock.screenHeight) > 0.5) height = dock.screenHeight.roundToInt()
                if (abs(screenWidth - dock.screenWidth) > 0.5) screenWidth = dock.screenWidth
                if (abs(screenHeight - dock.screenHeight) > 0.5) screenHeight = dock.screenHeight
            }
            dockCanvas.apply {
                if (abs(width - dock.screenWidth) > 0.5) width = dock.screenWidth.roundToInt()
                if (abs(height - dock.screenHeight) > 0.5) height = dock.screenHeight.roundToInt()
                if (abs(screenWidth - dock.screenWidth) > 0.5) screenWidth = dock.screenWidth
                if (abs(screenHeight - dock.screenHeight) > 0.5) screenHeight = dock.screenHeight
            }
            if (!dragging && wasDragging) docked = false
            if (!dragging && !docked
                && (htmlElement.screenMiddleX - dock.screenMiddleX).squared() + (htmlElement.screenMiddleY - dock.screenMiddleY).squared() <= Options.autoclickerDockSnapDistance.squared()
            ) moveToDock(source = "snap to dock position")
            wasDragging = dragging
        }
    }

    fun moveToDock(force: Boolean = false, source: String = "") {
        htmlElement.apply {
            screenMiddleX = dock.screenMiddleX
            screenMiddleY = dock.screenMiddleY
        }
        docked = true
        if (force) canvasParent.apply {
            screenMiddleX = dock.screenMiddleX
            screenMiddleY = dock.screenMiddleY
        }
    }
}

enum class ClickerMode(val pretty: String) {
    AUTO("Auto"),
    MANUAL("Manual"),
    DISABLED("Disabled")
}

typealias ModesUnlocked = MutableSet<ClickerMode>
val defaultModes: ModesUnlocked get() = mutableSetOf(ClickerMode.DISABLED)

fun DOMTokenList.setPresence(str: String, bool: Boolean) {
    if (bool) add(str)
    else remove(str)
}