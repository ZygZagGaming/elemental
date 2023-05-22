@file:Suppress("ObjectPropertyName")

import kotlinx.browser.document
import org.w3c.dom.events.KeyboardEvent

data class Key(val key: String) {
    constructor(event: KeyboardEvent): this(event.key)
}

interface Indexable<K, V> {
    operator fun get(key: K): V
}

object Input {
    private val _keyDownMap = mutableDefaultedMapOf<Key, Boolean>(false)
    val keyDownMap get() = _keyDownMap.unmutable()
    private val _keyHeldTimeMap = mutableDefaultedMapOf<Key, Double>(0.0)
    val keyHeldTimeMap get() = _keyHeldTimeMap.unmutable()
    private val _keyHeldTicksMap = mutableDefaultedMapOf<Key, Int>(0)
    val keyHeldTicksMap get() = _keyHeldTicksMap.unmutable()
    val keyPressedThisTick = object: Indexable<Key, Boolean> {
        override fun get(key: Key) = _keyHeldTicksMap[key] == 1
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
            } else {
                _keyHeldTicksMap[key] = 0
                _keyHeldTimeMap[key] = 0.0
            }
        }
    }
}