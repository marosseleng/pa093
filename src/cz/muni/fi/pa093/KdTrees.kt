package cz.muni.fi.pa093

fun constructKdTree(points: List<Point>): KdNode? {
    return constructKdTreeRecursive(points = points, level = 0)
}

private fun constructKdTreeRecursive(points: List<Point>, root: KdNode? = null, level: Int): KdNode? {
    if (points.isEmpty()) {
        return null
    } else if (points.count() == 1) {
        return KdNode(depth = level, point = points.first(), parent = root)
    }

    val sorted = if (level % 2 == 0) {
        points.sortedBy { it.x }
    } else {
        points.sortedBy { it.y }//.reversed()
    }
    val median = if (sorted.count() % 2 == 0) {
        sorted[((sorted.count() / 2) - 1)]
    } else {
        sorted[sorted.count() / 2]
    }

    val newRoot = KdNode(depth = level, point = median, parent = root)

    return newRoot.apply {
        lesser = constructKdTreeRecursive(sorted.subList(0, sorted.indexOf(median)), root = newRoot, level = level + 1)
        greater = constructKdTreeRecursive(sorted.subList(sorted.indexOf(median) + 1, sorted.lastIndex + 1), root = newRoot, level =  level + 1)
    }
}