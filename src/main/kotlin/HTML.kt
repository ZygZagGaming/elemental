import kotlinx.browser.document
import org.w3c.dom.*
import kotlin.math.roundToInt

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

    fun getPageElement(page: Page): HTMLElement? {
        return pages.toList().firstOrNull { it.dataset["pageId"] == Pages.id(page) }
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
            if (dyn.classList.contains("draggable")) {
                var dx: Int
                var dy: Int
                var x: Int
                var y: Int
                dyn.onmousedown = {
                    it.preventDefault()
                    x = it.clientX
                    y = it.clientY
                    dyn.dataset["dragging"] = "true"

                    document.onmousemove = { it2 ->
                        it2.preventDefault()
                        dx = x - it2.clientX
                        dy = y - it2.clientY
                        x = it2.clientX
                        y = it2.clientY

                        dyn.style.top = (dyn.offsetTop - dy).px
                        dyn.style.left = (dyn.offsetLeft - dx).px

                        Unit
                    }

                    document.onmouseup = {
                        document.onmousemove = null
                        document.onmouseup = null
                        dyn.dataset["dragging"] = "false"

                        Unit
                    }

                    Unit
                }
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

val Int.px get() = "${this}px"
val Int.pxToVw get() = "${this.toDouble() * 100 / viewportWidth}vw"
val Int.pxToVh get() = "${this.toDouble() * 100 / viewportHeight}vh"
val Double.px get() = "${roundToInt()}px"
val Double.pxToVw get() = "${this * 100 / viewportWidth}vw"
val Double.pxToVh get() = "${this * 100 / viewportHeight}vh"
fun HTMLCollection.toList(): List<HTMLElement> {
    val list = mutableListOf<HTMLElement>()
    for (index in 0..length) {
        val elem = get(index)
        if (elem is HTMLElement) list.add(elem)
    }
    return list
}

operator fun HTMLCollection.iterator() = toList().iterator()
val HTMLCollection.indices get() = toList().indices
val HTMLCollection.indexed get() = toList().zip(indices) { a, b -> Indexed(a, b) }
val viewportWidth get() = (document.documentElement as HTMLElement).screenWidth
val viewportHeight get() = (document.documentElement as HTMLElement).screenHeight
val DOMRect.xMiddle get() = left + 0.5 * width
val DOMRect.yMiddle get() = top + 0.5 * height
var HTMLElement.x
    get() = screenX / screenWidth
    set(value) {
        screenX = value * screenWidth
    }
var HTMLElement.y
    get() = screenY / screenHeight
    set(value) {
        screenY = value * screenHeight
    }
var HTMLElement.screenX
    get() = getBoundingClientRect().left
    set(value) {
        style.left = value.pxToVw
    }
var HTMLElement.screenMiddleX
    get() = getBoundingClientRect().xMiddle
    set(value) {
        style.left = (value - clientWidth / 2).pxToVw
    }
var HTMLElement.screenY
    get() = getBoundingClientRect().top
    set(value) {
        style.top = value.pxToVh
    }
var HTMLElement.screenMiddleY
    get() = getBoundingClientRect().yMiddle
    set(value) {
        style.top = (value - clientHeight / 2).pxToVh
    }
var HTMLElement.screenWidth
    get() = getBoundingClientRect().width
    set(value) {
        style.width = value.pxToVw
    }
var HTMLElement.screenHeight
    get() = getBoundingClientRect().height
    set(value) {
        style.height = value.pxToVh
    }