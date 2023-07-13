import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlin.js.Date

fun save(saveMode: SaveMode = Options.saveMode) {
    when (saveMode) {
        SaveMode.LOCAL_STORAGE -> saveLocalStorage()
    }
}

fun load(saveMode: SaveMode = Options.saveMode) {
    when (saveMode) {
        SaveMode.LOCAL_STORAGE -> loadLocalStorage()
    }
}

fun saveLocalStorage() {
    console.log("Saving game to local storage...")
    document.apply {
        localStorage["elementAmts"] = Elements.map.map { (k, v) -> "$k:${Stats.elementAmounts[v]}" }.joinToString(separator = ",")
        localStorage["reactionAmts"] = SpecialReactions.map.map { (k, v) -> "$k:${v.nTimesUsed}" }.joinToString(separator = ",")
        localStorage["timestamp"] = Date().toDateString()
        localStorage["timeSpent"] = gameState.timeSpent.toString()
        localStorage["autoclickerPositions"] = gameState.clickersById.map { (id, clicker) -> "${id}:${if (clicker.docked) "docked" else "${clicker.htmlElement.style.left},${clicker.htmlElement.style.top}"}" }.joinToString(separator = ";")
        localStorage["elementDeltas"] = Elements.map.map { (k, v) -> "$k:${Stats.elementDeltas[v]}" }.joinToString(separator = ",")
        localStorage["autoclickerSettings"] = gameState.clickersById.map { (id, clicker) -> "${id}:(${clicker.mode},${Input.keybinds["keyclicker-$id"]!!.key.key})" }.joinToString(separator = ";")
        localStorage["flags"] = Stats.flags.joinToString(separator = ",")
    }
}

fun loadLocalStorage() {
    console.log("Loading game from local storage...")
    document.apply {
        val timestamp = localStorage["timestamp"]
        if (timestamp != "") {
            localStorage["reactionAmts"].split(',').forEach {
                val pair = it.split(':')
                val reaction = SpecialReactions.map[pair[0]]
                if (reaction != null) repeat(pair[1].toInt()) { reaction.execute() }
            }
            localStorage["elementAmts"].split(',').forEach {
                val pair = it.split(':')
                val element = Elements.map[pair[0]]
                if (element != null) Stats.elementAmounts[element] = pair[1].toDouble()
            }
            gameState.timeSpent = localStorage["timeSpent"].toDouble()
            simulateTime((Date().getUTCMilliseconds() - Date(timestamp).getUTCMilliseconds()) / 1000.0)
            val positions = localStorage["autoclickerPositions"].split(";").filter { it != "" }.associate {
                val pair = it.split(":")
                val pair2 = pair[1].split(",")
                pair[0].toInt() to (if (pair2.size == 2) pair2[0] to pair2[1] else null)
            }
            gameState.clickersById.forEach { (id, it) ->
                val pos = positions[id]
                if (pos != null) {
                    GameTimer.nextTick { _ ->
                        it.docked = false
                        it.htmlElement.style.left = pos.first
                        it.htmlElement.style.top = pos.second
                        it.canvasParent.style.left = pos.first
                        it.canvasParent.style.top = pos.second
                    }
                } else {
                    it.moveToDock(force = true)
                }
            }
            localStorage["elementDeltas"].split(',').forEach {
                val pair = it.split(':')
                val element = Elements.map[pair[0]]
                if (element != null) Stats.elementDeltas[element] = pair[1].toDouble()
            }
            localStorage["autoclickerSettings"].split(';').forEach {
                val pair = it.split(':')
                if (pair.size == 2) {
                    val pair2 = pair[1].trim('(', ')').split(',')
                    val clicker = gameState.clickersById[pair[0].toInt()]
                    if (clicker != null) {
                        clicker.setMode(ClickerMode.valueOf(pair2[0]))
                        Input.keybinds["keyclicker-${pair[0]}"]!!.key = Key(pair2[1])
                    }
                }
            }
            Stats.flags.addAll(localStorage["flags"].split(','))
        }
    }
}

fun simulateTime(dt: Double) {
    gameState.tick(dt, offline = true)
    GameTimer.tick(dt)
}