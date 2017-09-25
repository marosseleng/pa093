package cz.muni.fi.pa039

class Point(var x: Int, var y: Int) {
    override fun equals(other: Any?) = other != null && other is Point && other.x == this.x && other.y == this.y
    override fun hashCode() = 37 * (this.x + 19 * this.y)
}
