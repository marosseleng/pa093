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
 * Returns a [Point] that represents the center of this edge
 */
fun Edge.center() = Point((first.x + second.x) / 2, (first.y + second.y) / 2)

/**
 * Class representing a triangle that is defined by the [kotlin.collections.Set] of points
 */
class Triangle(first: Point, second: Point, third: Point) {

    val pointsAsSet = setOf(first, second, third)
    val pointsAsList = listOf(first, second, third)

    constructor(edge: Edge, point: Point): this(edge.first, edge.second, point)

    override fun equals(other: Any?): Boolean {
        return other != null && other is Triangle && this.pointsAsSet == other.pointsAsSet
    }

    override fun hashCode(): Int {
        return pointsAsSet.hashCode()
    }

    fun getEdges(): Set<Edge> {
        val pointsList = pointsAsSet.toList()
        val firstEdge = Edge(pointsList[0], pointsList[1])
        return setOf(firstEdge, firstEdge.to(pointsList[2]), firstEdge.to(pointsList[2]).to(pointsList[0]))
    }
}

/**
 * Class representing a [Circle] that is represented by its [center] and [radius]
 */
class Circle(val center: Point, val radius: Double) {
    override fun equals(other: Any?): Boolean {
        return other != null && other is Circle && other.center == this.center
    }

    override fun hashCode(): Int {
        return center.hashCode()
    }
}

/**
 * Returns [kotlin.collections.Map] of resulting triangles and their circum circles
 */
fun computeDelaunay(points: Set<Point>): Map<Triangle, Circle> {
    if (points.count() < 3) {
        return emptyMap()
    }

    val edgesToCheck = mutableListOf<Edge>()
    val triangulation = mutableSetOf<Edge>()
    val trianglesAndCircles = mutableMapOf<Triangle, Circle>()

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
                ?: return emptyMap()
    }

    triangulation.add(firstEdge)
    triangulation.add(firstEdge.to(thirdPoint))
    triangulation.add(Edge(thirdPoint, firstPoint))

    val triangle = Triangle(firstEdge.first, firstEdge.second, thirdPoint)
    val circumCircle = circumCircle(triangle)
    if (circumCircle != null) {
        trianglesAndCircles[triangle] = circumCircle
    }

    edgesToCheck.add(firstEdge)
    edgesToCheck.add(firstEdge.to(thirdPoint))
    edgesToCheck.add(Edge(thirdPoint, firstPoint))

    while (edgesToCheck.isNotEmpty()) {
        // take reversed edge to keep CCW rule
        val nextEdge = edgesToCheck[0].reversed()
        val bestPoint = getPointWithSmallestDelaunayDistance(nextEdge, points - nextEdge.first - nextEdge.second)
        if (bestPoint != null) {
            val triangle = Triangle(nextEdge.first, nextEdge.second, bestPoint)
            val circumCircle = circumCircle(triangle)
            if (circumCircle != null) {
                trianglesAndCircles[triangle] = circumCircle
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

    return trianglesAndCircles
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
    val circumCircle = circumCircle(Triangle(edge, point)) ?: return Double.MAX_VALUE
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
fun circumCircle(triangle: Triangle): Circle? {
    val points = triangle.pointsAsList
    val p1 = points[0]
    val p2 = points[1]
    val p3 = points[2]
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