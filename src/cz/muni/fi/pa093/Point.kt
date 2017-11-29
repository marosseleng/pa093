package cz.muni.fi.pa093

/**
 * Data class representing a point in the 2D space
 */
data class Point(var x: Double, var y: Double) {

    constructor(x: Int, y: Int): this(x.toDouble(), y.toDouble())

    operator fun minus(other: Point): Point {
        return Point(x - other.x, other.y - y)
    }

    operator fun plus(other: Point): Point {
        return Point(x + other.x, other.y + y)
    }

    operator fun times(other: Point): Double {
        return x * other.x + y * other.y
    }

    operator fun times(coeff: Double): Point {
        return Point(x * coeff, y * coeff)
    }

    fun vectorLength(): Double {
        return Math.sqrt(x * x + y * y)
    }

    fun distanceTo(other: Point): Double {
        return Math.sqrt(Math.pow(this.x - other.x, 2.0) + Math.pow(this.y - other.y, 2.0))
    }
}