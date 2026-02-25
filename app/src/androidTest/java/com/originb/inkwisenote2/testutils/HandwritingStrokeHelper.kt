package com.originb.inkwisenote2.testutils

import kotlin.math.cos
import kotlin.math.sin

object HandwritingStrokeHelper {
    // Base coordinates for writing
    private var START_X = 100f
    private var BASE_Y = 300f
    private const val LETTER_SPACING = 50f

    fun getHelloWorldStrokes(x: Float, y: Float): MutableList<MutableList<Point?>> {
        val allStrokes: MutableList<MutableList<Point?>> = ArrayList<MutableList<Point?>>()
        START_X += x
        BASE_Y += y
        var currentX = START_X

        // "H"
        allStrokes.add(createVerticalLine(currentX, BASE_Y, 150f)) // left vertical
        allStrokes.add(createHorizontalLine(currentX, currentX + 30, BASE_Y + 50)) // middle horizontal
        allStrokes.add(createVerticalLine(currentX + 30, BASE_Y, 150f)) // right vertical
        currentX += LETTER_SPACING

        // "e"
        allStrokes.add(createLowerE(currentX, BASE_Y))
        currentX += LETTER_SPACING

        // "l"
        allStrokes.add(createVerticalLine(currentX, BASE_Y - 50, 100f))
        currentX += LETTER_SPACING

        // "l"
        allStrokes.add(createVerticalLine(currentX, BASE_Y - 50, 100f))
        currentX += LETTER_SPACING

        // "o"
        allStrokes.add(createLowerO(currentX, BASE_Y))
        currentX += LETTER_SPACING + 20

        // Space between words
        currentX += LETTER_SPACING

        // "W"
        allStrokes.add(createUpperW(currentX, BASE_Y))
        currentX += LETTER_SPACING + 20

        // "o"
        allStrokes.add(createLowerO(currentX, BASE_Y))
        currentX += LETTER_SPACING

        // "r"
        allStrokes.add(createLowerR(currentX, BASE_Y))
        currentX += LETTER_SPACING

        // "l"
        allStrokes.add(createVerticalLine(currentX, BASE_Y - 50, 100f))
        currentX += LETTER_SPACING

        // "d"
        allStrokes.add(createLowerD(currentX, BASE_Y))
        currentX += LETTER_SPACING

        // "!"
        allStrokes.add(createExclamationMark(currentX, BASE_Y))

        return allStrokes
    }

    private fun createVerticalLine(x: Float, startY: Float, height: Float): MutableList<Point?> {
        val points: MutableList<Point?> = ArrayList<Point?>()
        var i = 0
        while (i <= height) {
            points.add(Point(x, startY + i))
            i += 2
        }
        return points
    }

    private fun createHorizontalLine(startX: Float, endX: Float, y: Float): MutableList<Point?> {
        val points: MutableList<Point?> = ArrayList<Point?>()
        var x = startX
        while (x <= endX) {
            points.add(Point(x, y))
            x += 2f
        }
        return points
    }

    private fun createLowerE(x: Float, y: Float): MutableList<Point?> {
        val points: MutableList<Point?> = ArrayList<Point?>()
        val radius = 15f

        // Create a circular motion for 'e'
        run {
            var i = 45
            while (i <= 360) {
                val angle = Math.toRadians(i.toDouble())
                val pointX = x + (radius * cos(angle)).toFloat()
                val pointY = y + (radius * sin(angle)).toFloat()
                points.add(Point(pointX, pointY))
                i += 5
            }
        }

        // Add the middle horizontal stroke
        var i = x - radius
        while (i <= x + radius) {
            points.add(Point(i, y))
            i += 2f
        }

        return points
    }

    private fun createLowerO(x: Float, y: Float): MutableList<Point?> {
        val points: MutableList<Point?> = ArrayList<Point?>()
        val radius = 15f

        var i = 0
        while (i <= 360) {
            val angle = Math.toRadians(i.toDouble())
            val pointX = x + (radius * cos(angle)).toFloat()
            val pointY = y + (radius * sin(angle)).toFloat()
            points.add(Point(pointX, pointY))
            i += 5
        }

        return points
    }

    private fun createUpperW(x: Float, y: Float): MutableList<Point?> {
        val points: MutableList<Point?> = ArrayList<Point?>()
        val width = 40f
        val height = 50f

        // First diagonal down
        run {
            var i = 0f
            while (i <= width / 4) {
                points.add(Point(x + i, y - height + i * 2))
                i += 2f
            }
        }

        // First diagonal up
        run {
            var i = 0f
            while (i <= width / 4) {
                points.add(Point(x + width / 4 + i, y - height / 2 - i * 2))
                i += 2f
            }
        }

        // Second diagonal down
        run {
            var i = 0f
            while (i <= width / 4) {
                points.add(Point(x + width / 2 + i, y - height + i * 2))
                i += 2f
            }
        }

        // Second diagonal up
        var i = 0f
        while (i <= width / 4) {
            points.add(Point(x + width * 3 / 4 + i, y - height / 2 - i * 2))
            i += 2f
        }

        return points
    }

    private fun createLowerR(x: Float, y: Float): MutableList<Point?> {
        val points: MutableList<Point?> = ArrayList<Point?>()

        // Vertical line
        run {
            var i = 0f
            while (i <= 30) {
                points.add(Point(x, y - i))
                i += 2f
            }
        }

        // Curve at top
        var i = 0
        while (i <= 90) {
            val angle = Math.toRadians(i.toDouble())
            val pointX = x + (15 * cos(angle)).toFloat()
            val pointY = (y - 30) + (15 * sin(angle)).toFloat()
            points.add(Point(pointX, pointY))
            i += 5
        }

        return points
    }

    private fun createLowerD(x: Float, y: Float): MutableList<Point?> {
        val points: MutableList<Point?> = ArrayList<Point?>()

        // Vertical line
        run {
            var i = 0f
            while (i <= 100) {
                points.add(Point(x, y - i))
                i += 2f
            }
        }

        // Circle part
        val radius = 15f
        var i = 0
        while (i <= 360) {
            val angle = Math.toRadians(i.toDouble())
            val pointX = (x - radius) + (radius * cos(angle)).toFloat()
            val pointY = y + (radius * sin(angle)).toFloat()
            points.add(Point(pointX, pointY))
            i += 5
        }

        return points
    }

    private fun createExclamationMark(x: Float, y: Float): MutableList<Point?> {
        val points: MutableList<Point?> = ArrayList<Point?>()

        // Vertical line
        run {
            var i = 0f
            while (i <= 40) {
                points.add(Point(x, y - i))
                i += 2f
            }
        }

        // Dot
        var i = 0
        while (i <= 360) {
            val angle = Math.toRadians(i.toDouble())
            val pointX = x + (2 * cos(angle)).toFloat()
            val pointY = (y + 10) + (2 * sin(angle)).toFloat()
            points.add(Point(pointX, pointY))
            i += 30
        }

        return points
    }

    class Point(@JvmField var x: Float, @JvmField var y: Float)
}