import kotlinx.browser.document
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class Autoclicker(val id: Int, val page: Page, var cps: Double = 2.0) {
    var clickPercent = 0.0
    val htmlElement: HTMLElement
    val canvas: HTMLCanvasElement
    val dock: HTMLElement
    val dockCanvas: HTMLCanvasElement
    var timeSinceLastClick = 0.0
    var wasDragging = false
    val dragging get() = htmlElement.dataset["dragging"] == "true"
    var docked get() = htmlElement.dataset["docked"] == "true"
    set(value) {
        htmlElement.dataset["docked"] = value.toString()
    }
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
            id = "autoclicker-${this@Autoclicker.id}"
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

    fun tick(dt: Double) {
        if (!docked) {
            clickPercent += dt * cps
            timeSinceLastClick += dt
            while (clickPercent > 1) {
                clickPercent--
                click()
                timeSinceLastClick = 0.0
            }
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
            }
            if (abs(width - htmlElement.screenWidth) > 0.5) width = htmlElement.screenWidth.roundToInt()
            if (abs(height - htmlElement.screenHeight) > 0.5) height = htmlElement.screenHeight.roundToInt()
            if (abs(screenWidth - htmlElement.screenWidth) > 0.5) screenWidth = htmlElement.screenWidth
            if (abs(screenHeight - htmlElement.screenHeight) > 0.5) screenHeight = htmlElement.screenHeight
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

    fun moveToDock() {
        htmlElement.apply {
            screenMiddleX = dock.screenMiddleX
            screenMiddleY = dock.screenMiddleY
            docked = true
        }
    }
}