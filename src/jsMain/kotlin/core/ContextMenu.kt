package core

import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.div
import libraries.Resources
import org.w3c.dom.DocumentFragment
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.get

object ContextMenu {
    fun init() {
        document.addEventListener("keydown", { keyEvent ->
            if (changingKey != null && keyEvent is KeyboardEvent) {
                changingKey!!.key = Key(keyEvent)
                changingKey = null
            }
        })
    }

    fun applyEventListeners() {
        document.addEventListener("contextmenu", openContextMenu)
        document.addEventListener("mousedown", removeContextMenu)
    }

    val openContextMenu: (Event) -> dynamic = { e ->
        val ctx = document.contextMenu
        ctx.style.display = "flex"
        isContextMenuOpen = true
        e.preventDefault()
        if (e is MouseEvent) {
            ctx.style.left = e.clientX.px
            ctx.style.top = e.clientY.px
        }
        js("ctx.replaceChildren()")
        ctx.appendChild(getContextMenuContents(e.target!!))

        //make sure its on the screen
        if (ctx.screenY + ctx.screenHeight >= document.body!!.screenHeight) {
            ctx.screenY = document.body!!.screenHeight - ctx.screenHeight
        }
        if (ctx.screenX + ctx.screenWidth >= document.body!!.screenWidth) {
            ctx.screenX = document.body!!.screenWidth - ctx.screenWidth
        }

        Unit
    }

    var isContextMenuOpen = false

    val removeContextMenu: (Event) -> dynamic = { e ->
        val target = e.target
        val ctx = document.contextMenu
        if (isContextMenuOpen && e is MouseEvent && e.button == 0.toShort() && target is HTMLElement && !ctx.contains(target)) {
            ctx.style.display = "none"
            js("ctx.replaceChildren()")
            isContextMenuOpen = false
        }

        Unit
    }

    var changingKey: Keybind? = null
    set(value) {
        val oldValue = field
        field = value
        DynamicHTMLManager.updateKeybindDisplays(oldValue, value)
    }

    private fun getContextMenuContents(target: EventTarget): DocumentFragment {
        val fragment = document.createDocumentFragment().apply {
            append {
                if (target is HTMLElement) {
                    val actualElementCircle = target.closest(".alchemy-element") as HTMLElement?
                    val autoclickerId = target.dataset["autoclickerId"]?.toIntOrNull()
                    if (actualElementCircle != null) {
                        val symbol = actualElementCircle.dataset["element"]
                        val element = Resources.symbolMap[symbol]
                        if (element != null) div {
                            +"$symbol is called ${element.name}"
                        }
                        div("dynamic") {
                            attributes["data-dynamic-id"] = "$symbol-amount-display"
                        }
                        div("dynamic") {
                            attributes["data-dynamic-id"] = "$symbol-bounds-display"
                        }
                        div("dynamic") {
                            attributes["data-dynamic-id"] = "$symbol-rate-display"
                        }
                        div("dynamic") {
                            attributes["data-dynamic-id"] = "$symbol-max-rate-display"
                        }
                        /*if (element == libraries.Elements.catalyst) {
                            div("horizontal-line")
                            div {
                                +"${libraries.Elements.heat.symbol} = ${core.Stats.elementAmounts[libraries.Elements.heat].core.roundTo(2)}"
                            }
                            div {
                                +"${core.Stats.functionalElementUpperBounds[libraries.Elements.heat]} ≤ ${libraries.Elements.heat.symbol} ≤ ${core.Stats.functionalElementLowerBounds[libraries.Elements.heat]}"
                            }
                            div {
                                +"current ${libraries.Elements.heat.symbol} / s = ${core.Stats.elementRates[libraries.Elements.heat]}"
                            }
                            div {
                                +"$prefix${libraries.Elements.heat.symbol}$suffix = ${core.Stats.elementDeltas[libraries.Elements.heat]}"
                            }
                        }*/
                    } else if (autoclickerId != null) {
                        val clicker = gameState.clickersById[autoclickerId]
                        if (clicker != null) {
                            div {
                                +"Clicker $autoclickerId"
                            }
                            div {
                                div {
                                    +"Key: "
                                }
                                val keybind = Input.keybinds["keyclicker-$autoclickerId"]
                                val k = div("keyclicker-key-change dynamic no-highlight") {
                                    if (keybind != null) {
                                        attributes["data-dynamic-id"] = "keybind-${keybind.id}-key"
                                        attributes["data-keybind-id"] = keybind.id
                                    }
                                    else +"—"
                                }
                                if (keybind != null) k.addEventListener("click", { _ ->
                                    changingKey = keybind
                                })
                            }
                            div {
                                div {
                                    +"Mode: "
                                }
                                div("keyclicker-mode-change dynamic no-highlight") {
                                    attributes["data-dynamic-id"] = "clicker-${clicker.id}-mode"
                                }.addEventListener("click", { _ ->
                                    val root = clicker.mode.ordinal
                                    var amt = 0
                                    while (amt <= ClickerMode.values().size) {
                                        amt++
                                        val mode = ClickerMode.values()[(root + amt) % ClickerMode.values().size]
                                        if (mode in clicker.modesUnlocked) {
                                            clicker.setMode(mode)
                                            break
                                        }
                                    }
                                })
                            }
                            if (ClickerMode.AUTO == clicker.mode) div("dynamic") {
                                attributes["data-dynamic-id"] = "$autoclickerId-clicker-cps-display"
                            }
                        } else div {
                            +"N/A"
                        }
                    } else div {
                        +"N/A"
                    }
                }
                div("horizontal-line")
            }
        }
        return fragment
    }
}

enum class RateOfChangeNotation(val prefix: String = "", val suffix: String = "") {
    DELTA("\u0394", ""),
    APOSTROPHE("", "'"),
    MAXPERSEC("max ", " / s")
}