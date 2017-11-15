package cz.muni.fi.pa093

val lexicographicComparator = Comparator<Point> { first, second ->
    if (first.y != second.y) {
        second.y - first.y
    } else {
        first.x - second.x
    }
}

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
 *
 */
fun angleBetween3(first: Point, center: Point, last: Point): Double {
    val v1 = first - center
    val v2 = last - center
    val lengthV1 = v1.vectorLength()
    val lengthV2 = v2.vectorLength()
    val dotProduct = v1 * v2
    return Math.acos(dotProduct / (lengthV1 * lengthV2))
}

fun pointsFormLeftTurnOrLine(first: Point, center: Point, last: Point): Boolean {
    return ((first.x - center.x) * (center.y - last.y)) - ((center.y - first.y) * (last.x - center.x)) > 0
}