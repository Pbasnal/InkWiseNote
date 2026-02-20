package com.originb.inkwisenote2.modules.handwrittennotes.data

import java.io.Serializable

/**
 * Represents a single point in a stroke with coordinates, pressure and timestamp
 */
class StrokePoint : Serializable {
    @JvmField
    var x: Float = 0f
    @JvmField
    var y: Float = 0f
    var pressure: Float = 0f
    @JvmField
    var timestamp: Long

    constructor() {
        this.timestamp = System.currentTimeMillis()
    }

    constructor(x: Float, y: Float, pressure: Float) {
        this.x = x
        this.y = y
        this.pressure = pressure
        this.timestamp = System.currentTimeMillis()
    }

    constructor(x: Float, y: Float, pressure: Float, timestamp: Long) {
        this.x = x
        this.y = y
        this.pressure = pressure
        this.timestamp = timestamp
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}