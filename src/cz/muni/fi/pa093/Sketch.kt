package cz.muni.fi.pa093

import cz.muni.fi.pa093.impl.*
import processing.core.PApplet
import processing.core.PConstants
import java.util.*

class Sketch : PApplet() {

    private val widgets = setOf(
            InputFieldWidget(Point(18, 85), 50, 30, "10", Ids.RANDOM_POINTS_INPUT_FIELD),
            ButtonWidget(Point(80, 85), 90, 30, "GENERATE!", Ids.RANDOM_POINTS_GENERATE_BUTTON))

    private val points = HashSet<Point>()
    private val polygonPoints = mutableListOf<Point>()
    private var polygonClosed: Boolean = false
        set(value) {
            field = value
            // turn off the polygon definition
            if (value) {
                polygonDefinitionEnabled = false
            }
        }

    /*
     * A point we are currently dragging
     */
    private var currentDraggingPoint: Point? = null
    /*
     * This variable serves as text input handler. When this is not null, all text input goes here
     */
    private var selectedInputField: InputFieldWidget? = null

    /* SWITCHES */
    private var isInPointsEditingMode = true
        set(value) {
            field = value
            selectedInputField = null
            widgets.forEach { it.isSelected = false }
            polygonDefinitionEnabled = false
        }

    private var giftWrappingEnabled: Boolean = false
        set(value) {
            field = value
            if (grahamScanEnabled) {
                grahamScanEnabled = !value
            }
            if (!value && !grahamScanEnabled) {
                triangulationEnabled = false
                polygonPoints.clear()
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
                polygonPoints.clear()
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

    private var voronoiDiagramEnabled: Boolean = false

    private var circumCirclesEnabled: Boolean = false

    /*
     * PROCESSING CORE functions
     */
    override fun settings() {
        size(windowSize, windowSize)
    }

    override fun setup() {
        textSize(15f)
    }

    override fun draw() {
        background(255)

        /* CONVEX HULL COMPUTATION */
        if ((grahamScanEnabled || giftWrappingEnabled) && points.size > 2) {
            polygonPoints.clear()
            polygonPoints.addAll(when {
                giftWrappingEnabled -> giftWrapping(points)
                grahamScanEnabled -> grahamScan(points)
                else -> emptyList()
            })
            polygonClosed = polygonPoints.isNotEmpty()
        }

        /* TRIANGULATION */
        if (triangulationEnabled && polygonPoints.count() > 2) {
            /* DRAW ONLY POINTS THAT BELONG TO POLYGON */
            drawPoints(polygonPoints)
            drawLines(triangulation(adjustPointsOrder(polygonPoints)))
        } else {
            /* DRAW ALL POINTS */
            drawPoints(points)
        }

        /* POLYGON */
        if (polygonPoints.isNotEmpty()) {
            drawPolygon(
                    polygonPoints = polygonPoints,
                    polygonClosed = polygonClosed)
        }

        /* kD trees */
        if (kDTreesEnabled) {
            drawKdTree(
                    node = constructKdTree(points.toList()))
        }

        /* DELAUNAY TRIANGULATION AND VORONOI DIAGRAM */
        if (delaunayTriangulationEnabled || voronoiDiagramEnabled) {
            drawDelaunayOrVoronoi(
                    delaunay = computeDelaunay(points),
                    delaunayTriangulationEnabled = delaunayTriangulationEnabled,
                    voronoiDiagramEnabled = voronoiDiagramEnabled,
                    circumCirclesEnabled = circumCirclesEnabled)
        }

        /* UI/controls */
        fill(150f, 241f, 255f)
        stroke(150f, 241f, 255f)
        rect(0f, 0f, leftPanelSize.toFloat(), height.toFloat())
        fill(0)
        stroke(0)
        text(if (isInPointsEditingMode) "[Enter] Mouse editing ENABLED." else "[Enter] Mouse editing DISABLED.", 18f, 25f)

        if (isInPointsEditingMode) {
            text("[L_MOUSE] Add new point / move point.", 18f, 75f)
            text("[R_MOUSE] Delete the point.", 18f, 100f)
            text("[X] CLEAR", 18f, 125f)
            text("[R] Add random point.", 18f, 150f)
            text(if (polygonDefinitionEnabled) "[P] Polygon definition ENABLED." else "[P] Polygon definition DISABLED.", 18f, 175f)
        } else {
            text("Number of random points:", 18f, 75f)
            widgets.forEach { it.draw(this) }
        }

        /* ACTIONS */
        fill(255f, 0f, 90f)
        text(if (grahamScanEnabled) "[S] Graham Scan ENABLED." else "[S] Graham Scan DISABLED.", 18f, 215f)
        text(if (giftWrappingEnabled) "[G] Gift Wrapping ENABLED." else "[G] Gift Wrapping DISABLED.", 18f, 240f)
        text(if (triangulationEnabled) "[T] Triangulation ENABLED." else "[T] Triangulation DISABLED.", 18f, 265f)
        text(if (kDTreesEnabled) "[K] k-d trees ENABLED." else "[K] k-d trees DISABLED.", 18f, 290f)
        text(if (delaunayTriangulationEnabled) "[D] Delaunay triangulation ENABLED." else "[D] Delaunay triangulation DISABLED.", 18f, 315f)
        text(if (voronoiDiagramEnabled) "[V] Voronoi diagram ENABLED." else "[V] Voronoi diagram DISABLED.", 18f, 340f)
        text(if (circumCirclesEnabled) "[C] Circum circles ENABLED." else "[C] Circum circles DISABLED.", 18f, 365f)


        fill(0)
        stroke(0)
        text("Total points: ${points.count()}", 18f, 950f)
        text("Points on polygon: ${polygonPoints.count()}", 18f, 975f)
    }

    /* MOUSE EVENTS */

    override fun mousePressed() {
        if (isInPointsEditingMode) {
            if (mouseX <= leftPanelSize) {
                // do not handle point manipulation in the left panel
                return
            }
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
        if (isInPointsEditingMode) {
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
        when (widget) {
            is ButtonWidget -> {
                handleButtonOnClickAction(widget.id)
                disableSelectedInputField()
                widget.isSelected = false
            }
            is InputFieldWidget -> {
                setSelectedInputField(widget)
            }
        }
    }

    private fun handleButtonOnClickAction(widgetId: Int) {
        when (widgetId) {
            Ids.RANDOM_POINTS_GENERATE_BUTTON -> generateRandomPoints()
        }
    }

    /* KEYBOARD EVENTS */

    override fun keyPressed() {
        if (selectedInputField != null && selectedInputField is InputFieldWidget) {
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
                }
                'v' -> {
                    voronoiDiagramEnabled = !voronoiDiagramEnabled
                }
                'c' -> {
                    circumCirclesEnabled = !circumCirclesEnabled
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
        voronoiDiagramEnabled = false
        circumCirclesEnabled = false
    }

    /*
     * Generates n random points where n is the user input
     */
    private fun generateRandomPoints() {
        widgets
                .filter { it.id == Ids.RANDOM_POINTS_INPUT_FIELD }
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
}

fun main(args: Array<String>) {
    PApplet.main(Sketch::class.qualifiedName)
}