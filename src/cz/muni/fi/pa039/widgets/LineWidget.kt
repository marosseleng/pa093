package cz.muni.fi.pa039.widgets

import cz.muni.fi.pa039.Point
import java.awt.Color

class LineWidget(var first: Point, var second: Point) : AbstractWidget {
    override var isSelected = false
    override val id = -2
    override val fill = Color.RED.rgb

    override fun pointIsInside(x: Int, y: Int): Boolean {
        return false
    }
}
