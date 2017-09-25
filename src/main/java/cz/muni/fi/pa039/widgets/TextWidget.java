package cz.muni.fi.pa039.widgets;

import cz.muni.fi.pa039.Point;

public class TextWidget extends AbstractWidget {

    public Point bottomLeft;
    public String text;

    private int id;

    public TextWidget(Point bottomLeft, String text, int id) {
        this.bottomLeft = bottomLeft;
        this.text = text;
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getFill() {
        return 0;
    }

    @Override
    public boolean pointIsInside(int x, int y) {
        return false;
    }
}
