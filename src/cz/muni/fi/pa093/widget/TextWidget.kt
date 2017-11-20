package cz.muni.fi.pa093.widget

import cz.muni.fi.pa093.Point
import processing.core.PApplet

class TextWidget(var bottomLeft: Point, var text: String, override val id: Int) : AbstractWidget {
    override var isSelected = false
    override val fill = 0
    override fun pointIsInside(x: Int, y: Int): Boolean {
        return false
    }

    override fun draw(applet: PApplet) {
        with(applet) {
            fill(0)
            stroke(0)
            text(text, bottomLeft.x.toFloat(), bottomLeft.y.toFloat())
        }
    }
}
