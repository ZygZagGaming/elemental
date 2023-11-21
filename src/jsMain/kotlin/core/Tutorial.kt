package core

import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.img
import kotlinx.html.style
import org.w3c.dom.DocumentFragment

class Tutorial(val pages: List<TutorialPage>, val name: String, unlockedByDefault: Boolean = false) {
    init {
        if (unlockedByDefault) Stats.tutorialsSeen.add(this)
    }
}

fun Tutorial.getAsHTML(pageIndex: Int, onClose: () -> Unit = DynamicHTMLManager::clearModal, force: Boolean = false): DocumentFragment {
    if (pageIndex in pages.indices) {
        val k = pages[pageIndex].getAsHTML().apply {
            val x = document.createElement("img")
            appendChild(x)
            x.setAttribute("alt", "x button")
            x.setAttribute("src", "images/x.png")
            x.classList.add("tutorial-x")
            GameTimer.nextTick {
                x.addEventListener("click", {
                    onClose()

                    Unit
                })
            }
            if (pageIndex != 0) {
                val leftArrow = document.createElement("img")
                appendChild(leftArrow)
                leftArrow.setAttribute("alt", "left arrow")
                leftArrow.setAttribute("src", "images/left-arrow.png")
                leftArrow.classList.add("left-arrow")
                GameTimer.nextTick {
                    leftArrow.addEventListener("click", {
                        DynamicHTMLManager.showTutorial(this@getAsHTML, pageIndex - 1, onClose = onClose, force = force)

                        Unit
                    })
                }
            }
            if (pageIndex != pages.size - 1) {
                val rightArrow = document.createElement("img")
                appendChild(rightArrow)
                rightArrow.setAttribute("alt", "right arrow")
                rightArrow.setAttribute("src", "images/right-arrow.png")
                rightArrow.classList.add("right-arrow")
                GameTimer.nextTick {
                    rightArrow.addEventListener("click", {
                        DynamicHTMLManager.showTutorial(this@getAsHTML, pageIndex + 1, onClose = onClose, force = force)

                        Unit
                    })
                }
            }
        }
        return k
    }
    return document.createDocumentFragment()
}

abstract class TutorialPage() {
    abstract fun getAsHTML(): DocumentFragment
}

class ImageTitleTutorialPage(private val image: String, private val imageAlt: String, private val titleText: String, val subTitleText: String): TutorialPage() {
    override fun getAsHTML(): DocumentFragment {
        return document.createDocumentFragment().apply {
            append {
                img(alt = imageAlt, src = image, classes = "tutorial-image")
                div("tutorial-title") {
                    +titleText
                }
                div("tutorial-subtitle") {
                    +subTitleText
                }
            }
        }
    }
}

class ImageTextTutorialPage(private val image: String, private val imageAlt: String, private val headerText: String, val text: String): TutorialPage() {
    override fun getAsHTML(): DocumentFragment {
        return document.createDocumentFragment().apply {
            append {
                img(alt = imageAlt, src = image, classes = "tutorial-image")
                div("tutorial-header") {
                    +headerText
                }
                div("tutorial-text").apply {
                    innerHTML = text
                }
            }
        }
    }
}// TODO: merge core.ImageTextTutorialPage and core.ImageTitleTutorialPage

class TextTutorialPage(private val headerText: String, val text: String): TutorialPage() {
    override fun getAsHTML(): DocumentFragment {
        return document.createDocumentFragment().apply {
            append {
                div("tutorial-header") {
                    +headerText
                    style = "font-size: 6vmin;"
                }
                val div = div("tutorial-text") {
                    style = "line-height: 5vmin; font-size: 3.5vmin;"
                }
                div.innerHTML = text
            }
        }
    }
}