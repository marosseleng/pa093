package cz.muni.fi.pa039;

import cz.muni.fi.pa039.widgets.AbstractWidget;
import cz.muni.fi.pa039.widgets.ButtonWidget;
import cz.muni.fi.pa039.widgets.InputFieldWidget;
import cz.muni.fi.pa039.widgets.PointWidget;
import cz.muni.fi.pa039.widgets.TextWidget;
import processing.core.PApplet;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * TODO:
 * -> CLEAR ALL button
 */
public class Sketch extends PApplet {

    private final Random random = new Random();

    private final Set<PointWidget> points = new HashSet<>();
    private final Set<AbstractWidget> widgets = new HashSet<>();

    /* SWITCHES */
    private boolean isInPointsEditingMode = true;
    private boolean giftWrappingEnabled = false;
    private boolean grahamScanEnabled = false;

    private String textToWrite = "";

    /**
     * A point we are currently dragging
     */
    private PointWidget currentDraggingPoint = null;
    /**
     * This variable serves as text input handler! When this is not null, all text input goes here
     */
    private InputFieldWidget selectedInputField = null;
    /**
     * Helper field to indicate that a button was selected
     */
    private ButtonWidget selectedButton = null;

    private int diameter = 12;
    private int windowSize = 800;
    private int leftPanelSize = 200;

    private void handleWidgetClick(AbstractWidget widget) {
        widget.setSelected(true);
        if (widget instanceof ButtonWidget) {
            selectedButton = (ButtonWidget) widget;
            handleButtonOnClickAction(widget.getId());
        }
        if (widget instanceof InputFieldWidget) {
            setSelectedInputField((InputFieldWidget) widget);
            return;
        }
        disableSelectedInputField();
    }

    private void handleButtonOnClickAction(int widgetId) {
        switch (widgetId) {
            case Ids.RANDOM_POINTS_GENERATE_BUTTON:
                generateRandomPoints();
                break;
        }
    }

    private void generateRandomPoints() {
        for (AbstractWidget w : widgets) {
            if (w.getId() == Ids.RANDOM_POINTS_INPUT_FIELD) {
                InputFieldWidget ifw = (InputFieldWidget) w;
                int amount;
                try {
                    amount = Integer.parseInt(ifw.text);
                } catch (Exception ex) {
                    amount = 0;
                }
                for (int i = 0; i < amount; i++) {
                    points.add(new PointWidget(randomPoint(), diameter / 2));
                }
            }
        }
    }


    public void settings() {
        fullScreen();
//        size(windowSize, windowSize);
    }

    public void setup() {
        background(255);
        fill(135, 175, 163);
        rect(0, 0, leftPanelSize, windowSize);
        stroke(0);
        fill(0);
        widgets.add(new TextWidget(new Point(25, 45), "Number of random points:", Ids.RANDOM_POINTS_TITLE_TEXT));
        widgets.add(new InputFieldWidget(new Point(25, 55), 50, 30, textToWrite, Ids.RANDOM_POINTS_INPUT_FIELD));
        widgets.add(new ButtonWidget(new Point(80, 55), 90, 30, "GENERATE!", Ids.RANDOM_POINTS_GENERATE_BUTTON));
    }

    public void draw() {
        background(255);
        fill(150, 241, 255);
        stroke(150, 241, 255);
        rect(0, 0, leftPanelSize, windowSize);
        fill(255, 0, 0);
        text(isInPointsEditingMode ? "Points editing ENABLED." : "Points editing DISABLED.", 25, 25);
        textToWrite = String.valueOf(System.currentTimeMillis());
        if (grahamScanEnabled && points.size() > 2) {
            performGrahamScan();
        }

        if (giftWrappingEnabled && points.size() > 2) {
            performGiftWrapping();
        }

        points.stream()
                .forEach(p -> {
                    fill(0);
                    stroke(0);
                    ellipse(p.point.x, p.point.y, p.radius * 2, p.radius * 2);
                });

        widgets.stream()
                .forEach(this::drawWidget);
    }

    private void performGiftWrapping() {

    }

    private void performGrahamScan() {

    }

    /* MOUSE EVENTS */

    public void mousePressed() {
        if (isInPointsEditingMode) {
            if (mouseButton == RIGHT) {
                deletePointContaining(mouseX, mouseY);
            } else {
                PointWidget found = findPointContaining(mouseX, mouseY);
                if (found == null) {
                    points.add(new PointWidget(new Point(mouseX, mouseY), diameter / 2));
                } else {
                    currentDraggingPoint = found;
                }
            }
        } else {
            Optional<AbstractWidget> selected = widgets.stream()
                    .filter(widget -> widget.pointIsInside(mouseX, mouseY))
                    .findFirst();
            if (selected.isPresent()) {
                handleWidgetClick(selected.get());
            } else {
                disableSelectedInputField();
            }
        }
    }

    public void mouseReleased() {
        if (!isInPointsEditingMode) {
            if (selectedButton != null) {
                selectedButton.setSelected(false);
            }
            selectedButton = null;
        } else {
            currentDraggingPoint = null;
        }
    }

    public void mouseDragged() {
        if (!isInPointsEditingMode) {
            return;
        }
        if (currentDraggingPoint != null) {
            points.remove(currentDraggingPoint);
            PointWidget p = new PointWidget(new Point(mouseX, mouseY), diameter / 2);
            currentDraggingPoint = p;
            points.add(p);
        }
    }

    /* KEYBOARD EVENTS */

    public void keyPressed() {
        switch (key) {
            case RETURN:
            case ENTER:
                isInPointsEditingMode = !isInPointsEditingMode;
                break;
            case 'p':
                if (isInPointsEditingMode) {
                    points.add(new PointWidget(randomPoint(), diameter / 2));
                }
            default:
                if (selectedInputField != null) {
                    selectedInputField.text += key;
                }
        }
    }

    private Point randomPoint() {
        int x = random.nextInt(windowSize - 200 - diameter) + 200 + diameter / 2;
        int y = random.nextInt(windowSize - diameter) + diameter / 2;
        return new Point(x, y);
    }

    private void drawWidget(AbstractWidget widget) {
        if (widget instanceof InputFieldWidget) {
            drawInputField((InputFieldWidget) widget);
        } else if (widget instanceof TextWidget) {
            drawTextWidget((TextWidget) widget);
        }
    }

    private void disableSelectedInputField() {
        if (selectedInputField != null) {
            selectedInputField.setSelected(false);
        }
        selectedInputField = null;
    }

    private void setSelectedInputField(InputFieldWidget widget) {
        widget.text = "";
        selectedInputField = widget;
        selectedInputField.setSelected(true);
    }

    private PointWidget findPointContaining(int x, int y) {
        for (PointWidget p : points) {
            if (p.pointIsInside(x, y)) {
                return p;
            }
        }
        return null;
    }

    private void deletePointContaining(int x, int y) {
        points.removeIf(p -> p.pointIsInside(x, y));
    }

    private void drawInputField(InputFieldWidget inputField) {
        fill(inputField.getFill());
        stroke(inputField.getStroke());
        int topLeftX = inputField.topLeft.x;
        int topLeftY = inputField.topLeft.y;
        rect(topLeftX, topLeftY, inputField.width, inputField.height, 2);
        fill(inputField.getStroke());
        text(inputField.text, topLeftX + 5, topLeftY + inputField.height - 10);
    }

    private void drawTextWidget(TextWidget textWidget) {
        fill(0);
        stroke(0);
        text(textWidget.text, textWidget.bottomLeft.x, textWidget.bottomLeft.y);
    }
}
