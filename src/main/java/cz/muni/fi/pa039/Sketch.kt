package cz.muni.fi.pa039

import cz.muni.fi.pa039.widgets.AbstractWidget
import cz.muni.fi.pa039.widgets.ButtonWidget
import cz.muni.fi.pa039.widgets.InputFieldWidget
import cz.muni.fi.pa039.widgets.PointWidget
import cz.muni.fi.pa039.widgets.TextWidget
import processing.core.PApplet
import processing.core.PConstants

import java.util.HashSet
import java.util.Random

/**
 * TODO:
 * -> CLEAR ALL button
 */
class Sketch : PApplet() {

    private val random = Random()

    private val points = HashSet<PointWidget>()
    private val widgets = HashSet<AbstractWidget>()

    /* SWITCHES */
    private var isInPointsEditingMode = true
    private val giftWrappingEnabled = false
    private val grahamScanEnabled = false

    private var textToWrite = ""

    /**
     * A point we are currently dragging
     */
    private var currentDraggingPoint: PointWidget? = null
    /**
     * This variable serves as text input handler! When this is not null, all text input goes here
     */
    private var selectedInputField: InputFieldWidget? = null
    /**
     * Helper field to indicate that a button was selected
     */
    private var selectedButton: ButtonWidget? = null

    private val diameter = 12
    private val windowSize = 800
    private val leftPanelSize = 200

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

    private fun generateRandomPoints() {
        for (w in widgets) {
            if (w.id == Ids.RANDOM_POINTS_INPUT_FIELD) {
                val ifw = w as InputFieldWidget
                var amount: Int
                try {
                    amount = Integer.parseInt(ifw.text)
                } catch (ex: Exception) {
                    amount = 0
                }

                for (i in 0 until amount) {
                    points.add(PointWidget(randomPoint(), diameter / 2))
                }
            }
        }
    }


    override fun settings() {
        fullScreen()
        //        size(windowSize, windowSize);
    }

    override fun setup() {
        background(255)
        fill(135f, 175f, 163f)
        rect(0f, 0f, leftPanelSize.toFloat(), windowSize.toFloat())
        stroke(0)
        fill(0)
        widgets.add(TextWidget(Point(25, 45), "Number of random points:", Ids.RANDOM_POINTS_TITLE_TEXT))
        widgets.add(InputFieldWidget(Point(25, 55), 50, 30, textToWrite, Ids.RANDOM_POINTS_INPUT_FIELD))
        widgets.add(ButtonWidget(Point(80, 55), 90, 30, "GENERATE!", Ids.RANDOM_POINTS_GENERATE_BUTTON))
    }

    override fun draw() {
        background(255)
        fill(150f, 241f, 255f)
        stroke(150f, 241f, 255f)
        rect(0f, 0f, leftPanelSize.toFloat(), windowSize.toFloat())
        fill(255f, 0f, 0f)
        text(if (isInPointsEditingMode) "Points editing ENABLED." else "Points editing DISABLED.", 25f, 25f)
        textToWrite = System.currentTimeMillis().toString()
        if (grahamScanEnabled && points.size > 2) {
            performGrahamScan()
        }

        if (giftWrappingEnabled && points.size > 2) {
            performGiftWrapping()
        }

        points.forEach { p ->
                    fill(0)
                    stroke(0)
                    ellipse(p.point.x.toFloat(), p.point.y.toFloat(), (p.radius * 2).toFloat(), (p.radius * 2).toFloat())
                }

        widgets.forEach(this::drawWidget)
    }

    private fun performGiftWrapping() {

    }

    private fun performGrahamScan() {

    }

    /* MOUSE EVENTS */

    override fun mousePressed() {
        if (isInPointsEditingMode) {
            if (mouseButton == PConstants.RIGHT) {
                deletePointContaining(mouseX, mouseY)
            } else {
                val found = findPointContaining(mouseX, mouseY)
                if (found == null) {
                    points.add(PointWidget(Point(mouseX, mouseY), diameter / 2))
                } else {
                    currentDraggingPoint = found
                }
            }
        } else {
            val selected = widgets.stream()
                    .filter { widget -> widget.pointIsInside(mouseX, mouseY) }
                    .findFirst()
            if (selected.isPresent) {
                handleWidgetClick(selected.get())
            } else {
                disableSelectedInputField()
            }
        }
    }

    override fun mouseReleased() {
        if (!isInPointsEditingMode) {
            if (selectedButton != null) {
                selectedButton!!.isSelected = false
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
            points.remove(currentDraggingPoint!!)
            val p = PointWidget(Point(mouseX, mouseY), diameter / 2)
            currentDraggingPoint = p
            points.add(p)
        }
    }

    /* KEYBOARD EVENTS */

    override fun keyPressed() {
        when (key) {
            PConstants.RETURN, PConstants.ENTER -> isInPointsEditingMode = !isInPointsEditingMode
            'p' -> {
                if (isInPointsEditingMode) {
                    points.add(PointWidget(randomPoint(), diameter / 2))
                }
                if (selectedInputField != null) {
                    selectedInputField!!.text += key
                }
            }
            else -> if (selectedInputField != null) {
                selectedInputField!!.text += key
            }
        }
    }

    private fun randomPoint(): Point {
        val x = random.nextInt(windowSize - 200 - diameter) + 200 + diameter / 2
        val y = random.nextInt(windowSize - diameter) + diameter / 2
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
            selectedInputField!!.isSelected = false
        }
        selectedInputField = null
    }

    private fun setSelectedInputField(widget: InputFieldWidget) {
        widget.text = ""
        selectedInputField = widget
        selectedInputField!!.isSelected = true
    }

    private fun findPointContaining(x: Int, y: Int): PointWidget? {
        for (p in points) {
            if (p.pointIsInside(x, y)) {
                return p
            }
        }
        return null
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
