package cz.muni.fi.pa039.widgets;

import cz.muni.fi.pa039.Point;

public class LineWidget extends AbstractWidget {

    Point first;
    Point second;

    public LineWidget(Point first, Point second) {
        this.first = first;
        this.second = second;
    }
    @Override
    public boolean pointIsInside(int x, int y) {
        return false;
    }

    @Override
    public int getId() {
        return -2;
    }
}
