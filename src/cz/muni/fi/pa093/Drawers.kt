/*
 * This file contains extension functions used to draw stuff. This makes the drawing itself separated
 * from the "computation" code, which makes the code cleaner and more readable
 */

package cz.muni.fi.pa093

import cz.muni.fi.pa093.impl.*
import processing.core.PApplet
import java.awt.Color

/**
 * Draws the given [kotlin.collections.Iterable] of points the size of point is specified in [cz.muni.fi.pa093.pointDiameter] variable
 * A point is black by default
 */
fun PApplet.drawPoints(points: Iterable<Point>) {
    points.forEach { p ->
        fill(0)
        stroke(0)
        ellipse(p.x.toFloat(), p.y.toFloat(), pointDiameter.toFloat(), pointDiameter.toFloat())
    }
}

/**
 * Draws the given [kotlin.collections.Iterable] of lines
 * A line is black by default
 */
fun PApplet.drawLines(lines: Iterable<Edge>) {
    lines.forEach {
        drawLine(it)
    }
}

/**
 * Draws a single line, specified by two points, represented as [Edge]
 * The line is black by default
 */
fun PApplet.drawLine(line: Edge) {
    val (previous, p) = line
    fill(0)
    stroke(0)
    line(previous.x.toFloat(), previous.y.toFloat(), p.x.toFloat(), p.y.toFloat())
}

/**
 * Given the points in the clockwise order, this function draws the polygon of these points
 * The polygon is black by default
 */
fun PApplet.drawPolygon(polygonPoints: List<Point>, polygonClosed: Boolean) {
    var previous = if (polygonClosed) {
        polygonPoints.last()
    } else {
        polygonPoints.first()
    }
    polygonPoints.forEach { p ->
        drawLine(Edge(previous, p))
        previous = p
    }
}

/**
 * Recursively draws the kD tree specified by its root [KdNode]
 */
fun PApplet.drawKdTree(node: KdNode?) {
    if (node == null) {
        return
    }
    val color = colors[node.depth % numOfColors]
    stroke(color)
    fill(color)
    ellipse(node.point.x.toFloat(), node.point.y.toFloat(), pointDiameter.toFloat(), pointDiameter.toFloat())

    val boundary = getLineBoundary(node) ?: return

    if (node.depth % 2 != 0) {
        line(boundary.first.toFloat(), node.point.y.toFloat(), boundary.second.toFloat(), node.point.y.toFloat())
    } else {
        line(node.point.x.toFloat(), boundary.first.toFloat(), node.point.x.toFloat(), boundary.second.toFloat())
    }
    drawKdTree(node.lesser)
    drawKdTree(node.greater)
}

/**
 * Responsible for drawing the Delaunay triangulation, circum circles of the respectives triangles and the voronoi diagram
 */
fun PApplet.drawDelaunayOrVoronoi(delaunay: Map<Triangle, Circle>, delaunayTriangulationEnabled: Boolean, voronoiDiagramEnabled: Boolean, circumCirclesEnabled: Boolean) {
    val distinctLines = delaunay.keys.flatMap { it.getEdges() }.distinctBy { setOf(it.first, it.second) }

    val triangulationLineColor = if (delaunayTriangulationEnabled && voronoiDiagramEnabled) {
        Color(125, 125, 125)
    } else {
        Color.BLACK
    }

    val voronoiLineColor = if (delaunayTriangulationEnabled && voronoiDiagramEnabled) {
        Color(255, 81, 81)
    } else {
        Color.RED
    }

    if (delaunayTriangulationEnabled) {
        // triangulation
        distinctLines.forEach {
            fill(triangulationLineColor.rgb)
            stroke(triangulationLineColor.rgb)
            line(it.first.x.toFloat(), it.first.y.toFloat(), it.second.x.toFloat(), it.second.y.toFloat())
        }
    }

    // Print circles
    if (circumCirclesEnabled) {
        delaunay.values.forEach {
            stroke(Color.MAGENTA.rgb, 80.0f)
            fill(255, 0.0f)
            ellipse(it.center.x.toFloat(), it.center.y.toFloat(), it.radius.toFloat() * 2.0f, it.radius.toFloat() * 2.0f)
            fill(Color.MAGENTA.rgb, 80.0f)
            ellipse(it.center.x.toFloat(), it.center.y.toFloat(), 4.0f, 4.0f)
        }
    }

    // voronoi
    if (voronoiDiagramEnabled) {
        distinctLines.forEach { line ->
            // find triangles containing a line
            val suitableTriangles = delaunay.keys.filter { it.pointsAsSet.containsAll(line.toList()) }

            stroke(voronoiLineColor.rgb)
            fill(voronoiLineColor.rgb)

            // each line lies in either one or two triangles
            when (suitableTriangles.count()) {
                1 -> {
                    // line lies on the convex hull
                    val triangle = suitableTriangles.first()
                    val circle = delaunay[triangle]
                    if (circle != null) {
                        val startingPoint = circle.center
                        val endingPoint = findOuterPoint(circle.center, line.center(), triangle, line)
                        line(startingPoint.x.toFloat(), startingPoint.y.toFloat(), endingPoint.x.toFloat(), endingPoint.y.toFloat())
                    }
                }
                2 -> {
                    val firstCircle = delaunay[suitableTriangles[0]]?.center
                    val secondCircle = delaunay[suitableTriangles[1]]?.center
                    if (firstCircle != null && secondCircle != null) {
                        line(firstCircle.x.toFloat(), firstCircle.y.toFloat(), secondCircle.x.toFloat(), secondCircle.y.toFloat())
                    }
                }
            }
        }
    }
}

/**
 * For the given triangle and edge (center of edge) finds the end point of an edge of the voronoi diagram
 */
private fun findOuterPoint(circleCenter: Point, centerOfLine: Point, triangle: Triangle, edge: Edge): Point {
    val point = (triangle.pointsAsList - edge.first - edge.second).first()

    val vector = if (pointsFormLeftTurn(circleCenter, edge.second, edge.first) == pointsFormLeftTurn(point, edge.second, edge.first)) {
        // the center of the circle is inside of a triangle
        Point(centerOfLine.x - circleCenter.x, centerOfLine.y - circleCenter.y)
    } else {
        // the center of the circle is outside of a triangle
        Point(circleCenter.x - centerOfLine.x, circleCenter.y - centerOfLine.y)
    }

    val largerDistance = maxOf((windowSize - circleCenter.x), (windowSize - circleCenter.y))

    // magic equation to determine the large-enough cordinates for the outer points
    return circleCenter + (vector * (largerDistance / vector.vectorLength()))
}
