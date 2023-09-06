package libraries

import core.Stats

object Flags: Library<Flag>() {
    fun register(str: String) = register(str, Flag(str))
    val clickersUnlocked = register("clickers_unlocked")
    val automagic = register("automagic")
    val escapism1 = register("escapism1")
    val escapism2 = register("escapism2")
    val seenTutorial = register("seen_tutorial")
    val respecUnlocked = register("respec_unlocked")
}

data class Flag(val name: String) {
    fun isUnlocked() = Stats.hasFlag(this)
    fun add() = Stats.addFlag(this)
    fun remove() = Stats.removeFlag(this)
}