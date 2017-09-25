package cz.muni.fi.pa039.widgets;

public abstract class AbstractWidget {
    private boolean selected = false;

    public abstract boolean pointIsInside(int x, int y);
    public abstract int getId();

    public int getStroke() {
        return 0;
    }

    public int getFill() {
        return selected ? 230 : 255;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
