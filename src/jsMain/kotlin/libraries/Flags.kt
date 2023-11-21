package libraries

import core.Stats

object Flags: Library<Flag>() {
    fun register(str: String) = register(str, Flag(str))
    val automagic = register("automagic")
    val escapism1 = register("escapism1")
    val escapism2 = register("escapism2")
    val seenTutorial = register("seen_tutorial")
    val respecUnlocked = register("respec_unlocked")
    val betterClockworkCostScaling = register("better_clockwork_cost_scaling")
    val paraAlpha = register("para_alpha")
    val reachedDuality = register("reached_duality")
}

data class Flag(val name: String) {
    fun isUnlocked() = Stats.hasFlag(this)
    fun add() = Stats.addFlag(this)
    fun remove() = Stats.removeFlag(this)
}