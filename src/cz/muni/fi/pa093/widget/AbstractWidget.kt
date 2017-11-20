package cz.muni.fi.pa093.widget

import processing.core.PApplet

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
