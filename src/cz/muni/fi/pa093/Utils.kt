/***************************************************
 * This file contains global constants definitions *
 * as well as some useful (extension) functions    *
 ***************************************************/

package cz.muni.fi.pa093

import java.awt.Color
import java.util.*

val windowSize = 1000
val pointDiameter = 8
val leftPanelSize = 320

val colors: List<Int> by lazy {
    arrayOf(Color.BLACK, Color.RED, Color.CYAN, Color.YELLOW, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.GRAY, Color.GREEN, Color.BLUE)
            .map { it.rgb }
}

val numOfColors = colors.size

val random = Random()

/**
 * Sorts the points lexicographically
 *
 * Implemented considering the fact that y grows down
 */
val lexicographicComparator = Comparator<Point> { first, second ->
    (if (first.y != second.y) {
        Math.signum(second.y - first.y)
    } else {
        Math.signum(first.x - second.x)
    }).toInt()
}

/**
 * Generates a random [Point] within the usable area of the Sketch
 */
fun randomPoint() = Point(
        x = random.nextInt(windowSize - leftPanelSize - pointDiameter) + leftPanelSize + pointDiameter / 2,
        y = random.nextInt(windowSize - pointDiameter) + pointDiameter / 2)

/**
 * last __
 *       \
 *        \
 *         \
 *          \
 *           \
 *            \
 *             \
 *  |-----------\ center
 * first
 * Computes the angle between the specified points (as shown above)
 */
fun angleBetween3(first: Point, center: Point, last: Point): Double {
    val v1 = first - center
    val v2 = last - center
    val lengthV1 = v1.vectorLength()
    val lengthV2 = v2.vectorLength()
    val dotProduct = v1 * v2
    return Math.acos(dotProduct / (lengthV1 * lengthV2))
}

/**
 * Returns true if specified points form the 'left turn'
 *
 * Do not forget to swap [first] and [last] if y-axis grows down (this case)
 */
fun pointsFormLeftTurn(first: Point, center: Point, last: Point): Boolean {
    return ((first.x - center.x) * (center.y - last.y)) - ((center.y - first.y) * (last.x - center.x)) > 0
}

/*
 * This function accepts the list of points defining a polygon.
 *
 * These points are put in the list in either clockwise or counter-clockwise order.
 *
 * This function will return the same list (containing the same points) but rotated (or mirrored) such that
 * the first point in the list will be the lower-most one and the button will be ordered in the counter-clockwise order
 *
 * 2 Conditions that guarantee the "correctness" of the polygon:
 *     * Index of the lowest point is 0
 *     * [1].y <= [0].y && [1].x > [0].x
 */
fun adjustPointsOrder(polygonPoints: List<Point>): List<Point> {
    val bottom = polygonPoints.minWith(lexicographicComparator) ?: Point(-1, -1)
    val indexOfBottom = polygonPoints.indexOf(bottom)
    val nextPoint = polygonPoints[(indexOfBottom + 1) % polygonPoints.count()]
    // first find out whether points were defined CW or CCW
    return if (nextPoint.y <= bottom.y && nextPoint.x > bottom.x) {
        // direction is CCW, just rotate!
        polygonPoints.rotate(-indexOfBottom)
    } else {
        // Clockwise direction, reverse the list and call recursively
        adjustPointsOrder(polygonPoints.reversed())
    }
}

/**
 * Rotates the items in the given list by the specified [amount]
 */
fun <T> List<T>.rotate(amount: Int): List<T> {
    val mutableList = toMutableList()
    Collections.rotate(mutableList, amount)
    return mutableList
}

/**
 * Returns the random RGB color as RGB int with alpha 255
 */
fun randomColorRGB() = Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)).rgb