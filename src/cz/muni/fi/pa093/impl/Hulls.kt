package cz.muni.fi.pa093.impl

import cz.muni.fi.pa093.Point
import cz.muni.fi.pa093.angleBetween3
import cz.muni.fi.pa093.pointsFormLeftTurn
import java.util.*

/**
 * Performs the gift wrapping algorithm
 */
fun giftWrapping(points: Set<Point>): List<Point> {
    if (points.isEmpty() || points.count() < 3) {
        return emptyList()
    }
    val startingPoint = points.maxBy { it.y } ?: Point(0, 0)
    val result = mutableListOf<Point>()
    // get a point that is little bit left from the starting point
    var previous = Point(startingPoint.x - 10, startingPoint.y)
    var current = startingPoint

    do {
        result.add(current)

        val remaining = if (result.count() <= 2) { points - result } else { points - result + startingPoint }
        val nextPoint = remaining.minBy { next ->
            // because acos is the angle in the clockwise order. we want min in the counterclockwise
            Math.PI - angleBetween3(previous, current, next)
        } ?: Point(0, 0)

        previous = current
        current = nextPoint
    } while (current != startingPoint)

    return result
}

/**
 * Performs the Graham Scan algorithm to find the convex hull
 */
fun grahamScan(points: Set<Point>): List<Point> {
    if (points.isEmpty() || points.count() < 3) {
        return emptyList()
    }

    val startingPoint = points.maxWith(Comparator { first, second ->
        (if (first.y != second.y) {
            Math.signum(first.y - second.y)
        } else {
            Math.signum(second.x - first.x)
        }).toInt()
    }) ?: Point(0, 0)

    val xAxisEnd = startingPoint.copy(x = startingPoint.x + 10)
    val remaining = (points - startingPoint).sortedBy { angleBetween3(xAxisEnd, startingPoint, it) }
    val stack = LinkedList<Point>()
    stack.push(startingPoint)
    stack.push(remaining.first())
    var j = 1
    while (j < remaining.count()) {
        // retrieving items like this, because this queue implemenation pushes to the front
        val first = stack[stack.lastIndex - stack.lastIndex]
        val second = stack[stack.lastIndex - stack.lastIndex + 1]
        val third = remaining[j]
        if (pointsFormLeftTurn(first, second, third)) {
            stack.push(third)
            j += 1
        } else {
            stack.pop()
        }
    }

    return stack
}