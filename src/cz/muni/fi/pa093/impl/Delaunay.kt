package cz.muni.fi.pa093.impl

import cz.muni.fi.pa093.Point
import cz.muni.fi.pa093.pointsFormLeftTurn

/**
 * Typealias for the [kotlin.Pair] of [Point]s
 */
typealias Edge = Pair<Point, Point>

/**
 * Returns the reversed [Edge]
 */
fun Edge.reversed() = Edge(second, first)

/**
 * Returns the 'next' [Edge]
 */
fun Edge.to(point: Point) = Edge(second, point)

/**
 * Data class representing a [Circle] that is represented by its [center] and [radius]
 */
data class Circle(val center: Point, val radius: Double)

/**
 * Returns [kotlin.Pair] containing edges present in the triangulation and circum circles of resulting triangles
 */
fun computeDelaunay(points: Set<Point>): Pair<List<Edge>, Set<Circle>> {
    if (points.count() < 3) {
        return Pair(emptyList(), emptySet())
    }

    val edgesToCheck = mutableListOf<Edge>()
    val triangulation = mutableListOf<Edge>()
    val circles = mutableSetOf<Circle>()

    // first two points
    val firstPoint = points.sortedBy { it.x }.first()
    val closestPoint = (points - firstPoint).minBy { it.distanceTo(firstPoint) } ?: firstPoint

    var firstEdge = Edge(firstPoint, closestPoint)

    // find the first point!
    var thirdPoint = getPointWithSmallestDelaunayDistance(firstEdge, (points - firstPoint - closestPoint))

    if (thirdPoint == null) {
        // no suitable point to the left of this edge, reverse it!
        firstEdge = firstEdge.reversed()
        thirdPoint = getPointWithSmallestDelaunayDistance(firstEdge, (points - firstPoint - closestPoint))
                ?: return Pair(emptyList(), emptySet())
    }

    triangulation.add(firstEdge)
    triangulation.add(firstEdge.to(thirdPoint))
    triangulation.add(Edge(thirdPoint, firstPoint))

    val circumCircle = circumCircle(firstEdge.first, firstEdge.second, thirdPoint)
    if (circumCircle != null) {
        circles.add(circumCircle)
    }

    edgesToCheck.add(firstEdge)
    edgesToCheck.add(firstEdge.to(thirdPoint))
    edgesToCheck.add(Edge(thirdPoint, firstPoint))

    while (edgesToCheck.isNotEmpty()) {
        // take reversed edge to keep CCW rule
        val nextEdge = edgesToCheck[0].reversed()
        val bestPoint = getPointWithSmallestDelaunayDistance(nextEdge, points - nextEdge.first - nextEdge.second)
        if (bestPoint != null) {
            val circumCircle = circumCircle(nextEdge.first, nextEdge.second, bestPoint)
            if (circumCircle != null) {
                circles.add(circumCircle)
            }
            val second = nextEdge.to(bestPoint)
            val third = second.to(nextEdge.first)
            if (!edgesToCheck.contains(second) && !triangulation.contains(second.reversed())) {
                edgesToCheck.add(second)
            }
            if (!edgesToCheck.contains(third) && !triangulation.contains(third.reversed())) {
                edgesToCheck.add(third)
            }
            triangulation.add(nextEdge)
            triangulation.add(second)
            triangulation.add(third)
        }
        edgesToCheck.removeAt(0)
    }

    return Pair(triangulation, circles)
}

/**
 * Returns the [Point] with the smallest Dellaunay distance from [edge]
 *
 * Chooses from [points] that do the 'left turn' with [edge]
 */
fun getPointWithSmallestDelaunayDistance(edge: Edge, points: Collection<Point>) =
        points.filter { pointsFormLeftTurn(it, edge.second, edge.first) }
                .minBy { delaunayDistance(edge, it) }

/**
 * Compute the Delaunay distance between the given [edge] and [point]
 *
 * Returns [kotlin.Int.MAX_VALUE] iff all points lie on the same line
 */
fun delaunayDistance(edge: Edge, point: Point): Double {
    val circumCircle = circumCircle(point, edge.second, edge.first) ?: return Double.MAX_VALUE
    val radius = circumCircle.radius

    return if (pointsFormLeftTurn(circumCircle.center, edge.second, edge.first) == pointsFormLeftTurn(point, edge.second, edge.first)) {
        // if the center and the point are on the same side of an edge (they both form the same kind of turn (left or right))
        radius
    } else {
        // the point is on the other side of an edge than center
        -radius
    }
}

/**
 * Returns [Circle] iff the points are not colinear; null otherwise
 */
fun circumCircle(p1: Point, p2: Point, p3: Point): Circle? {
    val cp = crossProduct(p1, p2, p3)
    return if (cp != 0.0) {
        val p1Sq = p1.x * p1.x + p1.y * p1.y
        val p2Sq = p2.x * p2.x + p2.y * p2.y
        val p3Sq = p3.x * p3.x + p3.y * p3.y
        val num = p1Sq * (p2.y - p3.y) + p2Sq * (p3.y - p1.y) + p3Sq * (p1.y - p2.y)
        val cx = num / (2.0 * cp)
        val num2 = p1Sq * (p3.x - p2.x) + p2Sq * (p1.x - p3.x) + p3Sq * (p2.x - p1.x)
        val cy = num2 / (2.0 * cp)
        val center = Point(cx, cy)
        Circle(center, p1.distanceTo(center))
    } else {
        null
    }
}

/**
 * Computes the cross products of two vectors defined by points [p1], [p2] and [p3]
 */
fun crossProduct(p1: Point, p2: Point, p3: Point): Double {
    val u1 = p2.x - p1.x
    val v1 = p2.y - p1.y
    val u2 = p3.x - p1.x
    val v2 = p3.y - p1.y
    return u1 * v2 - v1 * u2
}