package cz.muni.fi.pa039.widgets

import cz.muni.fi.pa039.Point

class PointWidget(var point: Point, var radius: Int) : AbstractWidget {
    override var isSelected: Boolean = false
    override val fill = 0
    override val id = -1
    override fun pointIsInside(x: Int, y: Int): Boolean {
        return Math.sqrt(Math.pow((x - this.point.x).toDouble(), 2.0) + Math.pow((y - this.point.y).toDouble(), 2.0)) <= radius
    }
}