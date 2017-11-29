package cz.muni.fi.pa093

import cz.muni.fi.pa093.impl.*
import processing.core.PApplet
import java.awt.Color

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
            val suitableTriangles = delaunay.keys.filter { it.pointsAsSet.containsAll(line.toList()) }

            stroke(voronoiLineColor.rgb)
            fill(voronoiLineColor.rgb)
            when (suitableTriangles.count()) {
                1 -> {
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

private fun findOuterPoint(circleCenter: Point, centerOfLine: Point, triangle: Triangle, edge: Edge): Point {
    val point = (triangle.pointsAsList - edge.first - edge.second).first()

    val vector = if (pointsFormLeftTurn(circleCenter, edge.second, edge.first) == pointsFormLeftTurn(point, edge.second, edge.first)) {
        // stred kruznice je vnutri trojuholnika
        Point(centerOfLine.x - circleCenter.x, centerOfLine.y - circleCenter.y)
    } else {
        // stred kruznice je mimo trojuholnika
        Point(circleCenter.x - centerOfLine.x, circleCenter.y - centerOfLine.y)
    }

    // fixme!
    return circleCenter + (vector * 500.0)
}
