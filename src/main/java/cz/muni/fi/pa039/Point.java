package cz.muni.fi.pa039;

public class Point {

    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (!(other instanceof Point)) {
            return false;
        } else {
            Point p = (Point) other;
            return p.x == this.x && p.y == this.y;
        }
    }

    @Override
    public int hashCode() {
        return 37 * (this.x + 19 * this.y);
    }
}
