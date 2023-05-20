import kotlinx.browser.window
import kotlin.js.Date

object GameTimer {
    private val date get() = Date()
    fun timeMillis() = date.getTime()
    fun timeSex() = timeMillis() / 1000.0
    private val tickers = mutableListOf<Ticker>()
    private val namedTickers = mutableMapOf<String, Ticker>()
    fun registerTicker(ticker: Ticker) {
        tickers += ticker
    }
    fun registerNamedTicker(name: String, ticker: Ticker) {
        namedTickers[name] = ticker
    }
    var lastTick = timeSex()
    fun tick(dt: Double) {
        for (ticker in tickers) ticker(dt)
        for ((_, ticker) in namedTickers) ticker(dt)
    }

    fun beginTicking() {
        val dt = timeSex() - lastTick
        tick(dt)
        window.setTimeout({ beginTicking() }, 1)
        lastTick = timeSex()
    }
}typealias Ticker = (Double) -> Unit