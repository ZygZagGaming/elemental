package core

import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.div
import libraries.*
import org.w3c.dom.*
import kotlin.math.roundToInt

@OptIn(ExperimentalJsExport::class)
@JsExport
fun getDynamicVariable(id: String) = DynamicHTMLManager.variables[id]

object DynamicHTMLManager {
    val variables = mutableMapOf<String, String>()
    var shownPage = Pages.elementsPage
    set (value) {
        field = value
        pages.toList().forEach {
            if (it.dataset["pageId"] == Pages.id(value)) it.classList.remove("hidden")
            else it.classList.add("hidden")
        }
        pageButtons.toList().forEach {
            if (it.dataset["pageId"] == Pages.id(value)) it.classList.add("active")
            else it.classList.remove("active")
        }
        gameState.clickersByPage.forEach { (page, clickers) ->
            if (page == value) {
                val dockContainer = getPageElement(page)!!.children.toList().first { it.classList.contains("clicker-dock-container") }
                if (clickers.isEmpty()) dockContainer.style.display = "none"
                else dockContainer.style.display = "flex"
                for (clicker in clickers) {
                    clicker.dock.style.display = "block"
                    clicker.canvasParent.style.display = "block"
                    clicker.htmlElement.style.display = "block"
                }
            } else for (clicker in clickers) {
                clicker.dock.style.display = "none"
                clicker.canvasParent.style.display = "none"
                clicker.htmlElement.style.display = "none"
            }
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
                dyn.innerHTML = data
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

                        dyn.style.top = (dyn.offsetTop - dy).pxToVw
                        dyn.style.left = (dyn.offsetLeft - dx).pxToVw

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
                    shownPage = page

                    Unit
                }
            }
        }
    }

    var currentTutorial: Tutorial? = null
    var currentTutorialPage: Int? = null
    fun showTutorial(tutorial: Tutorial, page: Int = 0, onClose: () -> Unit = DynamicHTMLManager::clearModal, force: Boolean = false) {
        if (force || tutorial !in Stats.tutorialsSeen) {
            val modal = document.getElementById("modal")!!
            modal.parentElement!!.classList.remove("hidden")
            js("modal.replaceChildren()")
            modal.appendChild(tutorial.getAsHTML(page, onClose = onClose, force = force))
            currentTutorial = tutorial
            currentTutorialPage = page
        }
    }

    fun showTutorialListModal() {
        val modal = document.getElementById("modal")!!
        modal.parentElement!!.classList.remove("hidden")
        js("modal.replaceChildren()")
        val fragment = document.createDocumentFragment()

        //generate tutorial list
        val tutorials = Stats.tutorialsSeen.toMutableList()
        val list = document.createElement("div")
        list.classList.add("tutorial-list")
        fragment.appendChild(list)
        for (tutorial in tutorials) {
            val row = document.createElement("div")
            row.classList.add("tutorial-list-row")
            row.classList.add("no-autoclick")

            val text = document.createElement("div")
            text.classList.add("tutorial-list-row-text")
            text.classList.add("no-autoclick")
            text.textContent = tutorial.name
            row.appendChild(text)
            row.addEventListener("click", {
                showTutorial(tutorial, onClose = DynamicHTMLManager::showTutorialListModal, force = true)
            })
            list.appendChild(row)
        }
        val x = document.createElement("img")
        fragment.appendChild(x)
        x.setAttribute("alt", "x button")
        x.setAttribute("src", "images/x.png")
        x.classList.add("tutorial-x")
        GameTimer.nextTick {
            x.addEventListener("click", {
                clearModal()

                Unit
            })
        }

        modal.appendChild(fragment)
    }

    fun clearModal() {
        if (currentTutorial != null) Stats.tutorialsSeen.add(currentTutorial!!)
        val modal = document.getElementById("modal")!!
        modal.parentElement!!.classList.add("hidden")
        js("modal.replaceChildren()")
    }

    fun setupHTML() {
        doCircleShit()
        run { //navbar
            val navbar = document.getElementById("navbar")!!
            var n = 0
            for (page in Pages.values) if (page != Pages.optionsPage) {
                n++
                val text = (document.createElement("div") as HTMLElement).apply {
                    classList.add("navbar-text", "page-text")
                }
                val element = (document.createElement("div") as HTMLElement).apply {
                    id = "navbar-element-$n"
                    classList.add("navbar-element", "dynamic", "page-button")
                    if (page == Pages.elementsPage) classList.add("active")
                    dataset["pageId"] = Pages.id(page) ?: ""
                    appendChild(text)
                }
                navbar.appendChild(element)
            }
        }
        setupReactionContainerContents(NormalReactions.values.size)
        setupReactionContainerContents(SpecialReactions.values.size, "special-")
        setupReactionContainerContents(DualityMilestones.values.size, "duality-", "yellow-bkg-style")
        setupReactionContainerContents(DeltaReactions.values.size, "delta-")
        document.getElementById("duality-button")?.let {
            it.addEventListener("click", {
                duality()

                Unit
            })
        }
        document.getElementById("big-duality-button")?.let {
            it.addEventListener("click", {
                duality()

                Unit
            })
        }
        document.getElementById("help-button")?.let {
            it.addEventListener("click", {
                showTutorialListModal()
            })
        }
    }

    fun setupReactionContainerContents(amount: Int, prefix: String = "", extraClasses: String = "grey-bkg-style") {
        val reactionContainer = document.getElementById("${prefix}reaction-container")!!
        for (n in 0 until amount) {
            reactionContainer.append {
                div("reaction-option no-highlight $extraClasses") {
                    id = "${prefix}reaction-option-$n"
                    attributes["data-reaction-id"] = "$n"
                    div("reaction-option-title dynamic") {
                        attributes["data-dynamic-id"] = "${prefix}reaction-$n-title"
                    }
                    div("reaction-option-description-container") {
                        div("reaction-option-description dynamic") {
                            attributes["data-dynamic-id"] = "${prefix}reaction-$n-description"
                        }
                        if (prefix != "") {
                            div("reaction-option-description reaction-option-effects dynamic") {
                                attributes["data-dynamic-id"] = "${prefix}reaction-$n-effects"
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateKeybindDisplays(oldValue: Keybind?, newValue: Keybind?) {
        for (dynamic in dynamix) {
            val keybindId = dynamic.dataset["keybindId"]
            if (keybindId != null) {
                if (keybindId == oldValue?.id) dynamic.classList.remove("active")
                if (keybindId == newValue?.id) dynamic.classList.add("active")
            }
        }
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun viewTutorial(s: String) {
    val tutorial = Tutorials.map[s]
    if (tutorial != null) DynamicHTMLManager.showTutorial(tutorial)
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
val Document.contextMenu get() = document.getElementById("context-menu") as HTMLElement