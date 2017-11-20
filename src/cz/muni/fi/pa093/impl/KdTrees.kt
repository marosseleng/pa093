package cz.muni.fi.pa093.impl

import cz.muni.fi.pa093.Point
import cz.muni.fi.pa093.leftPanelSize
import cz.muni.fi.pa093.windowSize

/**
 * Class representing a node in the kD tree
 *
 * @property parent a pointer to this node's parent; null if this node is root
 * @property lesser a pointer to the subtree containing the part of the points that were lesser than this node (in either horizontal or vertical cut)
 * @property greater similar to [lesser], but contains greater points from cut
 * @property point a [Point] representing this node in the 2D space
 * @property depth the depth of the current node; the even [depth] represents the vertical cut
 */
class KdNode(val k: Int = 2, var depth: Int = 0, val point: Point, val parent: KdNode? = null, var lesser: KdNode? = null, var greater: KdNode?= null)

/**
 * Returns the [KdNode] representing the root of the kD tree iff there are some points; null otherwise
 */
fun constructKdTree(points: List<Point>, root: KdNode? = null, level: Int = 0): KdNode? {
    if (points.isEmpty()) {
        return null
    } else if (points.count() == 1) {
        return KdNode(depth = level, point = points.first(), parent = root)
    }

    val sorted = if (level % 2 == 0) {
        points.sortedBy { it.x }
    } else {
        points.sortedBy { it.y }
    }
    // find the root point of this 'level'
    val median = if (sorted.count() % 2 == 0) {
        sorted[((sorted.count() / 2) - 1)]
    } else {
        sorted[sorted.count() / 2]
    }

    val newRoot = KdNode(depth = level, point = median, parent = root)

    // recursively construct left and right subtrees
    return newRoot.apply {
        lesser = constructKdTree(sorted.subList(0, sorted.indexOf(median)), root = newRoot, level = level + 1)
        greater = constructKdTree(sorted.subList(sorted.indexOf(median) + 1, sorted.lastIndex + 1), root = newRoot, level = level + 1)
    }
}

/**
 * Returns the boundaries for the line drawn through [node]
 *
 * If the line is to be horizontal, this function returns a [kotlin.Pair] of x-coordinates
 * If the line is to be vertical,   this function returns a [kotlin.Pair] of y-coordinates
 */
fun getLineBoundary(node: KdNode?): Pair<Double, Double>? {
    if (node == null) {
        return null
    }
    val myParent = node.parent ?: // first line is top-to bottom
            return Pair(0.0, windowSize.toDouble())

    val iAmLeftChild = myParent.lesser == node

    return if (iAmLeftChild) {
        val startBoundary = getLineBoundary(node.parent.parent)
        if (node.depth % 2 != 0) {
            // horizontal
            Pair(startBoundary?.first ?: leftPanelSize + 1.0, myParent.point.x - 1.0)
        } else {
            Pair(startBoundary?.first ?: 0.0, myParent.point.y - 1.0)
        }
    } else {
        val endBoundary = getLineBoundary(node.parent.parent)
        if (node.depth % 2 != 0) {
            // horizontal
            Pair(myParent.point.x + 1.0, endBoundary?.second ?: windowSize.toDouble())
        } else {
            Pair(myParent.point.y + 1.0, endBoundary?.second ?: windowSize.toDouble())
        }
    }
}