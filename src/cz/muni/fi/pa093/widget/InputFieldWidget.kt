package cz.muni.fi.pa093.widget

import cz.muni.fi.pa093.Point
import processing.core.PApplet

open class InputFieldWidget(private var topLeft: Point, private var width: Int, private var height: Int, var text: String, override val id: Int) : AbstractWidget {
    override var isSelected = false

    override fun pointIsInside(x: Int, y: Int): Boolean {
        return x >= this.topLeft.x && x <= this.topLeft.x + width && y >= this.topLeft.y && y <= this.topLeft.y + height
    }

    override fun draw(applet: PApplet) {
        with(applet) {
            fill(fill)
            stroke(stroke)
            val topLeftX = topLeft.x
            val topLeftY = topLeft.y
            rect(topLeftX.toFloat(), topLeftY.toFloat(), this@InputFieldWidget.width.toFloat(), this@InputFieldWidget.height.toFloat(), 2f)
            fill(stroke)
            text(text, (topLeftX + 5).toFloat(), (topLeftY + this@InputFieldWidget.height - 10).toFloat())
        }
    }
}
