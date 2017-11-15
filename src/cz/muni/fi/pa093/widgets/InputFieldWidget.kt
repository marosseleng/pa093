package cz.muni.fi.pa093.widgets

import cz.muni.fi.pa093.Point

open class InputFieldWidget(var topLeft: Point, var width: Int, var height: Int, var text: String, override val id: Int) : AbstractWidget {
    override var isSelected = false

    override fun pointIsInside(x: Int, y: Int): Boolean {
        return x >= this.topLeft.x && x <= this.topLeft.x + width && y >= this.topLeft.y && y <= this.topLeft.y + height
    }
}
