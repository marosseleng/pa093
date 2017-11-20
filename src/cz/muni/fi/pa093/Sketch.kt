package cz.muni.fi.pa093

import cz.muni.fi.pa093.impl.*
import cz.muni.fi.pa093.widget.*
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PFont
import java.awt.Color
import java.util.*

/**
 * TODO:
 *     * automatic repositioning of widgets on new widget addition
 */
class Sketch : PApplet() {

    private val widgets = setOf(
            TextWidget(Point(25, 300), "Number of random points:", Ids.RANDOM_POINTS_TITLE_TEXT),
            InputFieldWidget(Point(25, 310), 50, 30, "", Ids.RANDOM_POINTS_INPUT_FIELD),
            ButtonWidget(Point(80, 310), 90, 30, "GENERATE!", Ids.RANDOM_POINTS_GENERATE_BUTTON))

    private val points = HashSet<Point>()
    private var polygonPoints = mutableListOf<Point>()
    private var polygonClosed: Boolean = false
        set(value) {
            // turn off the polygon definition
            field = value
            if (value) {
                polygonDefinitionEnabled = false
            }
        }

    /*
     * A point we are currently dragging
     */
    private var currentDraggingPoint: Point? = null
    /*
     * This variable serves as text input handler! When this is not null, all text input goes here
     */
    private var selectedInputField: InputFieldWidget? = null
    /*
     * Helper field to indicate that a button was selected
     */
    private var selectedButton: ButtonWidget? = null


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

    private var convexHullEnabled: Boolean
        get() = grahamScanEnabled || giftWrappingEnabled
        set(value) {
            grahamScanEnabled = value
            giftWrappingEnabled = value
        }

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

    private var delaunayTriangulationEnabled: Boolean = false

    /*
     * CORE functions
     */
    override fun settings() {
        size(windowSize, windowSize)
    }

    override fun setup() {
        textSize(15f)
    }

    override fun draw() {
        background(255)
        fill(150f, 241f, 255f)
        stroke(150f, 241f, 255f)
        rect(0f, 0f, leftPanelSize.toFloat(), height.toFloat())
        fill(255f, 0f, 90f)
        text(if (isInPointsEditingMode) "[Enter] Points editing ENABLED." else "[Enter] Points editing DISABLED.", 25f, 25f)
        text("[X] CLEAR", 25f, 50f)
        text(if (grahamScanEnabled) "[S] Graham Scan ENABLED." else "[S] Graham Scan DISABLED.", 25f, 75f)
        text(if (giftWrappingEnabled) "[G] Gift Wrapping ENABLED." else "[G] Gift Wrapping DISABLED.", 25f, 100f)
        text(if (triangulationEnabled) "[T] Triangulation ENABLED." else "[T] Triangulation DISABLED.", 25f, 125f)
        text(if (polygonDefinitionEnabled) "[P] Polygon definition ENABLED." else "[P] Polygon definition DISABLED.", 25f, 150f)
        text(if (kDTreesEnabled) "[K] k-d trees ENABLED." else "[K] k-d trees DISABLED.", 25f, 175f)
        text(if (delaunayTriangulationEnabled) "[D] Delaunay triangulation ENABLED." else "[D] Delaunay triangulation DISABLED.", 25f, 200f)
        text("[R] Add random point.", 25f, 225f)
        text("[L_MOUSE] Add new point / move point.", 25f, 250f)
        text("[R_MOUSE] Delete the point.", 25f, 275f)

        widgets.forEach { it.draw(this) }

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
                giftWrappingEnabled -> giftWrapping(points)
                grahamScanEnabled -> grahamScan(points)
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
                ellipse(p.x.toFloat(), p.y.toFloat(), pointDiameter.toFloat(), pointDiameter.toFloat())
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
                ellipse(p.x.toFloat(), p.y.toFloat(), pointDiameter.toFloat(), pointDiameter.toFloat())
            }
        }

        /* kD trees */
        if (kDTreesEnabled) {
            drawKdTree(constructKdTree(points.toList()))
        }

        /* Delaunay triangulation */
        if (delaunayTriangulationEnabled) {
            val delaunay = computeDelaunay(points)
            delaunay.first.forEach {
                fill(0)
                stroke(0)
                line(it.first.x.toFloat(), it.first.y.toFloat(), it.second.x.toFloat(), it.second.y.toFloat())
            }

            delaunay.second.forEach {
                stroke(Color.RED.rgb, 40.0f)
                fill(255, 0.0f)
                ellipse(it.center.x.toFloat(), it.center.y.toFloat(), it.radius.toFloat() * 2.0f, it.radius.toFloat() * 2.0f)
                fill(Color.RED.rgb, 40.0f)
                ellipse(it.center.x.toFloat(), it.center.y.toFloat(), 4.0f, 4.0f)
            }
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
                    points.add(pnt)
                } else {
                    currentDraggingPoint = found
                    if (found == polygonPoints.firstOrNull()) {
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
            points.remove(currentDraggingPoint ?: points.first())
            val p = Point(mouseX, mouseY)
            currentDraggingPoint = p
            points.add(p)
        }
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

    /* KEYBOARD EVENTS */

    override fun keyPressed() {
        if (selectedInputField != null) {
            selectedInputField!!.text += key
        } else {
            when (key) {
                PConstants.RETURN, PConstants.ENTER -> isInPointsEditingMode = !isInPointsEditingMode
                'x' -> {
                    clearAll()
                }
                'r' -> {
                    if (isInPointsEditingMode) {
                        points.add(randomPoint())
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
                'd' -> {
                    delaunayTriangulationEnabled = !delaunayTriangulationEnabled
                    kotlin.io.println()
                }
                else -> if (selectedInputField != null) {
                    selectedInputField!!.text += key
                }
            }
        }
    }

    /* HELPER FUNCTIONS */

    private fun clearAll() {
        points.clear()
        polygonPoints.clear()
        polygonClosed = false

        isInPointsEditingMode = true
        polygonDefinitionEnabled = false
        convexHullEnabled = false
        triangulationEnabled = false
        kDTreesEnabled = false
        delaunayTriangulationEnabled = false
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
                .forEach { points.add(randomPoint()) }
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

    private fun findPointContaining(x: Int, y: Int) = points.firstOrNull {
        Math.pow((x - it.x), 2.0) + Math.pow((y - it.y), 2.0) <= Math.pow(pointDiameter / 2.0, 2.0)
    }

    private fun deletePointContaining(x: Int, y: Int) = points.removeIf {
        Math.pow((x - it.x), 2.0) + Math.pow((y - it.y), 2.0) <= Math.pow(pointDiameter / 2.0, 2.0)
    }

    private fun drawKdTree(node: KdNode?) {
        if (node == null) {
            return
        }
        val color = colors[node.depth % numOfColors]
        stroke(color)
        fill(color)
        ellipse(node.point.x.toFloat(), node.point.y.toFloat(), pointDiameter.toFloat(), pointDiameter.toFloat())

        val boundary = getLineBoundary(node) ?: return

        if (node.depth % 2 != 0) {
            line(boundary.first.toFloat(), node.point.y.toFloat(), boundary.second.toFloat(), node.point.y.toFloat())
        } else {
            line(node.point.x.toFloat(), boundary.first.toFloat(), node.point.x.toFloat(), boundary.second.toFloat())
        }
        drawKdTree(node.lesser)
        drawKdTree(node.greater)
    }
}