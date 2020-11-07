package meshIneBits.slicer.IA_util;

public class Point {
    private double abs, ord;

    public Point(double abs, double ord) {
        this.abs = abs;
        this.ord = ord;
    }

    public double getAbs() {
        return this.abs;
    }

    public void setAbs(double abs) {
        this.abs = abs;
    }

    public double getOrd() {
        return this.ord;
    }

    public void setOrd(double ord) {
        this.ord = ord;
    }
}