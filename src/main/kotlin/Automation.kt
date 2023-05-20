import kotlinx.browser.document
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

class Autoclicker(id: Int, page: Page, var cps: Double = 1.0): Clicker(id, page) {
    override fun tick(dt: Double) {
        if (!docked) clickPercent += dt * cps
        super.tick(dt)
    }
}

class Keyclicker(id: Int, page: Page, var key: Key): Clicker(id, page) {
    val heldCps = 6.0
    lateinit var text: HTMLElement
    override fun tick(dt: Double) {
        if (!docked) {
            if (Input.keyPressedThisTick[key]) clickPercent = 1.0
            else if (Input.keyDownMap[key]) clickPercent += heldCps * dt
        }
        super.tick(dt)
    }

    override fun init() {
        super.init()
        text = document.createElement("div") as HTMLElement
        text.id = "clicker-$id-text"
        text.textContent = key.key
        text.classList.add("clicker-text")
        canvasParent.appendChild(text)
    }

    override fun deInit() {
        super.deInit()
        text.remove()
    }
}

open class Clicker(val id: Int, val page: Page) {
    var clickPercent = 0.0
    lateinit var htmlElement: HTMLElement
    lateinit var canvas: HTMLCanvasElement
    lateinit var canvasParent: HTMLElement
    lateinit var dock: HTMLElement
    lateinit var dockCanvas: HTMLCanvasElement
    var timeSinceLastClick = 0.0
    var wasDragging = false
    val dragging get() = htmlElement.dataset["dragging"] == "true"
    var docked get() = htmlElement.dataset["docked"] == "true"
    set(value) {
        htmlElement.dataset["docked"] = value.toString()
    }

    open fun deInit() {
        htmlElement.remove()
        canvas.remove()
        canvasParent.remove()
        dock.remove()
        dockCanvas.remove()
    }

    open fun init() {
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
            classList.add("no-autoclick")
        }
        canvasParent.apply {
            style.position = "absolute"
            classList.add("no-autoclick")
        }
        htmlElement.apply {
            classList.apply {
                add("clicker")
                add("draggable")
                add("dynamic")
                add("no-autoclick")
            }
            id = "autoclicker-${this@Clicker.id}"
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
            }
            onclick = {
                if (!dragging) moveToDock()

                Unit
            }
            dockCanvas = document.createElement("canvas") as HTMLCanvasElement
            appendChild(dockCanvas)
            dockCanvas.apply {
                width = vw(3.0).roundToInt()
                height = vw(3.0).roundToInt()
                style.position = "absolute"
                classList.add("no-autoclick")
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
        if (!docked) {
            timeSinceLastClick += dt
            while (clickPercent > 1) {
                clickPercent--
                click()
                timeSinceLastClick = 0.0
            }
        }
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
            && (htmlElement.screenMiddleX - dock.screenMiddleX).squared() + (htmlElement.screenMiddleY - dock.screenMiddleY).squared() <= Options.autoclickerDockSnapDistance.squared())
            moveToDock()
        wasDragging = dragging
    }

    fun moveToDock(force: Boolean = false) {
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