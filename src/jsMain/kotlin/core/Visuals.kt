package core

import kotlinx.browser.document
import libraries.Pages
import libraries.Resources
import libraries.Symbols
import org.w3c.dom.CanvasGradient
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import kotlin.math.*

fun visuals(gameState: GameState) {
    when (DynamicHTMLManager.shownPage) {
        Pages.elementsPage -> elementsPageVisuals(gameState)
        Pages.dualityPage -> dualityPageVisuals(gameState)
    }
}

fun alchemyContainerVisuals(gameState: GameState, alchemyContainer: HTMLElement) {
    val elements = alchemyContainer.children
    val half = alchemyContainer.getBoundingClientRect().width / 2
    val radius = half * 0.8
    val portion = 2 * PI / 7
    val visuals = elements.toList().last() as HTMLCanvasElement
    val middle = Vec2(half, half)
    val size = vw(30.0)
    visuals.width = size.roundToInt()
    visuals.height = size.roundToInt()
    val elementRadius = vw(1.625)
    (visuals.getContext("2d") as CanvasRenderingContext2D).apply {
        strokeStyle = "rgba(0, 0, 0, 0)"
        beginPath()
        moveTo(0.0, 0.0)
        lineTo(size, 0.0)
        lineTo(size, size)
        lineTo(0.0, size)
        fill()

        strokeStyle = "#ffffff"
        val polygonRadius = (half + radius) * 0.5
        lineWidth = 3.0
        beginPath()
        moveTo(half, half + polygonRadius)
        for (i in 1..7) {
            lineTo(half + polygonRadius * sin(portion * i), half + polygonRadius * cos(portion * i))
        }
        stroke()

        if (DynamicHTMLManager.shownPage == Pages.elementsPage) {
            val seam = PI / 2
            val graphicalHeatAmount = graphicalAmounts[Resources.heat]
            val heatAngle = graphicalHeatAmount * 2 * PI
            val catalystAngle = graphicalAmounts[Resources.catalyst] * 2 * PI
            val strokeWidth = vw(1.0)
            if (Stats.elementAmounts[Resources.heat] <= 1e-6) {
                lineWidth = strokeWidth
                beginPath()
                strokeStyle = "#cccccc"
                arc(half, half, elementRadius + strokeWidth / 2, 0.0, 2 * PI)
                stroke()
            } else {
                val hotStrokeWidth = strokeWidth * (1 + graphicalHeatAmount)
                lineWidth = hotStrokeWidth
                strokeStyle = createRadialGradient(
                    half,
                    half,
                    elementRadius,
                    half,
                    half,
                    elementRadius + hotStrokeWidth
                ).apply {
                    val cutoff = (1 / (1 + graphicalHeatAmount)).clamp(0.01..0.98)
                    addColorStop(0.0, "rgba(255, 0, 0, 1)")
                    addColorStop(cutoff, "rgba(255, 0, 0, 1)")
                    addColorStop(cutoff + 0.01, "rgba(255, 0, 0, 0.5)")
                    addColorStop(1.0, "rgba(255, 0, 0, 0)")
                }

                stroke {
                    arc(half, half, elementRadius + hotStrokeWidth / 2, seam, seam + heatAngle)
                }

                lineWidth = strokeWidth
                strokeStyle = "#cccccc"
                stroke {
                    arc(half, half, elementRadius + strokeWidth / 2, seam + heatAngle, seam + 2 * PI)
                }

//                val border = 13.5
//                styled(
//                    strokeStyle = createRadialGradient(
//                        middle,
//                        elementRadius,
//                        elementRadius + border,
//                        0.0 to "rgba(255, 0, 0, 1)",
//                        0.4 to "rgba(255, 0, 0, 1)",
//                        0.41 to "rgba(255, 0, 0, 0.33)",
//                        1.0 to "rgba(255, 0, 0, 0)"
//                    ), lineWidth = border) {
//                    stroke {
//                        arc(half, half, elementRadius + border / 2, seam, seam + catalystAngle)
//                    }
//                }
//                styled(
//                    strokeStyle = createRadialGradient(
//                        middle,
//                        elementRadius,
//                        elementRadius + border,
//                        0.0 to "rgba(255, 255, 255, 1)",
//                        0.4 to "rgba(255, 255, 255, 1)",
//                        0.41 to "rgba(255, 255, 255, 0.33)",
//                        1.0 to "rgba(255, 255, 255, 0)"
//                    ), lineWidth = border) {
//                    stroke {
//                        arc(half, half, elementRadius + border / 2, seam + catalystAngle, seam + 2 * PI)
//                    }
//                }

//                val distance = polygonRadius * 0.8875
//                for (element in Resources.basicElements) if (element != Resources.heat && element != Resources.catalyst) {
//                    val pos = middle + getAlchemyElementPos(element.symbol[0]) * distance
//                    val (x, y) = pos
//                    val angle = graphicalAmounts[element] * 2 * PI
//                    styled(
//                        strokeStyle = createRadialGradient(
//                            pos,
//                            elementRadius,
//                            elementRadius + border,
//                            0.0 to "rgba(255, 0, 0, 1)",
//                            0.4 to "rgba(255, 0, 0, 1)",
//                            0.41 to "rgba(255, 0, 0, 0.33)",
//                            1.0 to "rgba(255, 0, 0, 0)"
//                        ), lineWidth = border
//                    ) {
//                        stroke {
//                            arc(x, y, elementRadius + border / 2, seam, seam + angle)
//                        }
//                    }
//                    styled(
//                        strokeStyle = createRadialGradient(
//                            pos,
//                            elementRadius,
//                            elementRadius + border,
//                            0.0 to "rgba(255, 255, 255, 1)",
//                            0.4 to "rgba(255, 255, 255, 1)",
//                            0.41 to "rgba(255, 255, 255, 0)",
//                            1.0 to "rgba(255, 255, 255, 0)"
//                        ), lineWidth = border
//                    ) {
//                        stroke {
//                            arc(x, y, elementRadius + border / 2, seam + angle, seam + 2 * PI)
//                        }
//                    }
//                }
            }

            val reaction = gameState.hoveredReaction
            val canDoReaction = gameState.canDoReaction(reaction)
            for ((element, _) in reaction.inputs) {
                if (element.symbol !in listOf(
                        Symbols.catalyst.toString(),
                        Symbols.heat.toString()
                    ) && reaction.inputs[element] != 0.0
                ) {
                    lineWidth = 10.0
                    val relative = getAlchemyElementPos(element.symbol.last()) * radius
                    if (canDoReaction) {
                        gradientLineColorBar(
                            middle,
                            middle + relative,
                            255,
                            255,
                            0,
                            1.0,
                            10.0,
                            (-(gameState.timeSpent * 200 + element.hashCode() * 800) / relative.magnitude).mod(1.0),
                            0.2,
                            0.65
                        )
                    } else {
                        gradientLine(middle, middle + relative, 255, 255, 0, 0.5, 10.0)
                    }
                }
            }

            for ((element, _) in reaction.outputs) {
                if (element.symbol !in listOf(
                        Symbols.catalyst.toString(),
                        Symbols.heat.toString()
                    ) && reaction.outputs[element] != 0.0
                ) {
                    lineWidth = 10.0
                    val relative = getAlchemyElementPos(element.symbol.last()) * radius
                    if (canDoReaction) {
                        gradientLineColorBar(
                            middle,
                            middle + relative,
                            255,
                            0,
                            255,
                            1.0,
                            10.0,
                            ((gameState.timeSpent * 200 + element.hashCode() * 225) / relative.magnitude).mod(1.0),
                            0.2,
                            0.65
                        )
                    } else {
                        gradientLine(middle, middle + relative, 255, 0, 255, 0.5, 10.0)
                    }
                }
            }

            // Draw percentage bars
        }
    }
}

const val margin = 50.0
val Clicker.color get() = when (mode) {
    ClickerMode.MANUAL -> "#669"
    ClickerMode.DISABLED -> "#444"
    ClickerMode.AUTO -> "#fff"
}

fun clickerVisuals(clicker: Clicker) {
    if (clicker.docked) drawDeactivatedClickerToCanvas(clicker)
    else drawClickerToCanvas(clicker)
    drawDeactivatedClickerToCanvas(clicker, canvas = clicker.dockCanvas, color = "#606060")
}

val clearStyle = StyleHolder(fillStyle = "rgba(255, 255, 255, 0)")

fun drawClickerToCanvas(clicker: Clicker, canvas: HTMLCanvasElement = clicker.canvas, color: String = clicker.color, blurry: Boolean = false) {
    val size = clicker.htmlElement.screenWidth
    val cps = if (clicker.mode == ClickerMode.MANUAL) 6.0 else clicker.cps
    val (r, g, b) = (1..3).map { color[it].uppercase().toInt(16) * 17 }
    val opaqueClickerStyle = StyleHolder(lineWidth = 3.0, fillStyle = color, strokeStyle = color)
    val transparentClickerStyle = StyleHolder(lineWidth = 3.0, fillStyle = "rgba($r, $g, $b, 0.5)", strokeStyle = "rgba($r, $g, $b, 0.5)")

    (canvas.getContext("2d") as CanvasRenderingContext2D).apply {
        styled(clearStyle) { // clear
            clearRect(-margin, -margin, size + margin, size + margin)
        }

        var workingCps = cps
        while (workingCps > 7) workingCps /= 7
        while (workingCps <= cps) {
            val tslc = if (clicker.mode == ClickerMode.MANUAL) clicker.timeSinceLastClick else (clicker.lifetime + clicker.randomOffset).mod(1 / workingCps)
            val arrowHeightAmount = (2 * workingCps * tslc - 1).squared().clamp(0.0..1.0)//1 - 4 * workingCps * tslc * min(workingCps * tslc, 1 - workingCps * tslc)
            val arrowHeight = if (clicker.mode == ClickerMode.DISABLED) 1.0 else size * 0.2 * arrowHeightAmount
            styled(if (workingCps > 7) transparentClickerStyle else opaqueClickerStyle) {
                fill { // arrow
                    moveTo(size / 2, arrowHeight)
                    lineTo(size * 0.2, arrowHeight + size * 0.3)
                    lineTo(size / 2, arrowHeight + size * 0.1)
                    lineTo(size * 0.8, arrowHeight + size * 0.3)
                }

                val angle = (((clicker.lifetime + clicker.randomOffset) * workingCps / 7)) * PI * 2
                stroke { // swirling line
                    moveTo(size / 2, size * (1 - 0.35))
                    lineTo(
                        size / 2 + (size * 0.35 - lineWidth / 2) * cos(angle),
                        size * (1 - 0.35) + (size * 0.35 - lineWidth / 2) * sin(angle)
                    )
                }
            }

            workingCps *= 7
        }

        styled(opaqueClickerStyle) {
            stroke { // draw outer circle
                arc(size / 2, size * (1 - 0.35), size * 0.35 - lineWidth / 2, 0.0, 2 * PI)
            }

            fill { // draw inner circle
                arc(size / 2, size * (1 - 0.35), size * 0.15, 0.0, 2 * PI)
            }
        }
    }
}

fun drawDeactivatedClickerToCanvas(clicker: Clicker, canvas: HTMLCanvasElement = clicker.canvas, color: String = clicker.color) {
    val size = clicker.htmlElement.screenWidth
    (canvas.getContext("2d") as CanvasRenderingContext2D).apply {
        fillStyle = "rgba(255, 255, 255, 0)"
        clearRect(-margin, -margin, size + margin, size + margin)

        strokeStyle = color
        fillStyle = color
        lineWidth = 3.0
        beginPath()
        arc(size / 2, size * (1 - 0.35), size * 0.35 - lineWidth / 2, 0.0, 2 * PI)
        stroke()

        beginPath()
        arc(size / 2, size * (1 - 0.35), size * 0.15, 0.0, 2 * PI)
        fill()

        val arrowHeight = size * 0.2
        beginPath()
        moveTo(size / 2, arrowHeight)
        lineTo(size * 0.2, arrowHeight + size * 0.3)
        lineTo(size / 2, arrowHeight + size * 0.1)
        lineTo(size * 0.8, arrowHeight + size * 0.3)
        fill()
    }
}

fun elementsPageVisuals(gameState: GameState) {
    document.getElementById("elements-alchemy-container")!!.also { alchemyContainerVisuals(gameState, it as HTMLElement) }
    for (clicker in gameState.clickersById.values) if (clicker.page == Pages.elementsPage) clickerVisuals(clicker)
}

fun dualityPageVisuals(gameState: GameState) {
    document.getElementById("delta-alchemy-container")!!.also { alchemyContainerVisuals(gameState, it as HTMLElement) }
    for (clicker in gameState.clickersById.values) if (clicker.page == Pages.dualityPage) clickerVisuals(clicker)
}

fun CanvasRenderingContext2D.gradientLine(posA: Vec2, posB: Vec2, r: Int, g: Int, b: Int, a: Double, width: Double) {
    //val len = (posA - posB).magnitude
    val avg = (posA + posB) / 2.0
    val diff = posA - posB
    val transpose = Vec2(-diff.y, diff.x).normalized * width// / 2.0
    val p1 = avg + transpose
    val p2 = avg - transpose
    val gradient = createLinearGradient(p1.x, p1.y, p2.x, p2.y)
    gradient.addColorStop(0.0, "rgba($r, $g, $b, 0.0)")
    gradient.addColorStop(0.5, "rgba($r, $g, $b, $a)")
    gradient.addColorStop(1.0, "rgba($r, $g, $b, 0.0)")
    strokeStyle = gradient
    beginPath()
    moveTo(posA)
    lineTo(posB)
    stroke()

}

val graphicalAmounts = object: Indexable<Resource, Double> {
    override fun get(key: Resource): Double = Stats.elementAmounts[key] / Stats.functionalElementUpperBounds[key]
}
fun CanvasRenderingContext2D.gradientLineColorBar(posA: Vec2, posB: Vec2, r: Int, g: Int, b: Int, a: Double, width: Double, colorBarPosition: Double, colorBarWidth: Double, colorBarOpacity: Double) {
    val len = (posA - posB).magnitude
    val avg = (posA + posB) / 2.0
    val diff = posA - posB
    val transpose = Vec2(-diff.y, diff.x).normalized * width / 2.0
    val p1 = avg + transpose
    val p2 = avg - transpose
    createLinearGradient(p1.x, p1.y, p2.x, p2.y).apply {
        addColorStop(0.0, "rgba($r, $g, $b, 0.0)")
        addColorStop(0.5, "rgba($r, $g, $b, $a)")
        addColorStop(1.0, "rgba($r, $g, $b, 0.0)")
        strokeStyle = this
    }

    beginPath()
    moveTo(posA)
    lineTo(posB)
    stroke()

    createLinearGradient(posA.x, posA.y, posB.x, posB.y).apply {
        addColorStop(0.0, "rgba(255, 255, 255, 0.0)")
        addColorStop(max(0.0, colorBarPosition - colorBarWidth), "rgba(255, 255, 255, 0.0)")
        addColorStop(colorBarPosition, "rgba(255, 255, 255, $colorBarOpacity)")
        addColorStop(min(1.0, colorBarPosition + colorBarWidth), "rgba(255, 255, 255, 0.0)")
        addColorStop(1.0, "rgba(255, 255, 255, 0.0)")
        strokeStyle = this
    }
    beginPath()
    moveTo(posA)
    lineTo(posB)
    stroke()
}

fun CanvasRenderingContext2D.lineTo(vec2: Vec2) {
    lineTo(vec2.x, vec2.y)
}

fun CanvasRenderingContext2D.moveTo(vec2: Vec2) {
    moveTo(vec2.x, vec2.y)
}

fun CanvasRenderingContext2D.stroke(function: () -> Unit) {
    beginPath()
    function()
    stroke()
}

fun CanvasRenderingContext2D.fill(function: () -> Unit) {
    beginPath()
    function()
    fill()
}

fun Double.clamp(range: ClosedFloatingPointRange<Double>): Double {
    return if (this in range) this else if (this < range.start) range.start else range.endInclusive
}

fun CanvasRenderingContext2D.createRadialGradient(center: Vec2, innerRadius: Double, outerRadius: Double, vararg colorStops: Pair<Double, String>): CanvasGradient {
    return createRadialGradient(center.x, center.y, innerRadius, center.x, center.y, outerRadius).apply {
        for ((pos, color) in colorStops) {
            addColorStop(pos, color)
        }
    }
}

fun CanvasRenderingContext2D.styled(strokeStyle: dynamic = "#000", fillStyle: dynamic = "#fff", lineWidth: Double = 1.0, contextFunction: CanvasRenderingContext2D.() -> Unit) {
    StyleContext(this, strokeStyle, fillStyle, lineWidth)(contextFunction)
}
fun CanvasRenderingContext2D.styled(holder: StyleHolder, contextFunction: CanvasRenderingContext2D.() -> Unit) {
    StyleContext(this, holder)(contextFunction)
}

data class StyleContext(
    val parent: CanvasRenderingContext2D,
    val strokeStyle: dynamic = "#000",
    val fillStyle: dynamic = "#fff",
    val lineWidth: Double = 1.0
) {
    constructor(parent: CanvasRenderingContext2D, holder: StyleHolder): this(parent, holder.strokeStyle, holder.fillStyle, holder.lineWidth)
    operator fun invoke(contextFunction: CanvasRenderingContext2D.() -> Unit) {
        val oldStrokeStyle = parent.strokeStyle
        val oldFillStyle = parent.fillStyle
        val oldLineWidth = parent.lineWidth

        parent.strokeStyle = strokeStyle
        parent.fillStyle = fillStyle
        parent.lineWidth = lineWidth

        parent.contextFunction()

        parent.strokeStyle = oldStrokeStyle
        parent.fillStyle = oldFillStyle
        parent.lineWidth = oldLineWidth
    }
}

data class StyleHolder(
    val strokeStyle: dynamic = "#000",
    val fillStyle: dynamic = "#fff",
    val lineWidth: Double = 1.0
)