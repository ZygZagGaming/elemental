import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.Image
import kotlin.math.*

fun visuals(gameState: GameState) {
    when (DynamicHTMLManager.shownPage) {
        "elements" -> elementsPageVisuals(gameState)
    }
}

fun alchemyContainerVisuals(gameState: GameState, alchemyContainer: HTMLElement) {
    val elements = alchemyContainer.children
    val half = alchemyContainer.getBoundingClientRect().width / 2
    val radius = half * 0.8
    val n = elements.length - 2
    val portion = 2 * PI / n
    val visuals = elements.toList().last() as HTMLCanvasElement
    val middle = Vec2(half, half)
    val size = vw(30.0)
    visuals.width = size.roundToInt()
    visuals.height = size.roundToInt()
    val elementRadius = vw(1.5)
    (visuals.getContext("2d") as CanvasRenderingContext2D).apply {
        strokeStyle = "#000000"
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
        for (i in 1..n) {
            lineTo(half + polygonRadius * sin(portion * i), half + polygonRadius * cos(portion * i))
        }
        stroke()

        val k = 0.5
        val angle = graphicalHeatAmount * 2 * PI
        val strokeWidth = vw(0.75)
        if (gameState.elementAmounts[Elements.heat] <= 1e-6) {
            lineWidth = strokeWidth
            beginPath()
            strokeStyle = "#cccccc"
            arc(half, half, elementRadius + strokeWidth / 2, 0.0, 2 * PI)
            stroke()
        } else {
            val big = graphicalHeatAmount > 0.75
            lineWidth = strokeWidth * (if (big) 3.0 / 2 else 1.0)
            beginPath()
            strokeStyle = if (big) createRadialGradient(half, half, elementRadius, half, half, elementRadius + strokeWidth * 3 / 2 - 2).apply {
                addColorStop(0.0, "rgba(255, 0, 0, 1)")
                addColorStop(0.66, "rgba(255, 0, 0, 1)")
                addColorStop(1.0, "rgba(255, 0, 0, 0)")
            } else "#ff0000"
            arc(half, half, elementRadius + strokeWidth * (if (big) 1.5 else 1.0) / 2, k * PI, k * PI + angle)
            stroke()

            lineWidth = strokeWidth
            beginPath()
            strokeStyle = "#cccccc"
            arc(half, half, elementRadius + strokeWidth / 2, k * PI + angle, (k + 2) * PI)
            stroke()
        }

        val reaction = gameState.hoveredReaction
        val canDoReaction = gameState.canDoReaction(reaction)
        for ((element, _) in reaction.inputs) {
            if (element.symbol !in listOf(Symbols.catalyst, Symbols.heat) && reaction.inputs[element] != 0.0) {
                lineWidth = 10.0
                val relative = getAlchemyElementPos(element.symbol) * radius
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
            if (element.symbol !in listOf(Symbols.catalyst, Symbols.heat) && reaction.outputs[element] != 0.0) {
                lineWidth = 10.0
                val relative = getAlchemyElementPos(element.symbol) * radius
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
    }
}


const val margin = 50.0

fun autoclickerVisuals(gameState: GameState, autoclicker: AutoClicker) {
    val canvas = autoclicker.canvas
    (canvas.getContext("2d") as CanvasRenderingContext2D).apply {
        val size = autoclicker.htmlElement.clientWidth.toDouble()
        fillStyle = "rgba(255, 255, 255, 0)"
        clearRect(-margin, -margin, size + margin, size + margin)

        strokeStyle = "#ffffff"
        lineWidth = 3.0
        beginPath()
        arc(size / 2, size * (1 - 0.35), size * 0.35 - lineWidth / 2, 0.0, 2 * PI)
        stroke()

        fillStyle = "#ffffff"
        beginPath()
        arc(size / 2, size * (1 - 0.35), size * 0.15, 0.0, 2 * PI)
        fill()

        val animSpeed = 20
        val slc = autoclicker.sinceLastClick * animSpeed
        val arrowHeightAmount = (if (slc < 1) slc * slc else 1 - (slc - 1) * (slc - 1)).clamp(0.0..1.0)
        val arrowHeight = size * 0.2 * (1 - arrowHeightAmount)
        beginPath()
        moveTo(size / 2, arrowHeight)
        lineTo(size * 0.2, arrowHeight + size * 0.3)
        lineTo(size / 2, arrowHeight + size * 0.1)
        lineTo(size * 0.8, arrowHeight + size * 0.3)
        fill()
    }
}

fun elementsPageVisuals(gameState: GameState) {
    val alchemyContainers = document.getElementsByClassName("alchemy-container")
    for (alchemyContainer in alchemyContainers) alchemyContainerVisuals(gameState, alchemyContainer)
    val autoclickers = gameState.autoclickers
    for (autoclicker in autoclickers) autoclickerVisuals(gameState, autoclicker)
}

fun CanvasRenderingContext2D.gradientLine(posA: Vec2, posB: Vec2, r: Int, g: Int, b: Int, a: Double, width: Double) {
    val len = (posA - posB).magnitude
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

val graphicalHeatAmount get() = gameState.elementAmounts[Elements.heat] / Stats.functionalElementCaps[Elements.heat]
fun CanvasRenderingContext2D.gradientLineColorBar(posA: Vec2, posB: Vec2, r: Int, g: Int, b: Int, a: Double, width: Double, colorBarPosition: Double, colorBarWidth: Double, colorBarOpacity: Double) {
    val len = (posA - posB).magnitude
    val avg = (posA + posB) / 2.0
    val diff = posA - posB
    val transpose = Vec2(-diff.y, diff.x).normalized * width / 2.0
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

    val secondaryGradient = createLinearGradient(posA.x, posA.y, posB.x, posB.y)
    secondaryGradient.addColorStop(0.0, "rgba(255, 255, 255, 0.0)")
    secondaryGradient.addColorStop(max(0.0, colorBarPosition - colorBarWidth), "rgba(255, 255, 255, 0.0)")
    secondaryGradient.addColorStop(colorBarPosition, "rgba(255, 255, 255, $colorBarOpacity)")
    secondaryGradient.addColorStop(min(1.0, colorBarPosition + colorBarWidth), "rgba(255, 255, 255, 0.0)")
    secondaryGradient.addColorStop(1.0, "rgba(255, 255, 255, 0.0)")
    strokeStyle = secondaryGradient
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

fun Double.clamp(range: ClosedFloatingPointRange<Double>): Double {
    return if (this in range) this else if (this < range.start) range.start else range.endInclusive
}