package cz.muni.fi.pa039

val lexicographicComparator = Comparator<Point> { first, second ->
    if (first.y != second.y) {
        first.y - second.y
    } else {
        first.x - second.x
    }
}