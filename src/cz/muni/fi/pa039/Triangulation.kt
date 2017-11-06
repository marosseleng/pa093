package cz.muni.fi.pa039

import java.util.*

/**
 * This function expects the list of points defining the polygon
 */
fun triangulation(points: List<Point>): List<Pair<Point, Point>> {
    val top = points.minWith(lexicographicComparator)
    val rightPath = LinkedList<Point>()
    val leftPath = LinkedList<Point>()

    var indexOfTop = points.indexOf(top)
    points.forEachIndexed { index, point ->
        if (index in 1 until indexOfTop) {
            // right path <- needs to be reversed
            rightPath.push(point)
        } else if (index > indexOfTop) {
            // left path
            leftPath.push(point)
        }
    }
    rightPath.reverse()

    val sorted = points.sortedWith(lexicographicComparator)

    val stack = LinkedList<Point>()
    stack.push(sorted.first())
    stack.push(sorted[1])
    for (i in 2..sorted.lastIndex) {
        leftPath.contains(stack.peek())
    }

















    TODO()
}