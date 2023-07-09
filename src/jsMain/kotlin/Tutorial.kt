import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.img
import kotlinx.html.js.onClickFunction
import org.w3c.dom.DocumentFragment

typealias Tutorial = List<TutorialPage>

fun Tutorial.getAsHTML(pageIndex: Int): DocumentFragment {
    if (pageIndex in indices) {
        return this[pageIndex].getAsHTML().apply {
            append {
                img(alt = "x button", src = "images/x.png", classes="tutorial-x") {
                    onClickFunction = { _ ->
                        DynamicHTMLManager.clearTutorial()
                        if (DynamicHTMLManager.currentTutorial == Tutorials.welcome) Stats.flags.add("seenTutorial")
                    }
                }
                if (pageIndex != 0) img(alt = "left arrow", src = "images/left-arrow.png", classes = "left-arrow") {
                    onClickFunction = { _ ->
                        DynamicHTMLManager.showTutorial(this@getAsHTML, pageIndex - 1)
                    }
                }
                if (pageIndex != size - 1) img(alt = "right arrow", src = "images/right-arrow.png", classes = "right-arrow") {
                    onClickFunction = { _ ->
                        DynamicHTMLManager.showTutorial(this@getAsHTML, pageIndex + 1)
                    }
                }
            }

        }
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
                div("tutorial-text") {
                    +text
                }
            }
        }
    }
}// TODO: merge ImageTextTutorialPage and ImageTitleTutorialPage