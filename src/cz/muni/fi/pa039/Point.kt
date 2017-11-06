package cz.muni.fi.pa039

data class Point(var x: Int, var y: Int) {
    operator fun minus(other: Point): Point {
        return Point(x - other.x, other.y - y)
    }

    operator fun times(other: Point): Int {
        return x * other.x + y * other.y
    }

    fun vectorLength(): Double {
        return Math.sqrt(x.toDouble() * x + y * y)
    }
}