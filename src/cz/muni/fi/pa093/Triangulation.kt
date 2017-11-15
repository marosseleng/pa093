package cz.muni.fi.pa093

import java.util.*

/**
 * This function expects the list of points defining the polygon
 */
fun triangulation(points: List<Point>): List<Pair<Point, Point>> {
    val top = points.maxWith(lexicographicComparator)
    val rightPath = LinkedList<Point>()
    val leftPath = LinkedList<Point>()

    val result = mutableListOf<Pair<Point, Point>>()

    val indexOfTop = points.indexOf(top)
    points.forEachIndexed { index, point ->
        if (index < indexOfTop) {
            // right path <- needs to be reversed
            rightPath.push(point)
        } else {
            // left path
            leftPath.push(point)
        }
    }
    rightPath.reverse()

    val sorted = points.sortedWith(lexicographicComparator).reversed()

    val stack = LinkedList<Point>()
    stack.push(sorted.first())
    stack.push(sorted[1])
    for (i in 2..sorted.lastIndex) {
        val currentPoint = sorted[i]
        val topOfStack = stack.peek()
        if ((rightPath.contains(currentPoint) && rightPath.contains(topOfStack)) || (leftPath.contains(currentPoint) && leftPath.contains(topOfStack))) {
            // same path
            if (stack.count() > 1) {
                val pointsToPop = findPointsToPop(stack, currentPoint, leftPath)
                // do not pop the last suitable point (Vk)
                repeat(pointsToPop) {
                    val something = stack.pop()
                    result.add(Pair(currentPoint, something))
                }
                result.add(Pair(currentPoint, stack.peek()))
                stack.push(currentPoint)
            }
        } else {
            while (stack.isNotEmpty()) {
                result += Pair(currentPoint, stack.pop())
            }
            stack.push(topOfStack)
            stack.push(currentPoint)
        }
    }

    return result
}

fun findPointsToPop(stack: Deque<Point>, currentPoint: Point, leftPath: Deque<Point>): Int {
    val newStack = LinkedList<Point>(stack)
    var top = newStack.pop()
    var someCounter = 1

    var foundCorrectPoint = false

    while (newStack.isNotEmpty()) {
        val tmp = newStack.pop()
        someCounter++
        if (leftPath.contains(currentPoint)) {
            // should be clockwise
            // currentPoint----top----tmp
            if (pointsFormLeftTurnOrLine(currentPoint, top, tmp)) {
                foundCorrectPoint = true
            } else {
                if (foundCorrectPoint) {
                    someCounter -= 1
                    break
                }
            }
        } else {
            // right path
            // should be counter-clockwise
            // currentPoint----top----tmp
            if (pointsFormLeftTurnOrLine(tmp, top, currentPoint)) {
                foundCorrectPoint = true
            } else {
                if (foundCorrectPoint) {
                    someCounter -= 1
                    break
                }
            }
        }
        top = tmp
    }

    return if (foundCorrectPoint) { someCounter - 1 } else { 0 } // returning " - 1" because we want to leave the last popped point in the stack
}
