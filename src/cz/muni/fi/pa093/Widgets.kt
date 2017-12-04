package cz.muni.fi.pa093

import processing.core.PApplet

/**
 * Simple abstract class representing a UI widget
 */
interface AbstractWidget {
    var isSelected: Boolean
    val id: Int
    val stroke: Int
        get() = 0

    val fill: Int
        get() = if (isSelected) 230 else 255

    fun pointIsInside(x: Int, y: Int): Boolean
    fun draw(applet: PApplet)
}

/**
 * Widget used to handle user text input
 */
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

/**
 * Button-like widget
 */
class ButtonWidget(topLeft: Point, width: Int, height: Int, text: String, id: Int) : InputFieldWidget(topLeft, width, height, text, id)