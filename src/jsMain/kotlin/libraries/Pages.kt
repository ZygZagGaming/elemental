package libraries

import core.Page

object Pages: Library<Page>() {
    val elementsPage = register("elements", Page("Elements"))
    val optionsPage = register("options", Page("Options"))
    val dualityPage = register("duality", Page("Duality"))
}