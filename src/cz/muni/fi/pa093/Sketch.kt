package cz.muni.fi.pa093

import cz.muni.fi.pa093.widgets.*
import processing.core.PApplet
import processing.core.PConstants
import java.awt.Color
import java.util.*

/**
 * TODO:
 *     * dynamic point ellipse size (scales with window size)
 *     * CLEAR ALL keystroke
 */
class Sketch : PApplet() {

    private val points = HashSet<PointWidget>()
    private val widgets = HashSet<AbstractWidget>()

    private var polygonPoints = mutableListOf<Point>()
    private var polygonClosed: Boolean = false
        set(value) {
            // turn off the polygon definition
            field = value
            if (value) {
                polygonDefinitionEnabled = false
            }
        }

    /* SWITCHES */
    private var isInPointsEditingMode = true

    private var giftWrappingEnabled: Boolean = false
        set(value) {
            field = value
            if (grahamScanEnabled) {
                grahamScanEnabled = !value
            }
            if (!value && !grahamScanEnabled) {
                triangulationEnabled = false
                polygonPoints = mutableListOf()
                polygonClosed = false
            }
        }

    private var grahamScanEnabled: Boolean = false
        set(value) {
            field = value
            if (giftWrappingEnabled) {
                giftWrappingEnabled = !value
            }
            if (!value && !giftWrappingEnabled) {
                triangulationEnabled = false
                polygonPoints = mutableListOf()
                polygonClosed = false
            }
        }

    private val isConvexHullEnabled: Boolean
        get() = grahamScanEnabled || giftWrappingEnabled

    private var triangulationEnabled: Boolean = false

    private var polygonDefinitionEnabled: Boolean = false
        set(value) {
            field = value
            if (value) {
                points.clear()
                polygonPoints.clear()
                polygonClosed = false
                grahamScanEnabled = false
                giftWrappingEnabled = false
                kDTreesEnabled = false
            }
        }

    private var kDTreesEnabled: Boolean = false
        set(value) {
            field = value
            if (value) {
                grahamScanEnabled = false
                giftWrappingEnabled = false
                triangulationEnabled = false
                polygonDefinitionEnabled = false
            }
        }

    /*
     * A point we are currently dragging
     */
    private var currentDraggingPoint: PointWidget? = null
    /*
     * This variable serves as text input handler! When this is not null, all text input goes here
     */
    private var selectedInputField: InputFieldWidget? = null
    /*
     * Helper field to indicate that a button was selected
     */
    private var selectedButton: ButtonWidget? = null

    private val windowSize = 900
    private val diameter = 10 * windowSize / 1000
    private val leftPanelSize = 280

    private val random = Random()
    private val numOfColors = 20
    private val colors: IntArray by lazy {
        val lst = mutableListOf<Int>()
        repeat(numOfColors) {
            lst.add(randomColorRGB())
        }
        lst.toIntArray()
    }

    private fun handleWidgetClick(widget: AbstractWidget) {
        widget.isSelected = true
        if (widget is ButtonWidget) {
            selectedButton = widget
            handleButtonOnClickAction(widget.id)
        }
        if (widget is InputFieldWidget) {
            setSelectedInputField(widget)
            return
        }
        disableSelectedInputField()
    }

    private fun handleButtonOnClickAction(widgetId: Int) {
        when (widgetId) {
            Ids.RANDOM_POINTS_GENERATE_BUTTON -> generateRandomPoints()
        }
    }

    /*
     * Generates n random points where n is the user input
     */
    private fun generateRandomPoints() {
        widgets
                .filter { it.id == Ids.RANDOM_POINTS_INPUT_FIELD }
                .filterIsInstance<InputFieldWidget>()
                .map { it.text.toIntOrNull() ?: 0 }
                .flatMap { 0 until it }
                .forEach { points.add(PointWidget(randomPoint(), diameter / 2)) }
    }


    override fun settings() {
        size(windowSize, windowSize)
    }

    override fun setup() {
        background(255)
        fill(135f, 175f, 163f)
        rect(0f, 0f, leftPanelSize.toFloat(), height.toFloat())
        stroke(0)
        fill(0)
        widgets.add(TextWidget(Point(25, 250), "Number of random points:", Ids.RANDOM_POINTS_TITLE_TEXT))
        widgets.add(InputFieldWidget(Point(25, 260), 50, 30, "", Ids.RANDOM_POINTS_INPUT_FIELD))
        widgets.add(ButtonWidget(Point(80, 260), 90, 30, "GENERATE!", Ids.RANDOM_POINTS_GENERATE_BUTTON))
        polygonPoints = mutableListOf()
        polygonClosed = false
    }

    override fun draw() {
        background(255)
        fill(150f, 241f, 255f)
        stroke(150f, 241f, 255f)
        rect(0f, 0f, leftPanelSize.toFloat(), height.toFloat())
        fill(255f, 0f, 90f)
        text(if (isInPointsEditingMode) "[Enter] Points editing ENABLED." else "[Enter] Points editing DISABLED.", 25f, 25f)
        text(if (grahamScanEnabled) "[S] Graham Scan ENABLED." else "[S] Graham Scan DISABLED.", 25f, 50f)
        text(if (giftWrappingEnabled) "[G] Gift Wrapping ENABLED." else "[G] Gift Wrapping DISABLED.", 25f, 75f)
        text(if (triangulationEnabled) "[T] Triangulation ENABLED." else "[T] Triangulation DISABLED.", 25f, 100f)
        text(if (polygonDefinitionEnabled) "[P] Polygon definition ENABLED." else "[P] Polygon definition DISABLED.", 25f, 125f)
        text(if (kDTreesEnabled) "[K] k-d trees ENABLED." else "[K] k-d trees DISABLED.", 25f, 150f)
        text("[R] Add random point.", 25f, 175f)
        text("[L_MOUSE] Add new point / move point.", 25f, 200f)
        text("[R_MOUSE] Delete the point.", 25f, 225f)

        widgets.forEach(this::drawWidget)

        if (polygonPoints.isNotEmpty()) {
            var previous = if (polygonClosed) {
                polygonPoints.last()
            } else {
                polygonPoints.first()
            }
            polygonPoints.forEach { p ->
                fill(0)
                stroke(0)
                line(previous.x.toFloat(), previous.y.toFloat(), p.x.toFloat(), p.y.toFloat())
                previous = p
            }
        }

        /* CONVEX HULL */
        if ((grahamScanEnabled || giftWrappingEnabled) && points.size > 2) {
            polygonPoints = when {
                giftWrappingEnabled -> giftWrapping(points.map(PointWidget::point))
                grahamScanEnabled -> grahamScan(points.map(PointWidget::point))
                else -> emptyList()
            }.toMutableList()
            polygonClosed = polygonPoints.isNotEmpty()
            // draw convex hull
            if (polygonPoints.isNotEmpty()) {
                var previous = if (polygonClosed) {
                    polygonPoints.last()
                } else {
                    polygonPoints.first()
                }
                polygonPoints.forEach { p ->
                    fill(0)
                    stroke(0)
                    line(previous.x.toFloat(), previous.y.toFloat(), p.x.toFloat(), p.y.toFloat())
                    previous = p
                }
            }
        }

        /* TRIANGULATION */
        if (triangulationEnabled && polygonPoints.count() > 2) {
            /* HIDE POINTS THAT ARE NOT ON POLYGON, BUT DO NOT DELETE THEM */
            polygonPoints.forEach { p ->
                fill(0)
                stroke(0)
                ellipse(p.x.toFloat(), p.y.toFloat(), diameter.toFloat(), diameter.toFloat())
            }
            val triangulationLines = triangulation(adjustPointsOrder(polygonPoints))
            triangulationLines.forEach {
                fill(0)
                stroke(0)
                line(it.first.x.toFloat(), it.first.y.toFloat(), it.second.x.toFloat(), it.second.y.toFloat())
            }
        } else {
            /* DRAW ALL POINTS */
            points.forEach { p ->
                fill(0)
                stroke(0)
                ellipse(p.point.x.toFloat(), p.point.y.toFloat(), (p.radius * 2).toFloat(), (p.radius * 2).toFloat())
            }
        }

        if (kDTreesEnabled) {
            drawKdTree(constructKdTree(points.map { it.point }))
        }
    }

    private fun drawKdTree(node: KdNode?) {
        if (node == null) {
            return
        }
        val color = colors[node.depth % numOfColors]
        stroke(color)
        fill(color)
        ellipse(node.point.x.toFloat(), node.point.y.toFloat(), diameter.toFloat(), diameter.toFloat())

        val boundary = getLineBoundary(node) ?: return

        if (node.depth % 2 != 0) {
            line(boundary.first, node.point.y.toFloat(), boundary.second, node.point.y.toFloat())
        } else {
            line(node.point.x.toFloat(), boundary.first, node.point.x.toFloat(), boundary.second)
        }
        drawKdTree(node.lesser)
        drawKdTree(node.greater)
    }

    private fun getLineBoundary(node: KdNode?): Pair<Float, Float>? {
        if (node == null) {
            return null
        }
        val myParent = node.parent ?: // first line is top-to bottom
                return Pair(0.0f, windowSize.toFloat())
        val iAmLeftChild = myParent.lesser == node

        return if (iAmLeftChild) {
            val startBoundary = getLineBoundary(node.parent.parent)
            if (node.depth % 2 != 0) {
                // horizontal
                Pair(startBoundary?.first ?: leftPanelSize + 1.0f, myParent.point.x - 1.0f)
            } else {
                Pair(startBoundary?.first ?: 0.0f, myParent.point.y - 1.0f)
            }
        } else {
            val endBoundary = getLineBoundary(node.parent.parent)
            if (node.depth % 2 != 0) {
                // horizontal
                Pair(myParent.point.x + 1.0f, endBoundary?.second ?:  windowSize.toFloat())
            } else {
                Pair(myParent.point.y + 1.0f, endBoundary?.second ?: windowSize.toFloat())
            }
        }
    }

    private fun randomColorRGB() = Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)).rgb

    /*
     * This function accepts the list of points defining a polygon.
     *
     * These points are put in the list in either clockwise or counter-clockwise order.
     *
     * This function will return the same list (containing the same points) but rotated (or mirrored) such that
     * the first point in the list will be the lower-most one and the button will be ordered in the counter-clockwise order
     *
     * 2 Conditions that guarantee the "goodness" of the polygon:
     *     * Index of the lowest point is 0
     *     * [1].y <= [0].y && [1].x > [0].x
     */
    private fun adjustPointsOrder(polygonPoints: List<Point>): List<Point> {
        val bottom = polygonPoints.minWith(lexicographicComparator) ?: Point(-1, -1)
        val indexOfBottom = polygonPoints.indexOf(bottom)
        val nextPoint = polygonPoints[(indexOfBottom + 1) % polygonPoints.count()]
        // first find out whether points were defined CW or CCW
        return if (nextPoint.y <= bottom.y && nextPoint.x > bottom.x) {
            // direction is CCW, just rotate!
            polygonPoints.rotate(-indexOfBottom)
        } else {
            // Clockwise direction, reverse the list and call recursively
            adjustPointsOrder(polygonPoints.reversed())
        }
    }

    /* MOUSE EVENTS */

    override fun mousePressed() {
        if (isInPointsEditingMode) {
            if (mouseButton == PConstants.RIGHT) {
                deletePointContaining(mouseX, mouseY)
            } else {
                val found = findPointContaining(mouseX, mouseY)
                if (found == null) {
                    val pnt = Point(mouseX, mouseY)
                    if (polygonDefinitionEnabled) {
                        if (!polygonClosed) {
                            polygonPoints.add(pnt)
                        }
                    }
                    points.add(PointWidget(pnt, diameter / 2))
                } else {
                    currentDraggingPoint = found
                    if (found.point == polygonPoints.firstOrNull()) {
                        polygonClosed = true
                    }
                }
            }
        } else {
            val selected = widgets.firstOrNull { widget -> widget.pointIsInside(mouseX, mouseY) }
            if (selected != null) {
                handleWidgetClick(selected)
            } else {
                disableSelectedInputField()
            }
        }
    }

    override fun mouseReleased() {
        if (!isInPointsEditingMode) {
            if (selectedButton != null) {
                selectedButton?.isSelected = false
            }
            selectedButton = null
        } else {
            currentDraggingPoint = null
        }
    }

    override fun mouseDragged() {
        if (!isInPointsEditingMode) {
            return
        }
        if (currentDraggingPoint != null) {
            points.remove(currentDraggingPoint!!)// FIXME fugly!
            val p = PointWidget(Point(mouseX, mouseY), diameter / 2)
            currentDraggingPoint = p
            points.add(p)
        }
    }

    /* KEYBOARD EVENTS */

    override fun keyPressed() {
        if (selectedInputField != null) {
            selectedInputField!!.text += key
        } else {
            when (key) {
                PConstants.RETURN, PConstants.ENTER -> isInPointsEditingMode = !isInPointsEditingMode
                'r' -> {
                    if (isInPointsEditingMode) {
                        points.add(PointWidget(randomPoint(), diameter / 2))
                    }
                }
                'g' -> {
                    if (!polygonDefinitionEnabled) {
                        giftWrappingEnabled = !giftWrappingEnabled
                    }
                }
                's' -> {
                    if (!polygonDefinitionEnabled) {
                        grahamScanEnabled = !grahamScanEnabled
                    }
                }
                't' -> {
                    if (polygonPoints.count() > 2) {
                        triangulationEnabled = !triangulationEnabled
                    }
                }
                'p' -> {
                    polygonDefinitionEnabled = !polygonDefinitionEnabled
                }
                'k' -> {
                    kDTreesEnabled = !kDTreesEnabled
                }
                else -> if (selectedInputField != null) {
                    selectedInputField!!.text += key
                }
            }
        }
    }

    private fun randomPoint(): Point {
        val magicConstant = 50
        val x = random.nextInt(width - leftPanelSize - diameter - magicConstant) + leftPanelSize + diameter / 2 + magicConstant / 2
        val y = random.nextInt(height - diameter - magicConstant) + diameter / 2 + magicConstant / 2
        return Point(x, y)
    }

    private fun drawWidget(widget: AbstractWidget) {
        if (widget is InputFieldWidget) {
            drawInputField(widget)
        } else if (widget is TextWidget) {
            drawTextWidget(widget)
        }
    }

    private fun disableSelectedInputField() {
        if (selectedInputField != null) {
            selectedInputField?.isSelected = false
        }
        selectedInputField = null
    }

    private fun setSelectedInputField(widget: InputFieldWidget) {
        widget.text = ""
        selectedInputField = widget
        selectedInputField?.isSelected = true
    }

    private fun findPointContaining(x: Int, y: Int): PointWidget? {
        return points.firstOrNull { it.pointIsInside(x, y) }
    }

    private fun deletePointContaining(x: Int, y: Int) {
        points.removeIf { p -> p.pointIsInside(x, y) }
    }

    private fun drawInputField(inputField: InputFieldWidget) {
        fill(inputField.fill)
        stroke(inputField.stroke)
        val topLeftX = inputField.topLeft.x
        val topLeftY = inputField.topLeft.y
        rect(topLeftX.toFloat(), topLeftY.toFloat(), inputField.width.toFloat(), inputField.height.toFloat(), 2f)
        fill(inputField.stroke)
        text(inputField.text, (topLeftX + 5).toFloat(), (topLeftY + inputField.height - 10).toFloat())
    }

    private fun drawTextWidget(textWidget: TextWidget) {
        fill(0)
        stroke(0)
        text(textWidget.text, textWidget.bottomLeft.x.toFloat(), textWidget.bottomLeft.y.toFloat())
    }
}

fun List<Point>.rotate(amount: Int): List<Point> {
    val mutableList = toMutableList()
    Collections.rotate(mutableList, amount)
    return mutableList
}