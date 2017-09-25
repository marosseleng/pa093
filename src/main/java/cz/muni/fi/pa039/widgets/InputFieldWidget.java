package cz.muni.fi.pa039.widgets;

import cz.muni.fi.pa039.Point;

public class InputFieldWidget extends AbstractWidget {
    public Point topLeft;
    public int width;
    public int height;
    public String text = "";

    private int id;

    public InputFieldWidget(Point topLeft, int width, int height, String text, int id) {
        this.topLeft = topLeft;
        this.width = width;
        this.height = height;
        this.text = text;
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean pointIsInside(int x, int y) {
        return x >= this.topLeft.x && x <= this.topLeft.x + width && y >= this.topLeft.y && y <= this.topLeft.y + height;
    }
}
