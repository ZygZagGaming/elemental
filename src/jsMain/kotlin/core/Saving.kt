package core

import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import libraries.*
import kotlin.js.Date

@OptIn(ExperimentalJsExport::class)
@JsExport
fun save() {
    save(Options.saveMode)
}

fun save(saveMode: SaveMode) {
    when (saveMode) {
        SaveMode.LOCAL_STORAGE -> saveLocalStorage()
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun load() {
    load(Options.saveMode)
}

fun load(saveMode: SaveMode) {
    when (saveMode) {
        SaveMode.LOCAL_STORAGE -> loadLocalStorage()
    }
}

fun saveLocalStorage() {
    console.log("Saving game to local storage...")
    saveToMindexable(SimpleMutableIndexable({
        localStorage[it]
    }, { a, b ->
        localStorage[a] = b
    }))
//    document.apply {
//        localStorage["tutorialsSeen"] = Stats.tutorialsSeen.joinToString(separator = ",") { Tutorials.id(it) ?: "" }
//        localStorage["elementAmts"] = Elements.map.map { (k, v) -> "$k:${Stats.elementAmounts[v]}" }.joinToString(separator = ",")
//        localStorage["reactionAmts"] = SpecialReactions.map.map { (k, v) -> "$k:${v.nTimesUsed}" }.joinToString(separator = ",")
//        localStorage["timestamp"] = Date().toDateString()
//        localStorage["timeSpent"] = gameState.timeSpent.toString()
//        localStorage["autoclickerPositions"] = gameState.clickersById.map { (id, clicker) -> "${id}:${if (clicker.docked) "docked" else "${clicker.htmlElement.style.left},${clicker.htmlElement.style.top}"}" }.joinToString(separator = ";")
//        localStorage["elementDeltas"] = Elements.map.map { (k, v) -> "$k:${Stats.elementDeltas[v]}" }.joinToString(separator = ",")
//        localStorage["autoclickerSettings"] = gameState.clickersById.map { (id, clicker) -> "${id}:(${clicker.mode},${Input.keybinds["keyclicker-$id"]!!.key.key})" }.joinToString(separator = ";")
//        localStorage["flags"] = Stats.flags.joinToString(separator = ",")
//        localStorage["page"] = Pages.id(DynamicHTMLManager.shownPage) ?: ""
//        localStorage["dualityMilestoneAmts"] = DualityMilestones.map.map { (k, v) -> "$k:${v.nTimesUsed}" }.joinToString(separator = ",")
//        localStorage["timeSinceLastDuality"] = Stats.timeSinceLastDuality.toString()
//        localStorage["deltaReactionAmts"] = DeltaReactions.map.map { (k, v) -> "$k:${v.nTimesUsed}" }.joinToString(separator = ",")
//    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun saveToClipboard() {
    console.log("Saving game to clipboard...")
    val text = saveToText()
    window.navigator.clipboard.writeText(text).then({
        window.alert("Successfully copied save file!")
    }, {
        window.alert("Clipboard write failed. Make sure you're on https!")
    })
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun loadSaveFromClipboard() {
    console.log("Loading game from clipboard...")
    window.navigator.clipboard.readText().then({
        loadSaveFromText(it)
    },
    {
        window.alert("Unable to read clipboard. Make sure you're on https!")
    })
}

fun loadSaveFromText(text: String) {
    val entryList = text.trim().split("\\\\").map { it.trim() }
    val map = entryList.associate { it.split("|").firstTwo() }
    loadIndexable(SimpleIndexable { map[it] ?: "" })
}

fun saveToMindexable(mutableIndexable: MutableIndexable<String, String>) {
    document.apply {
        mutableIndexable["tutorialsSeen"] = Stats.tutorialsSeen.joinToString(separator = ",") { Tutorials.id(it) ?: "" }
        mutableIndexable["elementAmts"] = Resources.map.map { (k, v) -> "$k:${Stats.elementAmounts[v]}" }.joinToString(separator = ",")
        mutableIndexable["reactionAmts"] = SpecialReactions.map.map { (k, v) -> "$k:${v.nTimesUsed}" }.joinToString(separator = ",")
        mutableIndexable["timestamp"] = Date().toDateString()
        mutableIndexable["timeSpent"] = gameState.timeSpent.toString()
        //mutableIndexable["autoclickerPositions"] = gameState.clickersById.map { (id, clicker) -> "${id}:${if (clicker.docked) "docked" else "${clicker.htmlElement.style.left},${clicker.htmlElement.style.top}"}" }.joinToString(separator = ";")
        mutableIndexable["elementDeltas"] = Resources.map.map { (k, v) -> "$k:${Stats.elementDeltas[v]}" }.joinToString(separator = ",")
        //mutableIndexable["autoclickerSettings"] = gameState.clickersById.map { (id, clicker) -> "${id}:(${clicker.mode},${Input.keybinds["keyclicker-$id"]!!.key.key})" }.joinToString(separator = ";")
        mutableIndexable["flags"] = Stats.flags.map { it.name }.joinToString(separator = ",")
        mutableIndexable["page"] = Pages.id(DynamicHTMLManager.shownPage) ?: ""
        mutableIndexable["dualityMilestoneAmts"] = DualityMilestones.map.map { (k, v) -> "$k:${v.nTimesUsed}" }.joinToString(separator = ",")
        mutableIndexable["timeSinceLastDuality"] = Stats.timeSinceLastDuality.toString()
        mutableIndexable["deltaReactionAmts"] = DeltaReactions.map.map { (k, v) -> "$k:${v.nTimesUsed}" }.joinToString(separator = ",")
        mutableIndexable["game_version"] = gameVersion
        // format: id;page;mode1!mode2!mode3;mode1;autoCps;heldCps;left;top,
        mutableIndexable["clickerData"] = gameState.clickersById.map { (id, clicker) -> "$id;${clicker.page.name};${clicker.modesUnlocked.joinToString(separator = "!") { it.name }};${clicker.mode.name};${clicker.autoCps};${clicker.heldCps};${if (clicker.docked) "dock;dock" else "${clicker.htmlElement.style.left};${clicker.htmlElement.style.top}"}" }.joinToString(separator = ",")
    }
}

fun loadIndexable(indexable: Indexable<String, String>) {
    document.apply {
        val timestamp = indexable["timestamp"]
        val savedGameVersion = indexable["game_version"]
        if (savedGameVersion == gameVersion && timestamp != "") {
            Stats.flags.addAll(indexable["flags"].split(',').mapNotNull { Flags.map[it] })
            indexable["tutorialsSeen"].split(',').map { Tutorials.map[it] }.forEach {
                if (it != null) Stats.tutorialsSeen.add(it)
            }
            indexable["reactionAmts"].split(',').forEach {
                val pair = it.split(':')
                val reaction = SpecialReactions.map[pair[0]]
                if (reaction != null) repeat(pair[1].toInt()) { reaction.execute(true) }
            }
            indexable["elementAmts"].split(',').forEach {
                val pair = it.split(':')
                val element = Resources.map[pair[0]]
                if (element != null) Stats.elementAmounts[element] = pair[1].toDouble()
            }
            indexable["elementDeltas"].split(',').forEach {
                val pair = it.split(':')
                val element = Resources.map[pair[0]]
                if (element != null) {
                    Stats.elementDeltas[element] = pair[1].toDouble()
                    if (element.isElement) Stats.baseElementUpperBounds[element.delta] = pair[1].toDouble()
                }

            }
            gameState.timeSpent = indexable["timeSpent"].toDouble() ?: 0.0
            //simulateTime((Date().getUTCMilliseconds() - Date(timestamp ?: "").getUTCMilliseconds()) / 1000.0)
//            val positions = indexable["autoclickerPositions"].split(";").filter { it != "" }.associate {
//                val pair = it.split(":")
//                val pair2 = pair[1].split(",")
//                pair[0].toInt() to (if (pair2.size == 2) pair2[0] to pair2[1] else null)
//            }
//            indexable["autoclickerSettings"].split(';').forEach {
//                val pair = it.split(':')
//                if (pair.size == 2) {
//                    val pair2 = pair[1].trim('(', ')').split(',')
//                    val clicker = gameState.clickersById[pair[0].toInt()]
//                    if (clicker != null) {
//                        clicker.setMode(ClickerMode.valueOf(pair2[0]))
//                        Input.keybinds["keyclicker-${pair[0]}"]!!.key = Key(pair2[1])
//                    }
//                }
//            }
            DynamicHTMLManager.shownPage = Pages.map[indexable["page"]]!!
//            gameState.clickersById.forEach { (id, it) ->
//                val pos = positions[id]
//                if (pos != null) {
//                    //GameTimer.nextTick { _ ->
//                        it.docked = false
//                        it.htmlElement.style.left = pos.first
//                        it.htmlElement.style.top = pos.second
//                        it.canvasParent.style.left = pos.first
//                        it.canvasParent.style.top = pos.second
//                    //}
//                } else {
//                    it.moveToDock(force = true, source = "null position")
//                }
//            }
            indexable["dualityMilestoneAmts"].split(',').forEach {
                val pair = it.split(':')
                val reaction = DualityMilestones.map[pair[0]]
                if (reaction != null) repeat(pair[1].toInt()) { reaction.execute(true) }
            }
            indexable["deltaReactionAmts"].split(',').forEach {
                val pair = it.split(':')
                val reaction = DeltaReactions.map[pair[0]]
                if (reaction != null) repeat(pair[1].toInt()) { reaction.execute(true) }
            }
            Stats.timeSinceLastDuality = try {
                indexable["timeSinceLastDuality"].toDouble()
            } catch (_: Exception) { 0.0 }

            // format: id;page;mode1!mode2!mode3;mode1;autoCps;heldCps;left;top,
            indexable["clickerData"].split(',').forEach {
                val split = it.split(';')
                if (split.size > 7) {
                    val id = split[0].toInt()
                    val page = Pages.map[split[1]] ?: Pages.elementsPage
                    val modesUnlocked = split[2].split("!").map { str -> ClickerMode.valueOf(str) }
                    val chosenMode = ClickerMode.valueOf(split[3])
                    val autoCps = split[4].toDouble()
                    val heldCps = split[5].toDouble()
                    val left = split[6]
                    val top = split[7]
                    val clicker = Clicker(id, page, chosenMode, autoCps, heldCps)
                    clicker.modesUnlocked.addAll(modesUnlocked)
                    gameState.addClicker(clicker)
                    if (left == "dock") clicker.moveToDock(true)
                    else {
                        clicker.htmlElement.style.left = left
                        clicker.htmlElement.style.top = top
                    }
                }
            }
        }
    }
}

fun saveToText(): String {
    console.log("Saving game to string...")
    val backer = mutableMapOf<String, String>()
    val mindexable = SimpleMutableIndexable<String, String>({
        backer[it] ?: ""
    }, { a, b ->
        backer[a] = b
    })
    saveToMindexable(mindexable)
    return backer.entries.joinToString("\\\\\n") { (a, b) -> "$a|$b" } + "\\\\"
}

fun loadLocalStorage() {
    console.log("Loading game from local storage...")
    loadIndexable(SimpleIndexable {
        localStorage[it]
    })
//    document.apply {
//        val timestamp = localStorage["timestamp"]
//        if (timestamp != "") {
//            localStorage["tutorialsSeen"].split(',').map { Tutorials.map[it] }.forEach {
//                if (it != null) Stats.tutorialsSeen.add(it)
//            }
//            localStorage["reactionAmts"].split(',').forEach {
//                val pair = it.split(':')
//                val reaction = SpecialReactions.map[pair[0]]
//                if (reaction != null) repeat(pair[1].toInt()) { reaction.execute(true) }
//            }
//            localStorage["elementAmts"].split(',').forEach {
//                val pair = it.split(':')
//                val element = Elements.map[pair[0]]
//                if (element != null) Stats.elementAmounts[element] = pair[1].toDouble()
//            }
//            localStorage["elementDeltas"].split(',').forEach {
//                val pair = it.split(':')
//                val element = Elements.map[pair[0]]
//                if (element != null) {
//                    Stats.elementDeltas[element] = pair[1].toDouble()
//                    if (element.isBasic) Stats.baseElementUpperBounds[element.delta] = pair[1].toDouble()
//                }
//            }
//            gameState.timeSpent = localStorage["timeSpent"].toDouble()
//            simulateTime((Date().getUTCMilliseconds() - Date(timestamp).getUTCMilliseconds()) / 1000.0)
//            val positions = localStorage["autoclickerPositions"].split(";").filter { it != "" }.associate {
//                val pair = it.split(":")
//                val pair2 = pair[1].split(",")
//                pair[0].toInt() to (if (pair2.size == 2) pair2[0] to pair2[1] else null)
//            }
//            localStorage["autoclickerSettings"].split(';').forEach {
//                val pair = it.split(':')
//                if (pair.size == 2) {
//                    val pair2 = pair[1].trim('(', ')').split(',')
//                    val clicker = gameState.clickersById[pair[0].toInt()]
//                    if (clicker != null) {
//                        clicker.setMode(ClickerMode.valueOf(pair2[0]))
//                        Input.keybinds["keyclicker-${pair[0]}"]!!.key = Key(pair2[1])
//                    }
//                }
//            }
//            Stats.flags.addAll(localStorage["flags"].split(','))
//            DynamicHTMLManager.shownPage = Pages.map[localStorage["page"]]!!
//            if ("clickersUnlocked" in Stats.flags) {
//                var n = 1
//                while (gameState.clickersById.size < 5) {
//                    val clicker = Clicker(n++, Pages.elementsPage, ClickerMode.DISABLED, 4.0, 6.0)
//                    gameState.addClicker(clicker)
//                    GameTimer.nextTick {
//                        clicker.moveToDock(true)
//                    }
//                }
//            }
//            gameState.clickersById.forEach { (id, it) ->
//                val pos = positions[id]
//                if (pos != null) {
//                    GameTimer.nextTick { _ ->
//                        it.docked = false
//                        it.htmlElement.style.left = pos.first
//                        it.htmlElement.style.top = pos.second
//                        it.canvasParent.style.left = pos.first
//                        it.canvasParent.style.top = pos.second
//                    }
//                } else {
//                    it.moveToDock(force = true)
//                }
//            }
//            localStorage["dualityMilestoneAmts"].split(',').forEach {
//                val pair = it.split(':')
//                val reaction = DualityMilestones.map[pair[0]]
//                if (reaction != null) repeat(pair[1].toInt()) { reaction.execute(true) }
//            }
//            localStorage["deltaReactionAmts"].split(',').forEach {
//                val pair = it.split(':')
//                val reaction = DeltaReactions.map[pair[0]]
//                if (reaction != null) repeat(pair[1].toInt()) { reaction.execute(true) }
//            }
//            Stats.timeSinceLastDuality = try { localStorage["timeSinceLastDuality"].toDouble() } catch (_: Exception) { 0.0 }
//        }
//    }
}

fun simulateTime(dt: Double) {
    gameState.tick(dt, offline = true)
    GameTimer.tick(dt)
}