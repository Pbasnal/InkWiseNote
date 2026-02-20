package com.originb.inkwisenote2.modules.handwrittennotes.data

import java.io.Serializable

/**
 * Represents a single stroke in a handwritten note
 */
class Stroke : Serializable {
    var color: Int = 0
    var width: Float = 0f
    var points: MutableList<StrokePoint?>?

    constructor() {
        this.points = ArrayList<StrokePoint?>()
    }

    constructor(color: Int, width: Float) {
        this.color = color
        this.width = width
        this.points = ArrayList<StrokePoint?>()
    }

    fun addPoint(point: StrokePoint?) {
        if (points == null) {
            points = ArrayList<StrokePoint?>()
        }
        points!!.add(point)
    }

    fun addPoint(x: Float, y: Float, pressure: Float) {
        addPoint(StrokePoint(x, y, pressure))
    }

    fun addPoint(x: Float, y: Float, pressure: Float, timestamp: Long) {
        addPoint(StrokePoint(x, y, pressure, timestamp))
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}