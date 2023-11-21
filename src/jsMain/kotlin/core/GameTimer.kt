package core

import kotlin.js.Date

object GameTimer {
    private val date get() = Date()
    fun timeMillis() = date.getTime()
    fun timeSex() = timeMillis() / 1000.0
    private val tickers = mutableMapOf<String, Ticker>()
    private val tempTickers = mutableListOf<Ticker>()
    fun registerTicker(name: String, ticker: Ticker) {
        tickers[name] = ticker
    }
    fun removeTicker(name: String) {
        tickers.remove(name)
    }
    var lastTick = timeSex()
    var tpsDisplay = false
    fun tick(dt: Double) {
        log("Start of tick")
        log("Beginning ticking tickers")
        for ((name, ticker) in tickers) {
            log("Beginning ticking $name")
            ticker(dt)
            log("Finished ticking $name")
        }
        log("Finished ticking tickers")
        log("Beginning ticking temporary tickers")
        for (ticker in tempTickers) {
            ticker(dt)
            tempTickers.remove(ticker)
        }
        log("Finished ticking temporary tickers")
        if (tpsDisplay) {
            log("Roughly ${1 / dt} ticks per second")
        }
    }

    fun beginTicking() {
        lastTick = timeSex()
        val handler = {
            val dt = timeSex() - lastTick
            lastTick = timeSex()
            if (profiling) console.log("Last tick: $lastTick")
            tick(dt)
            if (profiling) console.log("End of tick")
        }
        js("window.setInterval(handler, 10)")
    }

    fun nextTick(ticker: Ticker) {
        tempTickers += ticker
    }

    fun every(interval: Double, dt: Double) = (gameState.timeSpent - dt).mod(interval) > gameState.timeSpent.mod(interval)
}

typealias Ticker = (Double) -> Unit