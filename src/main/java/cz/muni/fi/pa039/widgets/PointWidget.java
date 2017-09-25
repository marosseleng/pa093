package cz.muni.fi.pa039.widgets;

import cz.muni.fi.pa039.Point;

public class PointWidget extends AbstractWidget {

    public Point point;
    public int radius;

    public PointWidget(Point point, int radius) {
        this.point = point;
        this.radius = radius;
    }

    @Override
    public int getStroke() {
        return 0;
    }

    @Override
    public int getFill() {
        return 0;
    }

    @Override
    public int getId() {
        return -1;
    }

    @Override
    public boolean pointIsInside(int x, int y) {
        return Math.sqrt(Math.pow(x - this.point.x, 2) + Math.pow(y - this.point.y, 2)) <= radius;
    }
}