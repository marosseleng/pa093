package cz.muni.fi.pa093.widgets

import cz.muni.fi.pa093.Point

class TextWidget(var bottomLeft: Point, var text: String, override val id: Int) : AbstractWidget {
    override var isSelected = false
    override val fill = 0
    override fun pointIsInside(x: Int, y: Int): Boolean {
        return false
    }
}
