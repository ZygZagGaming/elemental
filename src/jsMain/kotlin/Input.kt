@file:Suppress("ObjectPropertyName")

import kotlinx.browser.document
import org.w3c.dom.events.KeyboardEvent

data class Key(val key: String) {
    constructor(event: KeyboardEvent): this(event.key)
}

interface Indexable<K, V> {
    operator fun get(key: K): V
}

interface MutableIndexable<K, V>: Indexable<K, V> {
    operator fun set(key: K, value: V)
}

open class Keybind(val id: String, var key: Key, var down: (Double) -> Unit = { }, var up: (Double) -> Unit = { }, var tickDown: (Double, Double, Int) -> Unit) {
    val keyDown get() = Input.keyDownMap[key]
    val keyHoldLength get() = Input.keyHeldTimeMap[key]
    val keyHoldTicks get() = Input.keyHeldTicksMap[key]
    val keyPressedThisTick get() = Input.keyPressedThisTick[key]
    val keyReleasedThisTick get() = Input.keyReleasedThisTick[key]
    fun tick(dt: Double) {
        if (keyPressedThisTick) down(dt)
        if (keyReleasedThisTick) up(dt)
        if (keyDown) tickDown(dt, keyHoldLength, keyHoldTicks)
    }
}

class KeyclickerKeybind(val clicker: Clicker): Keybind("keyclicker-${clicker.id}", Key(clicker.id.toString()), down = { dt ->
    if (!clicker.docked && clicker.mode == ClickerMode.MANUAL) clicker.clickPercent = 1.0
}, tickDown = { dt, _, _ ->
    if (!clicker.docked && clicker.mode == ClickerMode.MANUAL) clicker.clickPercent += dt * clicker.cps
})

object Input {
    private val _keyDownMap = mutableDefaultedMapOf<Key, Boolean>(false)
    val keyDownMap get() = _keyDownMap.unmutable()
    private val _keyHeldTimeMap = mutableDefaultedMapOf<Key, Double>(0.0)
    val keyHeldTimeMap get() = _keyHeldTimeMap.unmutable()
    private val _keyHeldTicksMap = mutableDefaultedMapOf<Key, Int>(0)
    val keyHeldTicksMap get() = _keyHeldTicksMap.unmutable()
    private val _keyUpTimeMap = mutableDefaultedMapOf<Key, Double>(0.0)
    val keyUpTimeMap get() = _keyUpTimeMap.unmutable()
    private val _keyUpTicksMap = mutableDefaultedMapOf<Key, Int>(0)
    val keyUpTicksMap get() = _keyUpTicksMap.unmutable()
    val keyPressedThisTick = object: Indexable<Key, Boolean> {
        override fun get(key: Key) = _keyHeldTicksMap[key] == 1
    }
    val keyReleasedThisTick = object: Indexable<Key, Boolean> {
        override fun get(key: Key) = _keyUpTicksMap[key] == 1
    }
    val keybinds = mutableMapOf<String, Keybind>()

    fun addKeybind(keybind: Keybind) {
        keybinds[keybind.id] = keybind
    }

    fun removeKeybind(id: String) {
        keybinds.remove(id)
    }

    init {
        document.onkeydown = { event ->
            if (!event.repeat) _keyDownMap[Key(event)] = true

            Unit
        }
        document.onkeyup = { event ->
            _keyDownMap[Key(event)] = false

            Unit
        }
    }

    fun tick(dt: Double) {
        _keyDownMap.entries.forEach { (key, down) ->
            if (down) {
                _keyHeldTicksMap[key]++
                _keyHeldTimeMap[key] += dt
                _keyUpTicksMap[key] = 0
                _keyUpTimeMap[key] = 0.0
            } else {
                _keyUpTicksMap[key]++
                _keyUpTimeMap[key] += dt
                _keyHeldTicksMap[key] = 0
                _keyHeldTimeMap[key] = 0.0
            }
        }
        keybinds.values.forEach { it.tick(dt) }
    }
}