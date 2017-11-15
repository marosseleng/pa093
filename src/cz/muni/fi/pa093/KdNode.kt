package cz.muni.fi.pa093

class KdNode(val k: Int = 2, var depth: Int = 0, val point: Point, val parent: KdNode? = null, var lesser: KdNode? = null, var greater: KdNode?= null) {
    operator fun compareTo(other: KdNode): Int {
        // going top-left way ==> the point is less than iff it lays to the top or to the left (depending on (depth % 2)) of other
        return if (depth % 2 == 0) {
            // left--right
            if (point.x - other.point.x == 0) {
                -1
            } else {
                point.x - other.point.x
            }

        } else {
            // up--down
            if (other.point.y - point.y == 0) {
                -1
            } else {
                other.point.y - point.y
            }
        }
    }
}