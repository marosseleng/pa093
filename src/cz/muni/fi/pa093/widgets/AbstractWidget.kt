package cz.muni.fi.pa093.widgets

interface AbstractWidget {
    var isSelected: Boolean
    val id: Int
    val stroke: Int
        get() = 0

    val fill: Int
        get() = if (isSelected) 230 else 255

    fun pointIsInside(x: Int, y: Int): Boolean
}
